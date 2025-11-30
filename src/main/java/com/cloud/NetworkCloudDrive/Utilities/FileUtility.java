package com.cloud.NetworkCloudDrive.Utilities;

import com.cloud.NetworkCloudDrive.Models.FileMetadata;
import com.cloud.NetworkCloudDrive.Models.FolderMetadata;
import com.cloud.NetworkCloudDrive.Properties.FileStorageProperties;
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
    private final QueryUtility queryUtility;
    private final Logger logger = LoggerFactory.getLogger(FileUtility.class);

    public FileUtility(QueryUtility queryUtility, FileStorageProperties fileStorageProperties) {
        this.fileStorageProperties = fileStorageProperties;
        this.queryUtility = queryUtility;
    }

    //TODO implement folder type handling
    public void walkFsTree(Path dir) throws IOException {
        List<FileMetadata> files = new ArrayList<>();
        try(Stream<Path> fileTree = Files.walk(dir)) {
            List<Path> fileTreeStream = fileTree.sorted(Comparator.reverseOrder()).toList();
            for (Path path : fileTreeStream) {
                File file = path.toFile();
                if (file.isFile()) {
                    String parentFolderIdPath = generateIdPaths(file.getParent());
                    FolderMetadata folderMetadata =
                            queryUtility.getFolderMetadataFromIdPathAndName(parentFolderIdPath, file.getParentFile().getName(), 0L);
                    queryUtility.getFileMetadataByFolderIdNameAndUserId(folderMetadata.getId(), file.getName(), 0L);
                    continue;
                }
                // manage folders here

            }
        } catch (Exception e) {
            logger.error("Failed to traverse file system tree {}", e.getMessage());
        }
    }

    public String getIdPath(long folderId) throws SQLException {
        return folderId != 0 ? queryUtility.queryFolderMetadata(folderId).getPath() : "0";
    }

    public File returnIfItsNotADuplicate(String path) throws FileNotFoundException {
        File checkDuplicate = new File(path);
        if (!Files.exists(checkDuplicate.toPath()))
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
                resolvePathFromIdString(queryUtility.queryFolderMetadata(folderId).getPath())
                :
                fileStorageProperties.getOnlyUserName();
    }

    public String concatIdPaths(String former, long latterId) {
        return former.concat("/" + latterId);
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
            logger.info("added {}", idAsString);
            idList.add(Long.parseLong(idAsString));
        }
        logger.info("id size {}", idList.size());
        return appendFolderNames(idList);
    }

    private String appendFolderNames(List<Long> folderIdList) throws FileSystemException {
        StringBuilder fullPath = new StringBuilder();
        List<FolderMetadata> folderMetadataListById = queryUtility.findAllByIdInSQLFolderMetadata(folderIdList);
        logger.info("size {}", folderMetadataListById.size());
        for (int i = 0; i < folderIdList.size(); i++) {
            if (i == 0) {
                fullPath.append(fileStorageProperties.getOnlyUserName()).append(File.separator);
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

    // Generate ID path from System path
    public String generateIdPaths(String filePath) {
        String[] folders = filePath.split("/");
        StringBuilder idPath = new StringBuilder();
        //HOPEFULLY generate ID path starting from '0/'
        // cut beginning of path before to avoid having boolean conditional
        boolean startAdding = false;
        int depth = 0;
        idPath.append(0).append("/");
        for (String folderName : folders) {
            if (folderName.equals(fileStorageProperties.getOnlyUserName())) {
                startAdding = !startAdding;
            }
            if (startAdding) {
                depth++;
                List<FolderMetadata> folderResults = queryUtility.findAllContainingSectionOfIdPathIgnoreCase(idPath.toString());
                for (FolderMetadata folderMetadata : folderResults) {
                    String[] splitId = folderMetadata.getPath().split("/");
                    if (splitId.length == depth) {
                        idPath.append(folderMetadata.getId()).append("/");
                        break;
                    }
                }
            }
        }
        return idPath.toString();
    }
}
