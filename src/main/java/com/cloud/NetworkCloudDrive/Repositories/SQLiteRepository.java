package com.cloud.NetworkCloudDrive.Repositories;

import com.cloud.NetworkCloudDrive.Models.FileDetails;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SQLiteRepository extends JpaRepository<FileDetails, Long> {
    List<FileDetails> findByName(String name);
}
