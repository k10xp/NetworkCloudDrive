package com.cloud.NetworkCloudDrive.Repositories;

import com.cloud.NetworkCloudDrive.Models.FileMetadata;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Repository;

import java.io.File;

@Repository
public interface FileSystemRepository {
    //get file type
    FileMetadata getFileDetails(long id);

    //Files
    Resource getFile(long id);

    //Folders
    File getFolder(String pathWithName);

    //Actions
    boolean RemoveFolder(String pathWithName);

    boolean UpdateFileName(String oldName, String NewName, String path);

    boolean MoveFile(String oldPath, String newPath);

    boolean CreateFolder(String pathWithName);
}
