package com.cloud.NetworkCloudDrive.Repositories;

import com.cloud.NetworkCloudDrive.Models.FileMetadata;
import com.cloud.NetworkCloudDrive.Models.FolderMetadata;
import org.springframework.stereotype.Repository;

@Repository
public interface FileSystemRepository {
    //get file/folder metadata
    FileMetadata GetFileMetadata(long id) throws Exception;
    FolderMetadata getFolderMetadata(long fileId) throws Exception;

    //Actions
    void RemoveFolder(FolderMetadata folder) throws Exception;
    void UpdateFileName(String newName, FileMetadata file) throws Exception;
    void MoveFile(FileMetadata file, String newPath) throws Exception;
    void CreateFolder(String pathWithName) throws Exception;
}
