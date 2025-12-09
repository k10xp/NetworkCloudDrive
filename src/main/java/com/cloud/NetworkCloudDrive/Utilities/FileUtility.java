package com.cloud.NetworkCloudDrive.Utilities;

import com.cloud.NetworkCloudDrive.DAO.SQLiteDAO;
import com.cloud.NetworkCloudDrive.Models.FileMetadata;
import com.cloud.NetworkCloudDrive.Models.FolderMetadata;
import com.cloud.NetworkCloudDrive.Properties.FileStorageProperties;
import com.cloud.NetworkCloudDrive.Sessions.UserSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Stream;

@Component
public class FileUtility {
    private final FileStorageProperties fileStorageProperties;
    private final SQLiteDAO sqLiteDAO;
    private final UserSession userSession;
    private final Logger logger = LoggerFactory.getLogger(FileUtility.class);

    public FileUtility(SQLiteDAO sqLiteDAO, FileStorageProperties fileStorageProperties, UserSession userSession) {
        this.fileStorageProperties = fileStorageProperties;
        this.userSession = userSession;
        this.sqLiteDAO = sqLiteDAO;
    }

    public List<Path> walkFsTree(Path dir, boolean reverse) throws IOException {
        try (Stream<Path> fileTree = Files.walk(dir)) {
            return (reverse ? fileTree.sorted(Comparator.reverseOrder()) : fileTree).toList();
        } catch (IOException e) {
            throw new IOException("Failed to walk file tree. " + e.getMessage());
        }
    }

    //WHAT A MESS
    public void deleteFsTree(Path dir, String startingIdPath) throws IOException {
        logger.info("Start File Tree deletion operation");
        long errorCount = 0;
        List<Path> fileTreeStream = walkFsTree(dir, true);
        for (Path path : fileTreeStream) {
            File file = path.toFile();
            if (file.getParentFile().equals(new File(fileStorageProperties.getFullPath(userSession.getName())))) {
                logger.info("Skipped base path");
                continue;
            }
            if (!Files.exists(file.toPath())) {
                errorCount++;
                continue;
            }
            if (file.isFile()) {
                // use deleteIfExists at prod
                String parentFolderIdPath = generateIdPaths(file.getParentFile().getPath(), startingIdPath);
                logger.info("generated file path: {}", parentFolderIdPath);
                FolderMetadata folderMetadata =
                        sqLiteDAO.getFolderMetadataFromIdPathAndName(
                                parentFolderIdPath, file.getParentFile().getName(), userSession.getId());
                FileMetadata output = sqLiteDAO.getFileMetadataByFolderIdNameAndUserId(
                        folderMetadata.getId(), file.getName(), userSession.getId());
                if (!Files.deleteIfExists(file.toPath())) {
                    errorCount++;
                    continue;
                }
                sqLiteDAO.deleteFile(sqLiteDAO.getFileMetadataByFolderIdNameAndUserId(
                        folderMetadata.getId(), file.getName(), userSession.getId()));
                logger.info("File metadata: name {} path {} Id {}", output.getName(), output.getFolderId(), output.getId());
                continue;
            }
            // some progress
            String parentFolderIdPath = generateIdPaths(file.getPath(), startingIdPath);
            logger.info("generated folder path: {}", parentFolderIdPath);
            FolderMetadata folderMetadata =
                    sqLiteDAO.getFolderMetadataFromIdPathAndName(parentFolderIdPath, file.getName(), 0L);
            // manage folders here
            //temporary comment out to test
            if (!Files.deleteIfExists(file.toPath())) {
                errorCount++;
                continue;
            }
            sqLiteDAO.deleteFolder(folderMetadata);
            //check if it's correct
            logger.info("Folder metadata: name {} path {} Id {}", folderMetadata.getName(), folderMetadata.getPath(), folderMetadata.getId());
        }
        logger.info("Completed file tree deletion operation. Error count {}", errorCount);
    }

    public String getIdPath(long folderId) throws SQLException {
        return folderId != 0 ? sqLiteDAO.queryFolderMetadata(folderId).getPath() : "0";
    }

    public File returnIfItsNotADuplicate(String path) throws FileNotFoundException {
        File checkDuplicate = new File(path);
        if (Files.exists(checkDuplicate.toPath()))
            throw new FileNotFoundException(String.format("%s with name %s already exists at %s",
                    (checkDuplicate.isFile() ? "File" : "Folder"), checkDuplicate.getName(), checkDuplicate.getPath()));
        return checkDuplicate;
    }

    public File returnFileIfItExists(String path) throws FileNotFoundException {
        File checkExists = new File(fileStorageProperties.getBasePath() + path);
        if (!Files.exists(checkExists.toPath()))
            throw new FileNotFoundException(String.format("%s does not exist at path %s",
                    (checkExists.isFile() ? "File" : "Folder"),checkExists.getPath()));
        return checkExists;
    }

    public String getFolderPath(long folderId) throws SQLException, FileSystemException {
        return folderId != 0
                ?
                resolvePathFromIdString(sqLiteDAO.queryFolderMetadata(folderId).getPath())
                :
                userSession.getName();
    }

    public List<Path> getFileAndFolderPathsFromFolder(String folderPath) throws IOException {
        List<Path> fileList;
        try (Stream<Path> stream = Files.list(Path.of(fileStorageProperties.getBasePath() + folderPath))) {
            fileList = stream.toList();
        }
        return fileList;
    }

    public boolean checkAndMakeDirectories(String path) {
        File filePath = new File(path);
        return filePath.exists() || filePath.mkdirs();
    }

    //TODO Bug inside probeContentType() it cant detect 'yaml' format returns null instead of document of type
    // TODO consider tika-core
    public String getMimeTypeFromExtension(Path filePath) throws IOException {
        logger.info("File at path absolute {}, {}", filePath.toAbsolutePath(), filePath);
        return Files.probeContentType(filePath);
    }

    public String getFileExtension(String fileName) {
        StringBuilder ext = new StringBuilder();
        boolean afterDot = false;
        for (int i = 0; i < fileName.length(); i++) {
            if (fileName.charAt(i) == '.') afterDot = !afterDot;
            ext.append(afterDot ? fileName.charAt(i) : "");
        }
        return ext.toString();
    }

    public FolderMetadata getFolderMetadataByIdFromList(List<FolderMetadata> list, long targetId) {
        for (FolderMetadata folderMetadata : list) {
            if (targetId == folderMetadata.getId())
                return folderMetadata;
        }
        return null;
    }

    public String resolvePathFromIdString(String idString) throws FileSystemException {
        String[] splitLine = idString.split("/");
        List<Long> idList = new ArrayList<>();
        for (String idAsString : splitLine) {
            logger.debug("added {}", idAsString);
            idList.add(Long.parseLong(idAsString));
        }
        logger.info("id size {}", idList.size());
        return appendFolderNames(idList);
    }

    private String appendFolderNames(List<Long> folderIdList) throws FileSystemException {
        StringBuilder fullPath = new StringBuilder();
        List<FolderMetadata> folderMetadataListById = sqLiteDAO.findAllByIdInSQLFolderMetadata(folderIdList, userSession.getId());
        logger.info("size {}", folderMetadataListById.size());
        for (int i = 0; i < folderIdList.size(); i++) {
            if (i == 0) {
                fullPath.append(userSession.getName()).append(File.separator);
                continue;
            }
            String fileNameFromId = getFolderMetadataByIdFromList(folderMetadataListById, folderIdList.get(i)).getName();
            if (fileNameFromId == null) throw new FileSystemException("No match found for ID " + folderIdList.get(i));
            fullPath.append(fileNameFromId).append(File.separator);
        }
        fullPath.setLength(fullPath.length() - 1);
        logger.info("output {}", fullPath);
        return fullPath.toString();
    }

    public String returnCorrectSeparatorRegex() {
        return System.getProperty("os.name").toLowerCase().contains("windows") ? "\\\\" : "/";
    }

    // Generate ID path from System path
    //TODO cut folder path before generating
    public String generateIdPaths(String filePath, String startingIdPath) {
        logger.info("String path {}", filePath);
        String[] folders = filePath.split(returnCorrectSeparatorRegex());
        StringBuilder idPath = new StringBuilder();
        //HOPEFULLY generate ID path starting from '0/'
        // cut beginning of path before to avoid having boolean conditional
        boolean startAdding = false;
        int depth = 0;
        idPath.append(startingIdPath).append("/");
        for (String folderName : folders) {
            if (folderName.equals(userSession.getName())) {
                startAdding = !startAdding;
            }
            if (startAdding) {
                depth++;
                List<FolderMetadata> folderResults = sqLiteDAO.findAllContainingSectionOfIdPathIgnoreCase(idPath.toString(), userSession.getId());
                for (FolderMetadata folderMetadata : folderResults) {
                    String[] splitId = folderMetadata.getPath().split("/");
                    if (splitId.length == depth) {
                        idPath.append(folderMetadata.getId()).append("/");
                        break;
                    }
                }
            }
        }
        idPath.setLength(idPath.length() - 1);
        return idPath.toString();
    }
}
