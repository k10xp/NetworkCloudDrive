package com.cloud.NetworkCloudDrive.Services;

import com.cloud.NetworkCloudDrive.Models.FileMetadata;
import com.cloud.NetworkCloudDrive.Models.FolderMetadata;
import com.cloud.NetworkCloudDrive.Properties.FileStorageProperties;
import com.cloud.NetworkCloudDrive.Repositories.FileSystemRepository;
import com.cloud.NetworkCloudDrive.Repositories.SQLiteFileRepository;
import com.cloud.NetworkCloudDrive.Repositories.SQLiteFolderRepository;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
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
    private final FileService fileService;
    private final Logger logger = LoggerFactory.getLogger(FileSystemService.class);

    public FileSystemService(SQLiteFileRepository sqLiteFileRepository, SQLiteFolderRepository sqLiteFolderRepository, FileStorageProperties fileStorageProperties, FileService fileService) {
        this.sqLiteFileRepository = sqLiteFileRepository;
        this.fileStorageProperties = fileStorageProperties;
        this.sqLiteFolderRepository = sqLiteFolderRepository;
        this.fileService = fileService;
    }

    @Override
    @Transactional
    public FileMetadata GetFileMetadata(long id) throws IOException {
        Optional<FileMetadata> checkFile = sqLiteFileRepository.findById(id);
        if (checkFile.isEmpty()) throw new FileNotFoundException("File does not exist.");
        FileMetadata retrievedFile = checkFile.get();
        if (!Files.exists(Path.of(retrievedFile.getPath())))
            throw new IOException(String.format("File could not be found on the computer! File path: %s", retrievedFile.getPath()));
        retrievedFile.setSize(Files.size(Path.of(retrievedFile.getPath()))); //bytes
        return retrievedFile;
    }

    @Override
    public Resource getFile(long id) throws IOException {
        Optional<FileMetadata> file = sqLiteFileRepository.findById(id);
        if (file.isEmpty()) throw new FileNotFoundException(String.format("File with id %d not found\n", id));
        return fileService.RetrieveFile(file.get().getPath());
    }


    @Override
    public File getFolder(String pathWithName) throws FileAlreadyExistsException {
        File getFolder = new File(pathWithName);
        if (!getFolder.exists() || getFolder.isFile()) {
            throw new FileAlreadyExistsException(String.format("Folder with same name at path %s already exists", pathWithName));
        }
        return getFolder;
    }

    @Override
    @Transactional
    public void RemoveFolder(FolderMetadata folder) throws Exception {
        //find folder
        File checkExists = new File(folder.getPath());
        if (!checkExists.exists()) throw new FileNotFoundException();
        //remove Folder
        if (!checkExists.delete())
            throw new FileSystemException(String.format("Failed to remove folder at path %s\n", folder.getPath()));
        sqLiteFolderRepository.delete(folder);
    }

    private void ValidateFile(String mimeType) throws FileUploadException {
        if (!fileStorageProperties.getAllowedMimeTypes().contains(mimeType)) throw new FileUploadException("MimeType not allowed");
    }

    @Override
    public FileMetadata UploadFile(MultipartFile file) throws Exception {
        ValidateFile(file.getContentType());
        String storagePath;
        try (InputStream inputStream = file.getInputStream()) {
            storagePath = fileService.StoreFile(inputStream, file.getOriginalFilename());
        }
        FileMetadata metadata = new FileMetadata(file.getOriginalFilename(), storagePath, file.getContentType(), file.getSize());
        return sqLiteFileRepository.save(metadata);
    }

    @Override
    @Transactional
    public void UpdateFileName(String newName, FileMetadata file) throws Exception {
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
    }

    @Transactional
    @Override
    public void MoveFile(FileMetadata file, String newPath) throws Exception {
        //find file
        File checkExists = new File(file.getPath());
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
        File folder = new File(pathWithName);
        if (folder.exists()) throw new FileAlreadyExistsException("Folder already exists");
        boolean progress = folder.mkdirs();
        if (!progress) throw new IOException("Cannot create directory, path: " + pathWithName);
        FolderMetadata saveFolder = new FolderMetadata(folder.getName(), folder.getPath());
        sqLiteFolderRepository.save(saveFolder);
    }
}
