package com.cloud.NetworkCloudDrive.DTOs;

public class UpdateFileNameDTO {
    private long fileid;
    private String name;

    public UpdateFileNameDTO() {}

    public long getFileid() {
        return this.fileid;
    }
    public void setFileid(long fileid) {
        this.fileid = fileid;
    }
    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }
}
