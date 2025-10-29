package com.cloud.NetworkCloudDrive.Models;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Date;

@Entity
public class FileMetadata {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "name")
    private String name;

    @Column(name = "path")
    private String path;

    @Column(name = "mimiType")
    private String mimiType;

    private long size;

    @Column(name = "createdAt")
    @CreationTimestamp
    private Date createdAt;

    public FileMetadata(String name, String path, String mimiType, long size) {
        this.name = name;
        this.path = path;
        this.mimiType = mimiType;
        this.size = size;
    }

    public FileMetadata() {
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
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
