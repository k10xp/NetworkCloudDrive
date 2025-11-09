package com.cloud.NetworkCloudDrive.Services;

import com.cloud.NetworkCloudDrive.Models.FileMetadata;
import com.cloud.NetworkCloudDrive.Properties.FileStorageProperties;
import com.cloud.NetworkCloudDrive.Repositories.FileRepository;
import com.cloud.NetworkCloudDrive.Repositories.SQLiteFileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StreamUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

@Service
public class FileService implements FileRepository {
    private final FileStorageProperties fileStorageProperties;
    private final SQLiteFileRepository sqLiteFileRepository;
    private final Path rootPath;
    private final Logger logger = LoggerFactory.getLogger(FileService.class);

    public FileService(FileStorageProperties fileStorageProperties, SQLiteFileRepository sqLiteFileRepository) {
        this.sqLiteFileRepository = sqLiteFileRepository;
        this.fileStorageProperties = fileStorageProperties;
        this.rootPath = Paths.get(fileStorageProperties.getBasePath());
    }

    @Override
    @Transactional
    public List<FileMetadata> UploadFile(MultipartFile[] files, String folderPath) throws Exception {
        String storagePath;
        List<FileMetadata> uploadedFiles = new ArrayList<>();
        for (MultipartFile file : files) {
            try (InputStream inputStream = file.getInputStream()) {
                storagePath = StoreFile(inputStream, file.getOriginalFilename(), folderPath);
            }
            FileMetadata metadata = new FileMetadata(file.getOriginalFilename(), storagePath, file.getContentType(), file.getSize());
            uploadedFiles.add(metadata);
        }
        return sqLiteFileRepository.saveAll(uploadedFiles);
    }

    public String StoreFile(InputStream inputStream, String fileName, String parentPath) throws IOException {
        Path userDirectory = rootPath.resolve(Path.of(parentPath)); /* To be extended */
        Files.createDirectories(userDirectory);
        Path filePath = userDirectory.resolve(fileName);
        logger.info("test1: {}", filePath);
        try (OutputStream outputStream = Files.newOutputStream(filePath, StandardOpenOption.CREATE_NEW)) {
            logger.info("random message");
            StreamUtils.copy(inputStream, outputStream);
        }
        logger.info("test2: {}", rootPath.relativize(filePath));
        return rootPath.relativize(filePath).toString();
    }

    @Override
    public Resource getFile(FileMetadata file) throws Exception {
        logger.info("file metadata: {}", file.toString());
        return RetrieveFile(file.getPath());
    }

    public Resource RetrieveFile(String storedPath) throws Exception {
//        Path filePath = rootPath.resolve(storedPath).normalize().toAbsolutePath();
        Path filePath = Path.of(fileStorageProperties.getBasePath() + File.separator + storedPath);
        logger.info("path: {}",filePath);
        Path normalizedRoot = rootPath.normalize().toAbsolutePath();
        logger.info("normalized path: {}", normalizedRoot);
        if (filePath.startsWith(normalizedRoot)) throw new SecurityException("Unauthorized access");
        if (!Files.exists(filePath)) throw new IOException("File does not exist");
        return new UrlResource(filePath.toUri());
    }
}
