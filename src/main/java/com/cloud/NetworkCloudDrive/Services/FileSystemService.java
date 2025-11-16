package com.cloud.NetworkCloudDrive.Services;

import com.cloud.NetworkCloudDrive.Models.FileMetadata;
import com.cloud.NetworkCloudDrive.Models.FolderMetadata;
import com.cloud.NetworkCloudDrive.Properties.FileStorageProperties;
import com.cloud.NetworkCloudDrive.Repositories.FileSystemRepository;
import com.cloud.NetworkCloudDrive.Repositories.SQLiteFileRepository;
import com.cloud.NetworkCloudDrive.Repositories.SQLiteFolderRepository;
import com.cloud.NetworkCloudDrive.Utilities.FileUtility;
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

@Service
public class FileSystemService implements FileSystemRepository {
    private final SQLiteFileRepository sqLiteFileRepository;
    private final SQLiteFolderRepository sqLiteFolderRepository;
    private final FileStorageProperties fileStorageProperties;
    private final EntityManager entityManager;
    private final InformationService informationService;
    private final FileUtility fileUtility;
    private final Logger logger = LoggerFactory.getLogger(FileSystemService.class);

    public FileSystemService(
            SQLiteFileRepository sqLiteFileRepository,
            SQLiteFolderRepository sqLiteFolderRepository,
            InformationService informationService,
            FileStorageProperties fileStorageProperties,
            EntityManager entityManager,
            FileUtility fileUtility) {
        this.sqLiteFileRepository = sqLiteFileRepository;
        this.fileStorageProperties = fileStorageProperties;
        this.sqLiteFolderRepository = sqLiteFolderRepository;
        this.informationService = informationService;
        this.entityManager = entityManager;
        this.fileUtility = fileUtility;
    }

    @Override
    @Transactional
    public List<Object> getListOfMetadataFromPath(List<Path> filePaths, long currentFolderId)
            throws FileSystemException, FileNotFoundException {
        List<Object> folderAndFileMetadata = new ArrayList<>();
        List<Long> lastIdList = new ArrayList<>();
        for (Path path : filePaths) {
            File file = path.toFile();
            if (file.isFile()) {
                folderAndFileMetadata.add(informationService.getFileMetadataByFolderIdAndName(currentFolderId, file.getName(), 0));
                continue;
            }
            FolderMetadata foundFolderMetadata =
                    informationService.getFolderMetadataByFolderIdAndName(currentFolderId, file.getName(), lastIdList);
            folderAndFileMetadata.add(foundFolderMetadata);
            lastIdList.add(foundFolderMetadata.getId());
        }
        return folderAndFileMetadata;
    }

    //TODO update
    @Override
    @Transactional
    public void removeFile(FileMetadata file) throws Exception {
        //find folder
        File checkExists = new File(fileStorageProperties.getBasePath() +
                fileUtility.resolvePathFromIdString(informationService.getFolderMetadata(file.getFolderId()).getPath()) +
                File.separator + file.getName());
        if (!checkExists.exists()) throw new FileNotFoundException();
        //remove Folder
        if (!checkExists.delete())
            throw new FileSystemException(String.format("Failed to remove folder at path %s\n", checkExists.getPath()));
        sqLiteFileRepository.delete(file);
    }

    //TODO update
    @Override
    @Transactional
    public void removeFolder(FolderMetadata folder) throws Exception {
        String pathToRemove = fileUtility.resolvePathFromIdString(folder.getPath());
        //find folder
        File checkExists = new File(fileStorageProperties.getBasePath() + pathToRemove);
        if (!checkExists.exists()) throw new FileNotFoundException(String.format("Folder does not exist at path %s", pathToRemove));
        //remove Folder
        Files.delete(checkExists.toPath());
//        if (!checkExists.delete())
//            throw new FileSystemException(String.format("Failed to remove folder at path %s", pathToRemove));
        sqLiteFolderRepository.delete(folder);
    }

    //TODO update
    @Override
    @Transactional
    public void updateFolderName(String newName, FolderMetadata folder) throws Exception {
        //find file
        File checkExists = new File(fileStorageProperties.getBasePath() + fileUtility.resolvePathFromIdString(folder.getPath()));
        if (!checkExists.exists()) throw new FileNotFoundException("folder not found");
        //rename file
        File renamedFile = new File(Path.of(checkExists.getPath()).getParent() + File.separator + newName);
        logger.info("estimated path: {}", renamedFile.getPath());
        //check duplicate
        if (renamedFile.exists())
            throw new FileAlreadyExistsException(String.format("folder with name %s already exists", renamedFile.getName()));
        if (checkExists.renameTo(renamedFile)) {
            //set new name and path
            folder.setName(newName);
            //save
            sqLiteFolderRepository.save(folder);
            logger.info("Renamed folder full path: {}", renamedFile.getPath());
        } else {
            throw new FileSystemException(String.format("Failed to rename the folder to %s", renamedFile.getName()));
        }
    }

    //TODO update
    @Override
    @Transactional
    public void updateFileName(String newName, FileMetadata file) throws Exception {
        //find file
        File checkExists = new File(fileStorageProperties.getBasePath() + file.getName());
        if (!checkExists.exists())
            throw new FileNotFoundException("File not found");
        //save extension
        String oldExtension = fileUtility.getFileExtension(file.getName());
        //rename file
        File renamedFile = new File(
                fileStorageProperties.getBasePath() +
                        Path.of(
                                file.getName()).getParent() +
                        File.separator +
                        newName +
                        fileUtility.getFileExtension(file.getName()));
        //check duplicate
        if (renamedFile.exists())
            throw new FileAlreadyExistsException(String.format("File with name %s already exists", renamedFile.getName()));
        String newMimeType = fileUtility.getMimeTypeFromExtension(renamedFile.toPath()); /* <- get new mimetype of file */
        if (checkExists.renameTo(renamedFile)) {
            //set new name and path
            file.setName(newName + oldExtension);
            file.setMimiType(newMimeType.equals(file.getMimiType()) ? file.getMimiType() : newMimeType);
            //save
            sqLiteFileRepository.save(file);
            logger.info("Renamed file full path: {}", renamedFile.getPath());
        } else {
            throw new FileSystemException(String.format("Failed to rename the file to %s", renamedFile.getName()));
        }
    }

    @Transactional
    @Override
    public void moveFile(FileMetadata targetFile, String destinationFolder, String currentFolder) throws Exception {
        String newPath = fileStorageProperties.getBasePath() + destinationFolder + File.separator + targetFile.getName();
        logger.info("new file path = {}", newPath);
        //find file
        String oldPath = fileStorageProperties.getBasePath()
                + currentFolder +
                File.separator +
                targetFile.getName();
        logger.info("old path service {}", oldPath);
        File checkExists = new File(oldPath);
        if (!checkExists.exists()) throw new FileNotFoundException(String.format(
                "File does not exist with name %s at path %s",
                targetFile.getName(),
                oldPath));
        if (!checkExists.renameTo(new File(newPath)))
            throw new FileSystemException(String.format(
                    "Failed to move file with name %s from %s to %s",targetFile.getName(), oldPath, newPath ));
        //set new name and path
        targetFile.setFolderId(targetFile.getFolderId());
        //save
        sqLiteFileRepository.save(targetFile);
    }

    //TODO Update
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
//        folder.setPath(removeBeginningOfPath(newPath));
        //save
        sqLiteFolderRepository.save(folder);
    }

    @Override
    @Transactional
    public FolderMetadata createFolder(String folderName, long folderId) throws Exception {
        if (!fileUtility.checkAndMakeDirectories(fileStorageProperties.getFullPath()))
            throw new FileSystemException("Failed to create root directory");
        String rootPath = fileStorageProperties.getBasePath();
        String idPath;
        String precedingPath;
        if (folderId != 0) {
            FolderMetadata precedingFolderMetadata = informationService.getFolderMetadata(folderId);
            precedingPath = fileUtility.resolvePathFromIdString(precedingFolderMetadata.getPath());
            idPath = precedingFolderMetadata.getPath();
        } else {
            precedingPath = fileStorageProperties.getOnlyUserName();
            idPath = "0";
        }
        File folder = new File(rootPath + precedingPath + File.separator + folderName);
        if (folder.exists())
            throw new FileAlreadyExistsException(
                    String.format("Folder with name %s already exists at this path %s.", folderName, folder.getPath()));
        if (!folder.mkdir())
            throw new IOException(
                    String.format("Cannot create directory, with name %s. Make sure you are creating a single folder.", folderName));
        FolderMetadata createdFolder = new FolderMetadata();
        entityManager.persist(createdFolder);
        createdFolder.setPath(idPath + File.separator + createdFolder.getId());
        createdFolder.setUserid(0L);
        createdFolder.setName(folderName);
        return sqLiteFolderRepository.save(createdFolder); // reverse order
    }
}
