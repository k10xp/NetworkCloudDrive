package com.cloud.NetworkCloudDrive.DTOs;

public class UpdateFolderPathDTO {
    private long formerFolderid;
    private long destinationFolderid;

    public UpdateFolderPathDTO() {}

    public long getFormerFolderid() {
        return this.formerFolderid;
    }
    public void setFormerFolderid(long formerFolderid) {
        this.formerFolderid = formerFolderid;
    }
    public long getDestinationFolderid() {
        return this.destinationFolderid;
    }
    public void setDestinationFolderid(long destinationFolderid) {
        this.destinationFolderid = destinationFolderid;
    }
}
