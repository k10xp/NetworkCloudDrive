package com.cloud.NetworkCloudDrive.Repositories;

import com.cloud.NetworkCloudDrive.Models.FileMetadata;
import com.cloud.NetworkCloudDrive.Models.FolderMetadata;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.util.List;

@Repository
public interface FileSystemRepository {
    //get file type
    FileMetadata GetFileMetadata(long id) throws Exception;

    //Files
    Resource getFile(FileMetadata file) throws Exception;

    List<FileMetadata> UploadFile(MultipartFile[] files, String folderPath) throws Exception;

    //Folders
    FolderMetadata getFolderMetadata(long fileId) throws Exception;

    //Actions
    void RemoveFolder(FolderMetadata folder) throws Exception;

    void UpdateFileName(String newName, FileMetadata file) throws Exception;

    void MoveFile(FileMetadata file, String newPath) throws Exception;

    void CreateFolder(String pathWithName) throws Exception;
}
