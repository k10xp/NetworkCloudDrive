package com.cloud.NetworkCloudDrive.Services;

import com.cloud.NetworkCloudDrive.Models.FileMetadata;
import com.cloud.NetworkCloudDrive.Models.FolderMetadata;
import com.cloud.NetworkCloudDrive.Properties.FileStorageProperties;
import com.cloud.NetworkCloudDrive.Repositories.FileSystemRepository;
import com.cloud.NetworkCloudDrive.Repositories.SQLiteFileRepository;
import com.cloud.NetworkCloudDrive.Repositories.SQLiteFolderRepository;
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
import java.util.List;
import java.util.Optional;

@Service
public class FileSystemService implements FileSystemRepository {
    private final SQLiteFileRepository sqLiteFileRepository;
    private final SQLiteFolderRepository sqLiteFolderRepository;
    private final FileStorageProperties fileStorageProperties;
    private final Logger logger = LoggerFactory.getLogger(FileSystemService.class);

    public FileSystemService(
            SQLiteFileRepository sqLiteFileRepository,
            SQLiteFolderRepository sqLiteFolderRepository,
            FileStorageProperties fileStorageProperties
    ) {
        this.sqLiteFileRepository = sqLiteFileRepository;
        this.fileStorageProperties = fileStorageProperties;
        this.sqLiteFolderRepository = sqLiteFolderRepository;
    }

    @Override
    @Transactional
    public FileMetadata GetFileMetadata(long id) throws Exception {
        Optional<FileMetadata> checkFile = sqLiteFileRepository.findById(id);
        if (checkFile.isEmpty()) throw new FileNotFoundException("File does not exist.");
        FileMetadata retrievedFile = checkFile.get();
        logger.info("File requested path: {}", retrievedFile.getPath());
        File fileCheck = new File(fileStorageProperties.getBasePath() + File.separator + retrievedFile.getPath());
        if (!fileCheck.exists())
            throw new IOException(String.format("File could not be found on the computer! File path: %s", retrievedFile.getPath()));
        retrievedFile.setSize(fileCheck.length()); //bytes
        logger.info("pass");
        return retrievedFile;
    }

    @Override
    public FolderMetadata getFolderMetadata(long fileId) throws Exception {
        Optional<FolderMetadata> folderMetadata = sqLiteFolderRepository.findById(fileId);
        if (folderMetadata.isEmpty()) throw new FileNotFoundException();
        FolderMetadata folder = folderMetadata.get();
        File getFolder = new File( fileStorageProperties.getBasePath() + File.separator + folder.getPath());
        if (!getFolder.exists()) {
            throw new FileAlreadyExistsException(String.format("Folder with same name at path %s already exists", folder.getPath()));
        }
        return folder;
    }

    @Override
    @Transactional
    public void RemoveFolder(FolderMetadata folder) throws Exception {
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
    public void UpdateFileName(String newName, FileMetadata file) throws Exception {
        //find file
        File checkExists = new File(fileStorageProperties.getBasePath() + file.getPath());
        if (!checkExists.exists()) throw new FileNotFoundException("File not found");
        //save extension
        String oldExtension = getFileExtension(file.getName());
        //rename file
        logger.info("DEBUG new name {}",Path.of(file.getPath()).getParent() + File.separator + newName + getFileExtension(file.getName()));
        File renamedFile = new File(fileStorageProperties.getBasePath() + Path.of(file.getPath()).getParent() + File.separator + newName + getFileExtension(file.getName()));
        //check duplicate
        if (renamedFile.exists()) throw new FileAlreadyExistsException(String.format("File with name %s already exists", renamedFile.getName()));
        String newMimeType = getMimeTypeFromExtension(renamedFile.toPath()); /* <- get new mimetype of file */
        if (checkExists.renameTo(renamedFile)) {
            //set new name and path
            file.setName(newName + oldExtension);
            file.setMimiType(newMimeType.equals(file.getMimiType()) ? file.getMimiType() : newMimeType);
            file.setPath(removeBeginningOfPath(renamedFile.getPath()));
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
    public void MoveFile(FileMetadata file, String newPath) throws Exception {
        newPath = fileStorageProperties.getBasePath() + newPath + File.separator + file.getName();
        logger.info("new path = {}", newPath);
        //find file
        File checkExists = new File(fileStorageProperties.getBasePath() + file.getPath());
        if (!checkExists.exists()) throw new FileNotFoundException();
        if (!checkExists.renameTo(new File(newPath))) throw new FileSystemException(file.getName());
        //set new name and path
        file.setPath(newPath);
        //save
        sqLiteFileRepository.save(file);
    }

    @Override
    @Transactional
    public void CreateFolder(String pathWithName) throws Exception {
        String rootPath = fileStorageProperties.getBasePath();
        File folder = new File(
                rootPath
                        + fileStorageProperties.getOnlyUserName() + //get user folder when auth is implemented
                        File.separator +
                        pathWithName
        );
        if (!folder.mkdirs()) throw new IOException("Cannot create directory, path: " + pathWithName);

        //Find folders in path
        List<FolderMetadata> foldersDiscovered = new ArrayList<>();
        for(File file = new File(folder.getPath()); file != null; file = file.getParentFile()) {
            logger.info("preceding folder name: {}", file.getName());
            if (file.getName().equals(fileStorageProperties.getOnlyUserName())) break;
            FolderMetadata parentFolders = new FolderMetadata();
            parentFolders.setName(file.getName());
            parentFolders.setPath(removeBeginningOfPath(file.getPath()));
            foldersDiscovered.add(parentFolders);
        }

        if (foldersDiscovered.isEmpty()) throw new IOException("Invalid path");
        sqLiteFolderRepository.saveAll(foldersDiscovered);
    }

    private String removeBeginningOfPath(String path) {
        return path.replaceAll(fileStorageProperties.getBasePath(),"");
    }

    //check if one of the nested folders are already in db and remove them from the list
    private List<FolderMetadata> checkIfParentFolderExistsInDb(File folder) {
        File parentFolder = folder.getParentFile();
        logger.info("parent folder: path {} name {}", parentFolder.getPath(), parentFolder.getName());
        FolderMetadata dummyMetadata = new FolderMetadata();
        dummyMetadata.setName(parentFolder.getName());
        dummyMetadata.setId(null);
        dummyMetadata.setCreatedAt(null);
        dummyMetadata.setPath(null);
        Example<FolderMetadata> folderMetadataExample = Example.of(dummyMetadata);
        List<FolderMetadata> results = sqLiteFolderRepository.findAll(folderMetadataExample);
        logger.info("Results size: {}", results.size());
        return results;
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
}
