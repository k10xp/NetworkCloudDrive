package com.cloud.NetworkCloudDrive.DTOs;

public class UpdateFolderNameDTO {
    private long folderid;
    private String name;

    public UpdateFolderNameDTO() {}

    public long getFolderid() {
        return folderid;
    }
    public void setFolderid(long folderid) {
        this.folderid = folderid;
    }
    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }
}
