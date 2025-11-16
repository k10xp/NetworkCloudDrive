package com.cloud.NetworkCloudDrive.Utilities;

import com.cloud.NetworkCloudDrive.Models.FolderMetadata;
import com.cloud.NetworkCloudDrive.Properties.FileStorageProperties;
import com.cloud.NetworkCloudDrive.Repositories.SQLiteFolderRepository;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

@Component
public class FileUtility {
    private final SQLiteFolderRepository sqLiteFolderRepository;
    private final FileStorageProperties fileStorageProperties;
    private final Logger logger = LoggerFactory.getLogger(FileUtility.class);

    public FileUtility(SQLiteFolderRepository sqLiteFolderRepository, FileStorageProperties fileStorageProperties) {
        this.sqLiteFolderRepository = sqLiteFolderRepository;
        this.fileStorageProperties = fileStorageProperties;
    }

    public List<Path> getFileAndFolderPathsFromFolder(String folderPath) throws IOException {
        List<Path> fileList;
        try(Stream<Path> stream = Files.list(Path.of(fileStorageProperties.getBasePath() +  folderPath))) {
            fileList = stream.toList();
        }
        return fileList;
    }

    public boolean checkAndMakeDirectories(String path) {
        File filePath = new File(path);
        return filePath.exists() || filePath.mkdirs();
    }

    public String getMimeTypeFromExtension(Path filePath) throws IOException {
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

    public String resolvePathFromIdString(String idString) {
        String[] splitLine = idString.split("/");
        StringBuilder fullPath = new StringBuilder();
        List<Long> idList = new ArrayList<>();
        for (String idAsString : splitLine) {
            idList.add(Long.parseLong(idAsString));
        }
        List<FolderMetadata> folderMetadataListById = sqLiteFolderRepository.findAllById(idList);
        fullPath.append(fileStorageProperties.getOnlyUserName()).append(File.separator);
        for (FolderMetadata folderMetadata : folderMetadataListById) {
            fullPath.append(folderMetadata.getName()).append(File.separator);
        }
        fullPath.setLength(fullPath.length()-1); //remove last '/'
        logger.info("resolved path {}", fullPath);
        return fullPath.toString();
    }

    // Currently unused functions

    public List<FolderMetadata> findAllFoldersInPath(File folder, EntityManager entityManager) throws IOException {
        //Find folders in path
        List<FolderMetadata> foldersDiscovered = new ArrayList<>();
        for(File file = new File(folder.getPath()); file != null; file = file.getParentFile()) {
            logger.info("preceding folder name: {}", file.getName());
            if (file.getName().equals(fileStorageProperties.getOnlyUserName())) break;
            FolderMetadata parentFolders = new FolderMetadata();
            parentFolders.setName(file.getName());
            foldersDiscovered.add(parentFolders);
        }
        if (foldersDiscovered.isEmpty()) throw new IOException("Invalid path");
        // reverse list
        Collections.reverse(foldersDiscovered); // consider LIFO either stack or deque
        return generateIdPaths(foldersDiscovered, entityManager);
    }

    private List<FolderMetadata> generateIdPaths(List<FolderMetadata> folderMetadataList, EntityManager entityManager) {
        StringBuilder idPath = new StringBuilder();
        //HOPEFULLY generate Id path starting from '0/'
        idPath.append(0).append("/");
        for (FolderMetadata folderMetadata : folderMetadataList) {
            //assign id from db
            entityManager.persist(folderMetadata);
            idPath.append(folderMetadata.getId());
            folderMetadata.setPath(idPath.toString());
            idPath.append("/");
//            if (!checkIfFolderExistsInDb(folderMetadata).isEmpty()) {
//                entityManager.remove(folderMetadata);
//                folderMetadataList.remove(folderMetadata);
//            }

        }
        return folderMetadataList;
    }

    //check if one of the nested folders are already in db and remove them from the list
    private List<FolderMetadata> checkIfFolderExistsInDb(FolderMetadata folder) {
        FolderMetadata dummyMetadata = new FolderMetadata();
        dummyMetadata.setName(folder.getName());
        dummyMetadata.setId(null);
        dummyMetadata.setCreatedAt(null);
        dummyMetadata.setPath(folder.getPath());
        logger.info("dummy path {}", dummyMetadata.getPath());
        Example<FolderMetadata> exampleFolderMetadata = Example.of(dummyMetadata);
        return sqLiteFolderRepository.findAll(exampleFolderMetadata); //empty = false || not empty = true
    }
}
