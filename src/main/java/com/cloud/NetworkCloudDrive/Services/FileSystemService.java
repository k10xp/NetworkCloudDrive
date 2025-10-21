package com.cloud.NetworkCloudDrive.Services;

import com.cloud.NetworkCloudDrive.Models.FileMetadata;
import com.cloud.NetworkCloudDrive.Models.FolderMetadata;
import com.cloud.NetworkCloudDrive.Properties.FileStorageProperties;
import com.cloud.NetworkCloudDrive.Repositories.FileSystemRepository;
import com.cloud.NetworkCloudDrive.Repositories.SQLiteFileRepository;
import com.cloud.NetworkCloudDrive.Repositories.SQLiteFolderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
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
    public FileMetadata getFileDetails(long id) {
        try {
            Optional<FileMetadata> checkFile = sqLiteFileRepository.findById(id);
            if (checkFile.isEmpty()) throw new FileNotFoundException("File does not exist.");
            FileMetadata retrievedFile = checkFile.get();
            if (!Files.exists(Path.of(retrievedFile.getPath())))
                throw new IOException(String.format("File could not be found on the computer! File path: %s", retrievedFile.getPath()));
            retrievedFile.setSize(Files.size(Path.of(retrievedFile.getPath()))); //bytes
            return retrievedFile;
        } catch (Exception e) {
            logger.error(e.getMessage());
            return null;
        }
    }

    @Override
    public Resource getFile(long id) {
        return null;
    }


    @Override
    public File getFolder(String pathWithName) {
        File getFolder = new File(pathWithName);
        if (!getFolder.exists() || getFolder.isFile()) {
            return null;
        }
        return getFolder;
    }

    @Override
    public boolean RemoveFolder(String pathWithName) {
        return false;
    }

    @Override
    @Transactional
    public boolean UpdateFileName(String newName, FileMetadata file) {
        try {
            //TODO add check new file mimetype if its changed then validate and update it
            //find file
            File checkExists = new File(file.getPath());
            if (!checkExists.exists()) throw new FileNotFoundException();
            //rename file
            File renamedFile = new File(checkExists.getParent() + newName);
            //check duplicate
            if (renamedFile.exists()) throw new FileAlreadyExistsException(renamedFile.getName());
            if (!checkExists.renameTo(renamedFile)) throw new FileSystemException(renamedFile.getName());
            //set new name and path
            file.setName(newName);
            file.setPath(renamedFile.getPath());
            //save
            sqLiteFileRepository.save(file);
            return true;
        }  catch (FileNotFoundException fs) {
            logger.error("File not found! Exception: {}", fs.getMessage());
        } catch (FileAlreadyExistsException dup) {
            logger.error("File with name {} already exists! Exception: {}", newName,dup.getMessage());
        } catch (Exception e) {
            logger.error("File system error. Exception: {}", e.getMessage());
        }
        return false;
    }

    @Transactional
    @Override
    public boolean MoveFile(FileMetadata file, String newPath) {
        try {
            //find file
            File checkExists = new File(file.getPath());
            if (!checkExists.exists()) throw new FileNotFoundException();
            if (!checkExists.renameTo(new File(newPath))) throw new FileSystemException(file.getName());
            //set new name and path
            file.setPath(newPath);
            //save
            sqLiteFileRepository.save(file);
            return true;
        }  catch (FileNotFoundException fs) {
            logger.error("File not found! Exception: {}", fs.getMessage());
        } catch (FileAlreadyExistsException dup) {
            logger.error("File at new path {} already exists! Exception: {}", newPath, dup.getMessage());
        } catch (Exception e) {
            logger.error("File system error. Exception: {}", e.getMessage());
        }
        return false;

    }

    @Override
    @Transactional
    public boolean CreateFolder(String pathWithName) {
        try {
            File folder = new File(pathWithName);
            if (folder.exists()) throw new FileAlreadyExistsException("Folder already exists");
            boolean progress = folder.mkdirs();
            if (!progress) throw new IOException("Cannot create directory, path: " + pathWithName);
            FolderMetadata saveFolder = new FolderMetadata(folder.getName(), folder.getPath());
            sqLiteFolderRepository.save(saveFolder);
            return true;
        } catch (FileAlreadyExistsException dup) {
            logger.error("Folder at path \"{}\" already exists! Exception: {}", pathWithName, dup.getMessage());
            return false;
        }catch (Exception e) {
            logger.error(e.getMessage());
            return false;
        }
    }
}
