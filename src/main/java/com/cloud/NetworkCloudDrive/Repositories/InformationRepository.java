package com.cloud.NetworkCloudDrive.Repositories;

import com.cloud.NetworkCloudDrive.Models.FileMetadata;
import com.cloud.NetworkCloudDrive.Models.FolderMetadata;
import org.springframework.stereotype.Repository;

import java.io.FileNotFoundException;
import java.nio.file.FileSystemException;
import java.util.List;

@Repository
public interface InformationRepository {
    FileMetadata getFileMetadataByFolderIdAndName(long folderId, String name, long userid) throws FileSystemException;
    FolderMetadata getFolderMetadataByFolderIdAndName(long folderId, String name, List<Long> skipList)
            throws FileSystemException, FileNotFoundException;
    FileMetadata getFileMetadata(long id) throws Exception;
    FolderMetadata getFolderMetadata(long fileId) throws Exception;
}
