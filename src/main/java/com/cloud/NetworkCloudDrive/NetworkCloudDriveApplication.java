package com.cloud.NetworkCloudDrive;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class NetworkCloudDriveApplication {
    public static void main(String[] args) {
        System.out.println("Operating System: " + System.getProperty("os.name"));
        SpringApplication.run(NetworkCloudDriveApplication.class, args);
    }
}
