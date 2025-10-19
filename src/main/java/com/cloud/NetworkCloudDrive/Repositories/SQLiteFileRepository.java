package com.cloud.NetworkCloudDrive.Repositories;

import com.cloud.NetworkCloudDrive.Models.FileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SQLiteFileRepository extends JpaRepository<FileMetadata, Long> {}
