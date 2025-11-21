package com.cloud.NetworkCloudDrive.Services;

import com.cloud.NetworkCloudDrive.Models.FileMetadata;
import com.cloud.NetworkCloudDrive.Models.FolderMetadata;
import com.cloud.NetworkCloudDrive.Properties.FileStorageProperties;
import com.cloud.NetworkCloudDrive.Repositories.InformationRepository;
import com.cloud.NetworkCloudDrive.Repositories.SQLiteFileRepository;
import com.cloud.NetworkCloudDrive.Repositories.SQLiteFolderRepository;
import com.cloud.NetworkCloudDrive.Utilities.FileUtility;
import com.cloud.NetworkCloudDrive.Utilities.QueryUtility;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystemException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Service
public class InformationService implements InformationRepository {
    private final FileUtility fileUtility;
    private final QueryUtility queryUtility;

    public InformationService(FileUtility fileUtility, QueryUtility queryUtility) {
        this.fileUtility = fileUtility;
        this.queryUtility = queryUtility;
    }

    @Transactional
    @Override
    public FolderMetadata getFolderMetadataByFolderIdAndName(long folderId, String name, List<Long> skipList)
            throws FileSystemException, FileNotFoundException {
        String idPath = fileUtility.getIdPath(folderId);
        List<FolderMetadata> findAllByPathList = queryUtility.findAllContainingSectionOfIdPathIgnoreCase(idPath);
        if (findAllByPathList.isEmpty())
            throw new FileSystemException("Can't resolve path");
        String[] splitOriginalPath = idPath.split("/");
        int originalPathLength = splitOriginalPath.length;
        FolderMetadata returnFolder = new FolderMetadata();
        for (FolderMetadata folderMetadata : findAllByPathList) {
            if (skipList.contains(folderMetadata.getId()))
                continue;
            String[] splitBySlash = folderMetadata.getPath().split("/");
            if ((splitBySlash.length > originalPathLength) && (splitBySlash.length < originalPathLength + 2)) {
                returnFolder = folderMetadata;
                break;
            }
        }
        return returnFolder;
    }

    @Override
    public FileMetadata getFileMetadata(long id) throws FileNotFoundException, SQLException {
        FileMetadata retrievedFile = queryUtility.queryFileMetadata(id);
        File fileCheck = fileUtility.returnFileIfItExists(
                fileUtility.getFolderPath(retrievedFile.getFolderId()) + File.separator + retrievedFile.getName());
        retrievedFile.setSize(fileCheck.length()); //bytes
        return retrievedFile;
    }

    @Override
    public FolderMetadata getFolderMetadata(long folderId) throws IOException, SQLException {
        FolderMetadata folder = queryUtility.queryFolderMetadata(folderId);
        File getFolder = fileUtility.returnFileIfItExists(fileUtility.resolvePathFromIdString(folder.getPath()));
        return folder;
    }
}
