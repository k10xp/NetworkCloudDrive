package com.cloud.NetworkCloudDrive.Models;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

//TODO fix createdAt to be more readable and add ownership (later after auth)

@Entity
public class FileMetadata {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "name")
    private String name;

    @Column(name = "folderId")
    private Long folderId;

    @Column(name = "owner")
    private String owner;

    @Column(name = "mimiType")
    private String mimiType;

    private long size;

    @Column(name = "createdAt")
    @CreationTimestamp
    private Instant createdAt;

    public FileMetadata(String name, Long folderId,String owner, String mimiType, long size) {
        this.name = name;
        this.folderId = folderId;
        this.owner = owner;
        this.mimiType = mimiType;
        this.size = size;
    }

    public FileMetadata() {}

    public String getOwner() {
        return owner;
    }
    public void setId(long id) {
        this.id = id;
    }
    public Instant getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
    public Long getFolderId() {
        return folderId;
    }
    public void setFolderId(Long folderId) {
        this.folderId = folderId;
    }
    public void setOwner(String owner) {
        this.owner = owner;
    }
    public long getId() {
        return id;
    }
    public long getSize() {
        return size;
    }
    public void setSize(long size) {
        this.size = size;
    }
    public String getMimiType() {
        return mimiType;
    }
    public void setMimiType(String mimiType) {
        this.mimiType = mimiType;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
}
