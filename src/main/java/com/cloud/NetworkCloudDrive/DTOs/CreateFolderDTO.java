package com.cloud.NetworkCloudDrive.DTOs;

public class CreateFolderDTO {
    private long folderid;
    private String path;

    public CreateFolderDTO() {}

    public long getFolderid() {
        return folderid;
    }
    public void setFolderid(long folderid) {
        this.folderid = folderid;
    }
    public String getPath() {
        return path;
    }
    public void setPath(String path) {
        this.path = path;
    }
}
