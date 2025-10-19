package com.cloud.NetworkCloudDrive.Repositories;

import com.cloud.NetworkCloudDrive.Models.FolderMetadata;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SQLiteFolderRepository extends JpaRepository<FolderMetadata, Long> {}
