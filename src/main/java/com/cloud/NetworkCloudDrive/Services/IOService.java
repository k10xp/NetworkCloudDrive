package com.cloud.NetworkCloudDrive.Services;

import com.cloud.NetworkCloudDrive.Models.FileMetadata;
import com.cloud.NetworkCloudDrive.Models.FolderMetadata;
import com.cloud.NetworkCloudDrive.Repositories.IORepository;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

@Service
public class IOService implements IORepository {
    private final SQLiteFileRepository sqLiteFileRepository;
    private final SQLiteFolderRepository sqLiteFolderRepository;
    private final Logger logger = LoggerFactory.getLogger(IOService.class);

    public IOService(SQLiteFileRepository sqLiteFileRepository, SQLiteFolderRepository sqLiteFolderRepository) {
        this.sqLiteFileRepository = sqLiteFileRepository;
        this.sqLiteFolderRepository = sqLiteFolderRepository;
    }

    @Override
    @Transactional
    public FileMetadata getFileDetails(long id) {
        try {
            Optional<FileMetadata> checkFile = sqLiteFileRepository.findById(id);
            boolean isPresent = checkFile.isPresent();
            if (!isPresent) throw new FileNotFoundException("File does not exist.");
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
    public boolean UpdateFileName(String oldName, String NewName, String path) {
        return false;
    }

    @Override
    public boolean MoveFile(String oldPath, String newPath) {
        return false;
    }

    @Override
    @Transactional
    public boolean CreateFolder(String pathWithName) {
        try {
            File folder = new File(pathWithName);
            if (folder.exists()) throw new Exception("Folder already exists");
            boolean progress = folder.mkdirs();
            if (!progress) throw new IOException("Cannot create directory, path: " + pathWithName);
            FolderMetadata saveFolder = new FolderMetadata(folder.getName(), folder.getPath());
            sqLiteFolderRepository.save(saveFolder);
            return true;
        } catch (Exception e) {
            logger.error(e.getMessage());
            return false;
        }
    }
}
