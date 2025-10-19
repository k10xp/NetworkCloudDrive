package com.cloud.NetworkCloudDrive.Repositories;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.io.InputStream;

@Repository
public interface FileRepository {
    String StoreFile(InputStream inputStream, String fileName) throws IOException;

    Resource RetrieveFile(String storedPath) throws IOException;
}
