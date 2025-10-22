package com.cloud.NetworkCloudDrive.Services;

import com.cloud.NetworkCloudDrive.Models.FileMetadata;
import com.cloud.NetworkCloudDrive.Repositories.SQLiteFileRepository;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

@Service
public class IOService {
    private FileService fileService;
    private SQLiteFileRepository sqLiteFileRepository;

    public IOService(FileService fileService, SQLiteFileRepository sqLiteFileRepository) {
        this.fileService = fileService;
        this.sqLiteFileRepository = sqLiteFileRepository;
    }


    public FileMetadata UploadFile(MultipartFile file) throws IOException {
        String storagePath;
        try (InputStream inputStream = file.getInputStream()) {
            storagePath = fileService.StoreFile(inputStream, file.getOriginalFilename());
        }
        FileMetadata metadata = new FileMetadata(file.getOriginalFilename(), storagePath, file.getContentType(), file.getSize());
        return sqLiteFileRepository.save(metadata);
    }

    public Resource DownloadFile(long fileId) throws Exception{
        Optional<FileMetadata> file = sqLiteFileRepository.findById(fileId);
        if (file.isEmpty()) throw new FileNotFoundException(String.format("%d\n", fileId));
        return fileService.RetrieveFile(file.get().getPath());
    }

    public FileMetadata GetFileMetadata(long fileId) throws Exception{
        Optional<FileMetadata> file = sqLiteFileRepository.findById(fileId);
        if (file.isEmpty()) throw new FileNotFoundException(String.format("%d\n", fileId));
        return file.get();
    }
}
