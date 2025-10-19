package com.cloud.NetworkCloudDrive.Repositories;

import com.cloud.NetworkCloudDrive.Models.FileDetails;
import org.springframework.stereotype.Repository;

import java.io.File;

@Repository
public interface FileRepository {
    //get file type
    String getFileType(String pathWithName);
    //Files
    FileDetails getFile(long id);
    //Folders
    File getFolder(String pathWithName);
    //Actions
    boolean RemoveFolder(String pathWithName);
    boolean UpdateFileName(String oldName, String NewName, String path);
    boolean MoveFile(String oldPath, String newPath);
    boolean CreateFolder(String pathWithName);
    //to be removed
    boolean CreateTextFile(String name, String path, String content);
}
