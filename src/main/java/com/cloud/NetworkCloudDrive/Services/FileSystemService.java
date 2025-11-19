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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

//TODO Migrate from io to nio for thread safety
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
    public List<Object> getListOfMetadataFromPath(List<Path> filePaths, long currentFolderId) throws FileSystemException, FileNotFoundException {
        List<Object> folderAndFileMetadata = new ArrayList<>();
        List<Long> lastIdList = new ArrayList<>();
        for (Path path : filePaths) {
            File file = path.toFile();
            logger.info("file/folder in queue {}", file);
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

    @Override
    @Transactional
    public String removeFile(FileMetadata file) throws Exception {
        //find folder
        File checkExists = new File(fileStorageProperties.getBasePath() +
                (file.getFolderId() != 0 ?
                        fileUtility.resolvePathFromIdString(informationService.getFolderMetadata(file.getFolderId()).getPath())
                        :
                        fileStorageProperties.getOnlyUserName()) +
                File.separator + file.getName());
        if (!checkExists.exists())
            throw new FileNotFoundException(String.format("File does not exist at path %s", checkExists.getPath()));
        //remove Folder
        if (!checkExists.delete())
            throw new FileSystemException(String.format("Failed to remove folder at path %s\n", checkExists.getPath()));
        sqLiteFileRepository.delete(file);
        return checkExists.getPath();
    }

    //TODO implement walk fs tree to recursively remove contents of a folder from system and database
    @Override
    @Transactional
    public String removeFolder(FolderMetadata folder) throws IOException {
        String pathToRemove = fileUtility.resolvePathFromIdString(folder.getPath());
        //find folder
        File checkExists = new File(fileStorageProperties.getBasePath() + pathToRemove);
        if (!Files.deleteIfExists(checkExists.toPath()))
            throw new FileSystemException(String.format("Failed to delete folder. Does folder exist at path %s?", pathToRemove));
        //remove Folder
        Files.delete(checkExists.toPath());
        sqLiteFolderRepository.delete(folder);
        return checkExists.getPath();
    }

    @Override
    @Transactional
    public String updateFolderName(String newName, FolderMetadata folder) throws Exception {
        //find file
        File checkExists = new File(fileStorageProperties.getBasePath() + fileUtility.resolvePathFromIdString(folder.getPath()));
        if (!Files.exists(checkExists.toPath()))
            throw new FileNotFoundException("folder not found at path " + checkExists.getPath());
        //rename file
        File renamedFolder = new File(Path.of(checkExists.getPath()).getParent() + File.separator + newName);
        logger.info("estimated path: {}", renamedFolder.getPath());
        //check duplicate
        if (Files.exists(renamedFolder.toPath()))
            throw new FileAlreadyExistsException(String.format("folder with name %s already exists", renamedFolder.getName()));
        Path newUpdatedPath = Files.move(checkExists.toPath(), renamedFolder.toPath());
        if (Files.exists(newUpdatedPath)) {
            //set new name and path
            folder.setName(newName);
            //save
            sqLiteFolderRepository.save(folder);
            logger.info("Renamed folder full path: {}", renamedFolder.getPath());
        } else {
            throw new FileSystemException(String.format("Failed to rename the folder to %s", renamedFolder.getName()));
        }
        return renamedFolder.getPath();
    }

    @Override
    @Transactional
    public String updateFileName(String newName, FileMetadata file) throws Exception {
        String folderPath = fileStorageProperties.getBasePath() +
                (file.getFolderId() != 0 ?
                        fileUtility.resolvePathFromIdString(informationService.getFolderMetadata(file.getFolderId()).getPath())
                        :
                        fileStorageProperties.getOnlyUserName());
        //find file
        File checkExists = new File(folderPath + File.separator + file.getName());
        if (!Files.exists(checkExists.toPath(), LinkOption.NOFOLLOW_LINKS))
            throw new FileNotFoundException("File not found");
        //save extension
        String oldExtension = fileUtility.getFileExtension(file.getName());
        //rename file
        File renamedFile = new File(folderPath + File.separator + newName + fileUtility.getFileExtension(file.getName()));
        //check duplicate
        if (Files.exists(renamedFile.toPath()))
            throw new FileAlreadyExistsException(String.format("File with name %s already exists", renamedFile.getName()));
        // Perform movement
        Path newUpdatedPath = Files.move(checkExists.toPath(), renamedFile.toPath());
        if (!Files.exists(newUpdatedPath))
            throw new FileSystemException(String.format("Failed to rename the file to %s", renamedFile.getName()));
        // get ready for transaction
        // mimetype has bug in the library (cant detect types such as yaml)
        String newMimeType = fileUtility.getMimeTypeFromExtension(newUpdatedPath); /* <- get new mimetype of file */
        //set new name and path
        file.setName(newName + oldExtension);
        file.setMimiType(newMimeType.equals(file.getMimiType()) ? file.getMimiType() : newMimeType);
        //save
        sqLiteFileRepository.save(file);
        logger.info("Renamed file full path: {}", renamedFile.getPath());
        return renamedFile.getPath();
    }

    @Transactional
    @Override
    public String moveFile(FileMetadata targetFile, long folderId) throws Exception {
        String destinationFolder = fileStorageProperties.getBasePath() + informationService.getFolderPathAsString(folderId);
        String currentFolder = informationService.getFolderPathAsString(targetFile.getFolderId());
        String newPath = destinationFolder + File.separator + targetFile.getName();
        logger.info("new file path = {}", newPath);
        //find file
        String oldPath = fileStorageProperties.getBasePath() + currentFolder + File.separator + targetFile.getName();
        logger.info("old path service {}", oldPath);
        File checkExists = new File(oldPath);
        File checkDestinationExists = new File(destinationFolder);
        if (!Files.exists(checkExists.toPath(), LinkOption.NOFOLLOW_LINKS))
            throw new FileNotFoundException(String.format("File does not exist with name %s at path %s", targetFile.getName(), oldPath));
        if (!Files.exists(checkDestinationExists.toPath()))
            throw new FileNotFoundException(String.format("Destination folder does not exist at path %s", checkDestinationExists.getPath()));
        File updatedPath = new File(newPath);
        Path movedFile = Files.move(checkExists.toPath(), updatedPath.toPath());
        if (!Files.exists(movedFile))
            throw new FileSystemException(
                    String.format("Failed to move file with name %s from %s to %s", targetFile.getName(), oldPath, newPath));
        //set new name and path
        targetFile.setFolderId(folderId);
        //save
        sqLiteFileRepository.save(targetFile);
        return checkDestinationExists.getPath();
    }

    //TODO OPTIMIZE
    @Transactional
    @Override
    public String moveFolder(FolderMetadata folder, long destinationFolderId) throws Exception {
        String sourceFolderPath = fileStorageProperties.getBasePath() + fileUtility.resolvePathFromIdString(folder.getPath());
        FolderMetadata destinationFolderMetadata = informationService.getFolderMetadata(destinationFolderId);
        String destinationFolderPath =
                fileStorageProperties.getBasePath() + fileUtility.resolvePathFromIdString(destinationFolderMetadata.getPath());
        logger.info("destination folder path = {}", destinationFolderPath);
        File sourceFolderObj = new File(sourceFolderPath);
        File destinationFolderObj = new File(destinationFolderPath);
        // validate
        if (!Files.exists(destinationFolderObj.toPath()))
            throw new FileNotFoundException("Destination folder not found at path " + destinationFolderObj.getPath() + "!");
        if (!Files.exists(sourceFolderObj.toPath()))
            throw new FileNotFoundException("Source folder not found at path " + sourceFolderObj.getPath() + "!");
        logger.info("concat id path {}", folder.getPath());
        // get children folders to update
        List<FolderMetadata> foldersToMove = fileUtility.getChildrenFoldersInDirectory(folder.getPath());
        // generate folder id path
        // if the target is 0 and the source is at 0/1/4/2
        // then it will be 0/2 original source will be 0/1/4
        String formerIdPath = destinationFolderMetadata.getPath();
        String backupPath = "";
        for (int i = 0; i < foldersToMove.size(); i++) {
            if (i != 0) {
                foldersToMove.get(i).setPath(backupPath + "/" + foldersToMove.get(i).getId());
                logger.info(
                        "beginning id {} name {} path {}",
                        foldersToMove.get(i).getId(), foldersToMove.get(i).getName(), foldersToMove.get(i).getPath());
                continue;
            }
            foldersToMove.get(i).setPath(formerIdPath + "/" + foldersToMove.get(i).getId());
            backupPath = foldersToMove.get(i).getPath();
            logger.info("id {} name {} path {}", foldersToMove.get(i).getId(), foldersToMove.get(i).getName(), foldersToMove.get(i).getPath());
        }

        // perform filesystem move
        String updatedPath = destinationFolderPath + File.separator + sourceFolderObj.getName();
        Path moveFolderAction = Files.move(sourceFolderObj.toPath(), Path.of(updatedPath));
        if (!Files.exists(moveFolderAction))
            throw new FileSystemException(String.format("Failed to move the folder from %s to %s", sourceFolderPath, updatedPath));
        //save
        sqLiteFolderRepository.saveAll(foldersToMove);
        return updatedPath;
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
        createdFolder.setUserid(0L); //placeholder
        createdFolder.setName(folderName);
        return sqLiteFolderRepository.save(createdFolder); // reverse order
    }
}
