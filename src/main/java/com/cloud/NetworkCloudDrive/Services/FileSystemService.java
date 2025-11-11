package com.cloud.NetworkCloudDrive.Services;

import com.cloud.NetworkCloudDrive.Models.FileMetadata;
import com.cloud.NetworkCloudDrive.Models.FolderMetadata;
import com.cloud.NetworkCloudDrive.Properties.FileStorageProperties;
import com.cloud.NetworkCloudDrive.Repositories.FileSystemRepository;
import com.cloud.NetworkCloudDrive.Repositories.SQLiteFileRepository;
import com.cloud.NetworkCloudDrive.Repositories.SQLiteFolderRepository;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class FileSystemService implements FileSystemRepository {
    private final SQLiteFileRepository sqLiteFileRepository;
    private final SQLiteFolderRepository sqLiteFolderRepository;
    private final FileStorageProperties fileStorageProperties;
    private final EntityManager entityManager;
    private final Logger logger = LoggerFactory.getLogger(FileSystemService.class);

    public FileSystemService(
            SQLiteFileRepository sqLiteFileRepository,
            SQLiteFolderRepository sqLiteFolderRepository,
            FileStorageProperties fileStorageProperties,
            EntityManager entityManager
    ) {
        this.sqLiteFileRepository = sqLiteFileRepository;
        this.fileStorageProperties = fileStorageProperties;
        this.sqLiteFolderRepository = sqLiteFolderRepository;
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public FileMetadata getFileMetadata(long id) throws Exception {
        Optional<FileMetadata> checkFile = sqLiteFileRepository.findById(id);
        if (checkFile.isEmpty()) throw new FileNotFoundException("File does not exist.");
        FileMetadata retrievedFile = checkFile.get();
        logger.info("File requested path: {}", retrievedFile.getOwner());
        File fileCheck = new File(fileStorageProperties.getBasePath() + File.separator + retrievedFile.getOwner());
        if (!fileCheck.exists())
            throw new IOException(String.format("File could not be found on the computer! File path: %s", retrievedFile.getOwner()));
        retrievedFile.setSize(fileCheck.length()); //bytes
        logger.info("pass");
        return retrievedFile;
    }

    @Override
    public FolderMetadata getFolderMetadata(long fileId) throws Exception {
        Optional<FolderMetadata> folderMetadata = sqLiteFolderRepository.findById(fileId);
        if (folderMetadata.isEmpty()) throw new FileNotFoundException();
        FolderMetadata folder = folderMetadata.get();
        File getFolder = new File(fileStorageProperties.getBasePath() + File.separator + folder.getPath());
        if (!getFolder.exists())
            throw new FileAlreadyExistsException(String.format("Folder with same name at path %s already exists", folder.getPath()));
        return folder;
    }

    @Override
    @Transactional
    public void removeFile(FileMetadata file) throws Exception {
        //find folder
        File checkExists = new File(fileStorageProperties.getBasePath() + file.getOwner());
        if (!checkExists.exists()) throw new FileNotFoundException();
        //remove Folder
        if (!checkExists.delete())
            throw new FileSystemException(String.format("Failed to remove folder at path %s\n", file.getOwner()));
        sqLiteFileRepository.delete(file);
    }

    @Override
    @Transactional
    public void removeFolder(FolderMetadata folder) throws Exception {
        //find folder
        File checkExists = new File(fileStorageProperties.getBasePath() + folder.getPath());
        if (!checkExists.exists()) throw new FileNotFoundException();
        //remove Folder
        if (!checkExists.delete())
            throw new FileSystemException(String.format("Failed to remove folder at path %s\n", folder.getPath()));
        sqLiteFolderRepository.delete(folder);
    }

    @Override
    @Transactional
    public void updateFolderName(String newName, FolderMetadata folder) throws Exception {
        //find file
        File checkExists = new File(fileStorageProperties.getBasePath() + folder.getPath());
        if (!checkExists.exists()) throw new FileNotFoundException("folder not found");
        //rename file
        logger.info("DEBUG new name {}",Path.of(folder.getPath()).getParent() + File.separator + newName);
        File renamedFile = new File(fileStorageProperties.getBasePath() + Path.of(folder.getPath()).getParent() + File.separator + newName);
        //check duplicate
        if (renamedFile.exists()) throw new FileAlreadyExistsException(String.format("folder with name %s already exists", renamedFile.getName()));
        if (checkExists.renameTo(renamedFile)) {
            //set new name and path
            folder.setName(newName);
            folder.setPath(removeBeginningOfPath(renamedFile.getPath()));
            //save
            sqLiteFolderRepository.save(folder);
            logger.info("Renamed folder full path: {}", renamedFile.getPath());
        } else {
            throw new FileSystemException(String.format("Failed to rename the folder to %s", renamedFile.getName()));
        }
    }

    @Override
    @Transactional
    public void updateFileName(String newName, FileMetadata file) throws Exception {
        //find file
        File checkExists = new File(fileStorageProperties.getBasePath() + file.getOwner());
        if (!checkExists.exists()) throw new FileNotFoundException("File not found");
        //save extension
        String oldExtension = getFileExtension(file.getName());
        //rename file
        logger.info("new file name {}",Path.of(file.getOwner()).getParent() + File.separator + newName + getFileExtension(file.getName()));
        File renamedFile = new File(fileStorageProperties.getBasePath() + Path.of(file.getOwner()).getParent() + File.separator + newName + getFileExtension(file.getName()));
        //check duplicate
        if (renamedFile.exists()) throw new FileAlreadyExistsException(String.format("File with name %s already exists", renamedFile.getName()));
        String newMimeType = getMimeTypeFromExtension(renamedFile.toPath()); /* <- get new mimetype of file */
        if (checkExists.renameTo(renamedFile)) {
            //set new name and path
            file.setName(newName + oldExtension);
            file.setMimiType(newMimeType.equals(file.getMimiType()) ? file.getMimiType() : newMimeType);
            file.setOwner(removeBeginningOfPath(renamedFile.getPath()));
            //save
            sqLiteFileRepository.save(file);
            logger.info("Renamed file full path: {}", renamedFile.getPath());
        } else {
            throw new FileSystemException(String.format("Failed to rename the file to %s", renamedFile.getName()));
        }
    }

    private String getMimeTypeFromExtension(Path filePath) throws IOException {
        return Files.probeContentType(filePath);
    }

    @Transactional
    @Override
    public void moveFile(FileMetadata file, String newPath) throws Exception {
        newPath = fileStorageProperties.getBasePath() + newPath + File.separator + file.getName();
        logger.info("new file path = {}", newPath);
        //find file
        File checkExists = new File(fileStorageProperties.getBasePath() + file.getOwner());
        if (!checkExists.exists()) throw new FileNotFoundException();
        if (!checkExists.renameTo(new File(newPath))) throw new FileSystemException(file.getName());
        //set new name and path
        file.setOwner(removeBeginningOfPath(newPath));
        //save
        sqLiteFileRepository.save(file);
    }

    @Transactional
    @Override
    public void moveFolder(FolderMetadata folder, String newPath) throws Exception {
        newPath = fileStorageProperties.getBasePath() + newPath + File.separator + folder.getName();
        logger.info("new folder path = {}", newPath);
        //find file
        File checkExists = new File(fileStorageProperties.getBasePath() + folder.getPath());
        if (!checkExists.exists()) throw new FileNotFoundException();
        if (!checkExists.renameTo(new File(newPath))) throw new FileSystemException(folder.getName());
        //set new name and path
        folder.setPath(removeBeginningOfPath(newPath));
        //save
        sqLiteFolderRepository.save(folder);
    }

    @Override
    @Transactional
    public List<FolderMetadata> createFolder(String pathWithName) throws Exception {
        String rootPath = fileStorageProperties.getBasePath();
        File folder = new File(
                rootPath
                        + fileStorageProperties.getOnlyUserName() + //get user folder when auth is implemented
                        File.separator +
                        pathWithName
        );
        if (!folder.mkdirs()) throw new IOException("Cannot create directory, path: " + pathWithName);
        return sqLiteFolderRepository.saveAll(findAllFoldersInPath(folder)); // reverse order
    }

    private List<FolderMetadata> findAllFoldersInPath(File folder) throws IOException {
        //Find folders in path
        List<FolderMetadata> foldersDiscovered = new ArrayList<>();
        for(File file = new File(folder.getPath()); file != null; file = file.getParentFile()) {
            logger.info("preceding folder name: {}", file.getName());
            if (file.getName().equals(fileStorageProperties.getOnlyUserName())) break;
            if (checkIfFolderExistsInDb(file)) { // overhead
                logger.warn("Folder with name {} and path {}, already exists in database", file.getName(), file.getPath());
                continue;
            }
            FolderMetadata parentFolders = new FolderMetadata();
            parentFolders.setName(file.getName());
            parentFolders.setPath(removeBeginningOfPath(file.getPath()));
            entityManager.persist(parentFolders);
            logger.info("folderid set: {}", (parentFolders.getId() != null ? parentFolders.getId() : "null id"));
            foldersDiscovered.add(parentFolders);
        }
        if (foldersDiscovered.isEmpty()) throw new IOException("Invalid path");
        Collections.reverse(foldersDiscovered);
        return foldersDiscovered;
    }

    private String removeBeginningOfPath(String path) {
        return path.replaceAll(fileStorageProperties.getBasePath(),"");
    }

    //check if one of the nested folders are already in db and remove them from the list
    private boolean checkIfFolderExistsInDb(File folder) {
        FolderMetadata dummyMetadata = new FolderMetadata();
        dummyMetadata.setName(folder.getName());
        dummyMetadata.setId(null);
        dummyMetadata.setCreatedAt(null);
        dummyMetadata.setPath(removeBeginningOfPath(folder.getPath()));
        Example<FolderMetadata> folderMetadataExample = Example.of(dummyMetadata);
        List<FolderMetadata> results = sqLiteFolderRepository.findAll(folderMetadataExample);
        return !results.isEmpty(); //empty = false || not empty = true
    }

    private String getFileExtension(String fileName) {
        StringBuilder ext = new StringBuilder();
        boolean afterDot = false;
        for (int i = 0; i < fileName.length(); i++) {
            if (fileName.charAt(i) == '.') afterDot = !afterDot;
            ext.append(afterDot ? fileName.charAt(i) : "");
        }
        return ext.toString();
    }

    @Override
    public String resolvePathOfIdString(String encodedString) {
        String[] splitLine = encodedString.split("/");
        StringBuilder fullPath = new StringBuilder();
        List<Long> idList = new ArrayList<>();
        for (String idAsString : splitLine) {
            idList.add(Long.parseLong(idAsString));
        }
        List<FolderMetadata> folderMetadataListById = sqLiteFolderRepository.findAllById(idList);
        for (FolderMetadata folderMetadata : folderMetadataListById) {
            fullPath.append(folderMetadata.getName()).append(File.separator);
        }
        fullPath.setLength(fullPath.length()-1); //remove last '/'
        return fullPath.toString();
    }
}
