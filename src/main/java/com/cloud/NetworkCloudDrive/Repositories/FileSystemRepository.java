package com.cloud.NetworkCloudDrive.Repositories;

import com.cloud.NetworkCloudDrive.Models.FileMetadata;
import com.cloud.NetworkCloudDrive.Models.FolderMetadata;
import org.springframework.stereotype.Repository;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileSystemException;
import java.nio.file.Path;
import java.util.List;

@Repository
public interface FileSystemRepository {
    String removeFile(FileMetadata file) throws Exception;
    String removeFolder(FolderMetadata folder) throws IOException;
    void updateFolderName(String newName, FolderMetadata folder) throws Exception;
    void updateFileName(String newName, FileMetadata file) throws Exception;
    void moveFolder(FolderMetadata folder, String newPath) throws Exception;
    void moveFile(FileMetadata targetFile, String destinationFolder, String currentFolder) throws Exception;
    FolderMetadata createFolder(String folderName, long folderid) throws Exception;
    List<Object> getListOfMetadataFromPath(List<Path> filePaths, long currentFolderId)
            throws FileSystemException, FileNotFoundException;
}
