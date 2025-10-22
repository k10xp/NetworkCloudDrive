package com.cloud.NetworkCloudDrive.Models;

import jakarta.persistence.*;
import java.time.ZoneId;
import java.time.ZonedDateTime;

//TODO add folder permissions

@Entity
public class FolderMetadata {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "name")
    private String name;

    @Column(name = "path")
    private String path;

    @Column(name = "createdAt")
    private ZonedDateTime createdAt;

    public FolderMetadata(String name, String path) {
        this.name = name;
        this.path = path;
        this.createdAt = ZonedDateTime.now(ZoneId.systemDefault());
    }

    public FolderMetadata() {
    }

    public long getId() {
        return id;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
