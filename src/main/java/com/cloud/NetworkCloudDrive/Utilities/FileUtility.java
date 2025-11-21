package com.cloud.NetworkCloudDrive.Utilities;

import com.cloud.NetworkCloudDrive.Models.FolderMetadata;
import com.cloud.NetworkCloudDrive.Properties.FileStorageProperties;
import com.cloud.NetworkCloudDrive.Repositories.SQLiteFolderRepository;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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

    public String getIdPath(long folderId) throws FileNotFoundException {
        return folderId != 0 ? queryUtility.findFolderById(folderId).getPath() : "0";
    }

    public String getFolderPath(long folderId) throws FileNotFoundException {
        return folderId != 0
                ?
                resolvePathFromIdString(queryUtility.findFolderById(folderId).getPath())
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
        FolderMetadata result = new FolderMetadata();
        for (FolderMetadata folderMetadata : list) {
            if (targetId == folderMetadata.getId()) result = folderMetadata;
        }
        return result;
    }

    public String resolvePathFromIdString(String idString) {
        String[] splitLine = idString.split("/");
        List<Long> idList = new ArrayList<>();
        for (String idAsString : splitLine) {
            idList.add(Long.parseLong(idAsString));
        }
        return appendFolderNames(idList);
    }

    private String appendFolderNames(List<Long> folderIdList) {
        StringBuilder fullPath = new StringBuilder();
        List<FolderMetadata> folderMetadataListById = queryUtility.findAllByIdInSQLFolderMetadata(folderIdList);
        for (int i = 0; i < folderIdList.size(); i++) {
            if (i == 0) {
                fullPath.append(fileStorageProperties.getOnlyUserName()).append(File.separator);
                continue;
            }
            fullPath.append(getFolderMetadataByIdFromList(folderMetadataListById, folderIdList.get(i)).getName()).append(File.separator);
        }
        fullPath.setLength(fullPath.length() - 1);
        return fullPath.toString();
    }
}
