package com.cloud.NetworkCloudDrive.Services;

import com.cloud.NetworkCloudDrive.Models.FileDetails;
import com.cloud.NetworkCloudDrive.Repositories.FileRepository;
import com.cloud.NetworkCloudDrive.Repositories.SQLiteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;

@Service
public class FileService implements FileRepository {
    @Autowired
    private SQLiteRepository sqLiteRepository;

//    public FileService(SQLiteRepository sqLiteRepository) {
//        this.sqLiteRepository = sqLiteRepository;
//    }
//

    @Override
    public String getFileType(String pathWithName) {
        return null;
    }

    @Override
    @Transactional
    public FileDetails getFile(long id) {
        try {
            File getFile = new File(sqLiteRepository.findById(id).get().getPath());
            if (!getFile.exists()) throw new IOException("File could not be found! File path: ");
            FileDetails returned = new FileDetails(getFile.getName(), getFile.getPath(), !getFile.isFile());
            returned.setSize(getFile.length()); //bytes
            return returned;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public File getFolder(String pathWithName) {
        File getFolder = new File(pathWithName);
        if (!getFolder.exists() || getFolder.isFile()) {
            return null;
        }
        return getFolder;
    }

    @Override
    public boolean RemoveFolder(String pathWithName) {
        return false;
    }

    @Override
    public boolean UpdateFileName(String oldName, String NewName, String path) {
        return false;
    }

    @Override
    public boolean MoveFile(String oldPath, String newPath) {
        return false;
    }

    @Override
    @Transactional
    public boolean CreateFolder(String pathWithName) {
        try {
            File folder = new File(pathWithName);
            if (folder.exists()) throw new Exception("Folder already exists");
            boolean progress = folder.mkdirs();
            if (!progress) throw new IOException("Cannot create directory, path: " + pathWithName);
            FileDetails saveFolder = new FileDetails(folder.getName(), folder.getPath(), true);
            sqLiteRepository.save(saveFolder);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    @Transactional
    public boolean CreateTextFile(String name, String path, String content) {
        FileDetails file = new FileDetails(name, path, false);
        try {
            sqLiteRepository.save(file);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
