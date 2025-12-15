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
            if (file.getParentFile().equals(returnUserFolder())) {
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
                    sqLiteDAO.getFolderMetadataFromIdPathAndName(parentFolderIdPath, file.getName(), userSession.getId());
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
        return folderId != 0 ? sqLiteDAO.queryFolderMetadata(folderId, userSession.getId()).getPath() : "0";
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
                resolvePathFromIdString(sqLiteDAO.queryFolderMetadata(folderId, userSession.getId()).getPath())
                :
                encodeBase32UserFolderName(userSession.getId(), userSession.getName(), userSession.getMail());
    }

    public List<Path> getFileAndFolderPathsFromFolder(String folderPath) throws IOException {
        List<Path> fileList;
        try (Stream<Path> stream = Files.list(Path.of(fileStorageProperties.getBasePath() + folderPath))) {
            fileList = stream.toList();
        }
        return fileList;
    }

    //TODO Bug inside probeContentType() it cant detect 'yaml' format returns null instead of document of type
    //TODO consider tika-core
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

    private FolderMetadata getFolderMetadataByIdFromList(List<FolderMetadata> list, long targetId) {
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
                fullPath.append(encodeBase32UserFolderName(userSession.getId(), userSession.getName(), userSession.getMail()))
                        .append(File.separator);
                continue;
            }
            FolderMetadata getMetadataFromList = getFolderMetadataByIdFromList(folderMetadataListById, folderIdList.get(i));
            logger.info("IS RETURN NULL {}", (getMetadataFromList == null));
            if (getMetadataFromList == null)
                throw new FileSystemException("No match found for ID " + folderIdList.get(i));
            fullPath.append(getMetadataFromList.getName()).append(File.separator);
        }
        fullPath.setLength(fullPath.length() - 1);
        logger.info("output {}", fullPath);
        return fullPath.toString();
    }

    private String returnCorrectSeparatorRegex() {
        return System.getProperty("os.name").toLowerCase().contains("windows") ? "\\\\" : "/";
    }

    // Generate ID path from System path
    //TODO cut folder path before generating
    public String generateIdPaths(String filePath, String startingIdPath) throws IOException {
        logger.info("String path {}", filePath);
        String[] folders = filePath.split(returnCorrectSeparatorRegex());
        StringBuilder idPath = new StringBuilder();
        //HOPEFULLY generate ID path starting from '0/'
        // cut beginning of path before to avoid having boolean conditional
        // use replace all pattern : returnUserFolder() replace with: ""
        boolean startAdding = false;
        int depth = 0;
        idPath.append(startingIdPath).append("/");
        for (String folderName : folders) {
            if (folderName.equals(returnUserFolder().getName())) {
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

    public File createUserDirectory(long userId, String username, String mail) throws IOException {
        String encodedUserFolder = encodeBase32UserFolderName(userId, username, mail);
        File userDirectory = new File(fileStorageProperties.getFullPath(encodedUserFolder));
        if (Files.notExists(userDirectory.toPath())) {
            Files.createDirectories(userDirectory.toPath());
            if (!Files.exists(userDirectory.toPath())) {
                throw new FileSystemException("Could not create user directory");
            }
        }
        return userDirectory;
    }

    public File returnUserFolder() throws IOException {
        return createUserDirectory(userSession.getId(), userSession.getName(), userSession.getMail());
    }

    public String encodeBase32UserFolderName(long userId, String username, String mail) {
        String encode = userId + ":" + username + ":" + mail;
        return Base64.getUrlEncoder().withoutPadding().encodeToString(encode.getBytes());
    }

    public String decodeBase32UserFolderName(String base32String) {
        byte[] decodedString = Base64.getUrlDecoder().decode(base32String.getBytes());
        return new String(decodedString);
    }

    // alternative algorithm to walk file tree
    // potential candidate for move operation
    public void traverseFileTree(Path startingPath) throws IOException {
        int skippedFileCount = 0, skippedFolderCountInside = 0, skippedDuplicateCount = 0, discoveredFolderCount = 0, discoveredFileCount = 0;
        File lastFolder = new File("");
        List<Path> fileList = walkFsTree(startingPath, false);
        for (Path orgPath : fileList) {
            if (orgPath.toFile().isFile()) {
                skippedFileCount++;
                continue;
            }
            if (lastFolder.equals(orgPath.toFile())) {
                skippedDuplicateCount++;
                continue;
            }
            logger.info("CURRENT FOLDER -> {}", orgPath);
            discoveredFolderCount++;
            List<Path> returns = walkFsTree(orgPath, false);
            for (Path paths : returns) {
                if (!paths.toFile().isFile()) {
                    skippedFolderCountInside++;
                    continue;
                }
                logger.info("-> FILE {}", paths);
                discoveredFileCount++;
            }
            lastFolder = orgPath.toFile();
        }
        logger.info("Traversal complete, results:" +
                        "\nSkipped File count: {}" +
                        "\nSkipped Folder Count Inside: {}" +
                        "\nSkipped duplicate count: {}" +
                        "\nDiscovered Folder Count: {}" +
                        "\nDiscovered File Count: {}",
                skippedFileCount, skippedFolderCountInside, skippedDuplicateCount, discoveredFolderCount, discoveredFileCount);
    }
}
