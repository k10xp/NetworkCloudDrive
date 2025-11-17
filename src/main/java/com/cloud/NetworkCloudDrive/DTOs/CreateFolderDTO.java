package com.cloud.NetworkCloudDrive.DTOs;

public class CreateFolderDTO {
    private long folder_id;
    private String path;

    public CreateFolderDTO() {}

    public long getFolder_id() {
        return folder_id;
    }
    public void setFolder_id(long folder_id) {
        this.folder_id = folder_id;
    }
    public String getPath() {
        return path;
    }
    public void setPath(String path) {
        this.path = path;
    }
}
