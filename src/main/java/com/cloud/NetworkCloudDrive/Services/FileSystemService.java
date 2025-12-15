package com.cloud.NetworkCloudDrive.Services;

import com.cloud.NetworkCloudDrive.Models.FileMetadata;
import com.cloud.NetworkCloudDrive.Models.FolderMetadata;
import com.cloud.NetworkCloudDrive.Properties.FileStorageProperties;
import com.cloud.NetworkCloudDrive.Repositories.FileSystemRepository;
import com.cloud.NetworkCloudDrive.Sessions.UserSession;
import com.cloud.NetworkCloudDrive.Utilities.FileUtility;
import com.cloud.NetworkCloudDrive.DAO.SQLiteDAO;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

//TODO Migrate from io to nio for thread safety
@Service
public class FileSystemService implements FileSystemRepository {
    private final FileStorageProperties fileStorageProperties;
    private final EntityManager entityManager;
    private final InformationService informationService;
    private final FileUtility fileUtility;
    private final UserSession userSession;
    private final SQLiteDAO sqLiteDAO;
    private final Logger logger = LoggerFactory.getLogger(FileSystemService.class);

    public FileSystemService(
            InformationService informationService,
            FileStorageProperties fileStorageProperties,
            EntityManager entityManager,
            UserSession userSession,
            FileUtility fileUtility,
            SQLiteDAO sqLiteDAO) {
        this.fileStorageProperties = fileStorageProperties;
        this.informationService = informationService;
        this.entityManager = entityManager;
        this.userSession = userSession;
        this.fileUtility = fileUtility;
        this.sqLiteDAO = sqLiteDAO;
    }

    @Override
    @Transactional
    public List<Object> getListOfMetadataFromPath(List<Path> filePaths, long currentFolderId)
            throws FileSystemException, SQLException {
        List<Object> folderAndFileMetadata = new ArrayList<>();
        List<Long> lastIdList = new ArrayList<>();
        for (Path path : filePaths) {
            File file = path.toFile();
            logger.info("file/folder in queue {}", file);
            if (file.isFile()) {
                folderAndFileMetadata.add(
                        sqLiteDAO.getFileMetadataByFolderIdNameAndUserId(currentFolderId, file.getName(), userSession.getId()));
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
        File checkExists = fileUtility.returnFileIfItExists(
                fileUtility.getFolderPath(file.getFolderId()) + File.separator + file.getName());
        //remove Folder
        if (!checkExists.delete())
            throw new FileSystemException(String.format("Failed to remove folder at path %s\n", checkExists.getPath()));
        sqLiteDAO.deleteFile(file);
        return checkExists.getPath();
    }

    @Override
    @Transactional
    public String removeFolder(FolderMetadata folder) throws IOException {
        String pathToRemove = fileUtility.resolvePathFromIdString(folder.getPath());
        logger.info("pathToRemove = {}", pathToRemove);
        //find folder
        File checkExists = fileUtility.returnFileIfItExists(pathToRemove);
        //remove Folder
        fileUtility.deleteFsTree(checkExists.toPath(), folder.getPath());
        if (!Files.deleteIfExists(checkExists.toPath()))
            throw new IOException("Failed to remove parent folder");
        sqLiteDAO.deleteFolder(folder);
        return checkExists.getPath();
    }

    @Override
    @Transactional
    public String updateFolderName(String newName, FolderMetadata folder) throws Exception {
        //find file
        File checkExists = fileUtility.returnFileIfItExists(fileUtility.resolvePathFromIdString(folder.getPath()));
        //rename file
        //check duplicate
        File renamedFolder = fileUtility.returnIfItsNotADuplicate(Path.of(checkExists.getPath()).getParent() + File.separator + newName);
        logger.info("estimated path: {}", renamedFolder.getPath());
        Path newUpdatedPath = Files.move(checkExists.toPath(), renamedFolder.toPath());
        if (Files.exists(newUpdatedPath)) {
            //set new name and path
            folder.setName(newName);
            //save
            sqLiteDAO.saveFolder(folder);
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
                        userSession.getName());
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
        file.setMimiType(newMimeType != null ? (newMimeType.equals(file.getMimiType())
                ? file.getMimiType() : newMimeType) : file.getMimiType());
        //save
        sqLiteDAO.saveFile(file);
        logger.info("Renamed file full path: {}", renamedFile.getPath());
        return renamedFile.getPath();
    }

    @Transactional
    @Override
    public String moveFile(FileMetadata targetFile, long folderId) throws Exception {
        String destinationFolder = fileStorageProperties.getBasePath() + fileUtility.getFolderPath(folderId);
        String currentFolder = fileUtility.getFolderPath(targetFile.getFolderId());
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
        sqLiteDAO.saveFile(targetFile);
        return checkDestinationExists.getPath();
    }

    //TODO OPTIMIZE
    /**
     * <p>Moves folder(s) to new location.</p>
     *
     * <p>How it works:</p>
     * Generates Folder ID path if the target is 0 and the source is at 0/1/4/2 then it will be 0/2
     * original source will be 0/1/4 if target is 0/5/9 then it will be 0/5/9/2 and contents will be 0/5/9/2/x
     * @param folder source folder metadata
     * @param destinationFolderId   destination folder id
     * @return  updated path
     * @throws Exception    throws FileSystemException and FileNotFoundException
     */
    //TODO destination ID 0 doesn't work reliably
    @Transactional
    @Override
    public String moveFolder(FolderMetadata folder, long destinationFolderId) throws Exception {
        String sourceFolderPath = fileUtility.resolvePathFromIdString(folder.getPath());
        File destinationFolderObj;
        FolderMetadata destinationFolderMetadata;
        String destinationIdPath;
        if (destinationFolderId == 0) {
            destinationFolderObj = new File(fileStorageProperties.getBasePath() + userSession.getName());
            destinationIdPath = "0";
        } else {
            destinationFolderMetadata = informationService.getFolderMetadata(destinationFolderId);
            destinationIdPath = destinationFolderMetadata.getPath();
            destinationFolderObj = fileUtility.returnFileIfItExists(
                    fileUtility.resolvePathFromIdString(destinationFolderMetadata.getPath()));
        }
        File sourceFolderObj = fileUtility.returnFileIfItExists(sourceFolderPath);
        // get children folders to update
        List<FolderMetadata> foldersToMove = sqLiteDAO.getChildrenFoldersInDirectory(folder.getPath());
        String formerIdPath = destinationIdPath;
        String backupPath = "";
        for (int i = 0; i < foldersToMove.size(); i++) {
            if (i != 0) {
                foldersToMove.get(i).setPath(backupPath + "/" + foldersToMove.get(i).getId());
                continue;
            }
            foldersToMove.get(i).setPath(formerIdPath + "/" + foldersToMove.get(i).getId());
            backupPath = foldersToMove.get(i).getPath();
        }
        // perform filesystem move
        String updatedPath = destinationFolderObj.getPath() + File.separator + sourceFolderObj.getName();
        Path moveFolderAction = Files.move(sourceFolderObj.toPath(), Path.of(updatedPath));
        if (!Files.exists(moveFolderAction))
            throw new FileSystemException(String.format("Failed to move the folder from %s to %s", sourceFolderPath, updatedPath));
        //save
        sqLiteDAO.saveAllFolders(foldersToMove);
        return updatedPath;
    }

    @Override
    @Transactional
    public FolderMetadata createFolder(String folderName, long folderId) throws Exception {
        //interesting???!!
        File userFolder = fileUtility.returnUserFolder();
        String rootPath = fileStorageProperties.getBasePath();
        String idPath;
        String precedingPath;
        if (folderId != 0) {
            FolderMetadata precedingFolderMetadata = informationService.getFolderMetadata(folderId);
            precedingPath = fileUtility.resolvePathFromIdString(precedingFolderMetadata.getPath());
            idPath = precedingFolderMetadata.getPath();
        } else {
            precedingPath = userFolder.getName();
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
        createdFolder.setPath(idPath + "/" + createdFolder.getId());
        createdFolder.setUserid(userSession.getId());
        createdFolder.setName(folderName);
        return sqLiteDAO.saveFolder(createdFolder);
    }
}
