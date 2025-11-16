package com.cloud.NetworkCloudDrive.Repositories;

import com.cloud.NetworkCloudDrive.Models.FileMetadata;
import com.cloud.NetworkCloudDrive.Models.FolderMetadata;
import org.springframework.stereotype.Repository;

import java.io.FileNotFoundException;
import java.nio.file.FileSystemException;
import java.util.List;

@Repository
public interface FileSystemRepository {
    void removeFile(FileMetadata file) throws Exception;
    void removeFolder(FolderMetadata folder) throws Exception;
    void updateFolderName(String newName, FolderMetadata folder) throws Exception;
    void updateFileName(String newName, FileMetadata file) throws Exception;
    void moveFolder(FolderMetadata folder, String newPath) throws Exception;
    void moveFile(FileMetadata targetFile, FolderMetadata destinationFolder, String currentFolder) throws Exception;
    FolderMetadata createFolder(String folderName, long folderid) throws Exception;
}
