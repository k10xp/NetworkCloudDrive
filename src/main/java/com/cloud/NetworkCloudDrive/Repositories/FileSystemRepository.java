package com.cloud.NetworkCloudDrive.Repositories;

import com.cloud.NetworkCloudDrive.Models.FileMetadata;
import com.cloud.NetworkCloudDrive.Models.FolderMetadata;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;

@Repository
public interface FileSystemRepository {
    //get file type
    FileMetadata GetFileMetadata(long id) throws IOException;

    //Files
    Resource getFile(long id) throws IOException;

    FileMetadata UploadFile(MultipartFile file) throws Exception;

    //Folders
    File getFolder(String pathWithName) throws FileAlreadyExistsException;

    //Actions
    void RemoveFolder(FolderMetadata folder) throws Exception;

    void UpdateFileName(String newName, FileMetadata file) throws Exception;

    void MoveFile(FileMetadata file, String newPath) throws Exception;

    void CreateFolder(String pathWithName) throws Exception;
}
