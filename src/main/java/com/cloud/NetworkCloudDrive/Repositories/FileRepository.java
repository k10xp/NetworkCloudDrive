package com.cloud.NetworkCloudDrive.Repositories;

import com.cloud.NetworkCloudDrive.Models.FileMetadata;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Repository
public interface FileRepository {
    List<FileMetadata> UploadFile(MultipartFile[] files, String folderPath) throws Exception;
    String StoreFile(InputStream inputStream, String fileName, String parentPath) throws IOException;
    Resource getFile(FileMetadata file) throws Exception;
    Resource RetrieveFile(String storedPath) throws Exception;
}
