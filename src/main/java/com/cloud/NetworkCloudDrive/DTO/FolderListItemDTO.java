package com.cloud.NetworkCloudDrive.DTO;

import com.cloud.NetworkCloudDrive.Models.FolderMetadata;

import java.time.Instant;

public class FolderListItemDTO {
    private long id;
    private String name;
    private Instant createdAt;

    public FolderListItemDTO() {}

    public FolderListItemDTO(FolderMetadata folderMetadata) {
        this.id = folderMetadata.getId();
        this.name = folderMetadata.getName();
        this.createdAt = folderMetadata.getCreatedAt();
    }

    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public Instant getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
