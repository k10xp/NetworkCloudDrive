package com.cloud.NetworkCloudDrive.DTOs;

public class UpdateFilePathDTO {
    private long fileid;
    private long folderid;

    public UpdateFilePathDTO() {}

    public long getFileid() {
        return this.fileid;
    }
    public void setFileid(long fileid) {
        this.fileid = fileid;
    }
    public long getFolderid() {
        return this.folderid;
    }
    public void setFolderid(long folderid) {
        this.folderid = folderid;
    }
}
