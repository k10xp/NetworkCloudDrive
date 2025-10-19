package com.cloud.NetworkCloudDrive.Models;

import jakarta.persistence.*;

@Entity
public class FileDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "name")
    private String name;
    @Column(name = "path")
    private String path;
    @Column(name = "size")
    private long size;
    @Column(name = "folder")
    private boolean isFolder;

    public FileDetails(String name, String path, boolean isFolder) {
        this.name = name;
        this.path = path;
        this.isFolder = isFolder;
    }

    public FileDetails() {}

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public long getSize() {
        return size;
    }

    public boolean isFolder() {
        return isFolder;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setFolder(boolean folder) {
        isFolder = folder;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
