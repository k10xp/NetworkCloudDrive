package com.cloud.NetworkCloudDrive.Repositories;

import com.cloud.NetworkCloudDrive.Models.FileMetadata;
import com.cloud.NetworkCloudDrive.Models.FolderMetadata;
import org.springframework.stereotype.Repository;

@Repository
public interface FileSystemRepository {
    FileMetadata getFileMetadata(long id) throws Exception;
    FolderMetadata getFolderMetadata(long fileId) throws Exception;
    void removeFile(FileMetadata file) throws Exception;
    void removeFolder(FolderMetadata folder) throws Exception;
    void updateFolderName(String newName, FolderMetadata folder) throws Exception;
    void updateFileName(String newName, FileMetadata file) throws Exception;
    void moveFolder(FolderMetadata folder, String newPath) throws Exception;
    void moveFile(FileMetadata file, String newPath) throws Exception;
    void createFolder(String pathWithName) throws Exception;
}
