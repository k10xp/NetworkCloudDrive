package com.cloud.NetworkCloudDrive.Services;

import com.cloud.NetworkCloudDrive.Models.FileMetadata;
import com.cloud.NetworkCloudDrive.Repositories.SQLiteFileRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@Service
public class IOService {
    private FileService fileService;
    private SQLiteFileRepository sqLiteFileRepository;

    public IOService(FileService fileService, SQLiteFileRepository sqLiteFileRepository) {
        this.fileService = fileService;
        this.sqLiteFileRepository = sqLiteFileRepository;
    }


    public FileMetadata UploadImage(MultipartFile file) throws IOException {
        String storagePath;
        try (InputStream inputStream = file.getInputStream()) {
            storagePath = fileService.StoreFile(inputStream, file.getOriginalFilename());
        }
        FileMetadata metadata = new FileMetadata(file.getOriginalFilename(), storagePath, file.getContentType(), file.getSize());
        return sqLiteFileRepository.save(metadata);
    }
}
