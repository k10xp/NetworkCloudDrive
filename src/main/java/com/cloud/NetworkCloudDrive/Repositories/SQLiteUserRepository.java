package com.cloud.NetworkCloudDrive.Repositories;

import com.cloud.NetworkCloudDrive.Models.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SQLiteUserRepository extends JpaRepository<User, Long> {}
