package com.cloud.NetworkCloudDrive.Repositories;

import com.cloud.NetworkCloudDrive.Models.FileMetadata;
import com.cloud.NetworkCloudDrive.Models.FolderMetadata;
import org.springframework.stereotype.Repository;

import java.io.FileNotFoundException;
import java.nio.file.FileSystemException;
import java.util.List;

@Repository
public interface FileSystemRepository {
    FileMetadata getFileMetadataByFolderIdAndName(long folderId, String name, String owner) throws FileSystemException;
    FolderMetadata getFolderMetadataByFolderIdAndName(long folderId, String name) throws FileSystemException, FileNotFoundException;
    FileMetadata getFileMetadata(long id) throws Exception;
    FolderMetadata getFolderMetadata(long fileId) throws Exception;
    void removeFile(FileMetadata file) throws Exception;
    void removeFolder(FolderMetadata folder) throws Exception;
    void updateFolderName(String newName, FolderMetadata folder) throws Exception;
    void updateFileName(String newName, FileMetadata file) throws Exception;
    void moveFolder(FolderMetadata folder, String newPath) throws Exception;
    void moveFile(FileMetadata file, String newPath) throws Exception;
    FolderMetadata createFolder(String folderName, long folderid) throws Exception;
    String resolvePathFromIdString(String idString);
}
