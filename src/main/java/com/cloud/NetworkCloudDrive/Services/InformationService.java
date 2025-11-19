package com.cloud.NetworkCloudDrive.Services;

import com.cloud.NetworkCloudDrive.Models.FileMetadata;
import com.cloud.NetworkCloudDrive.Models.FolderMetadata;
import com.cloud.NetworkCloudDrive.Properties.FileStorageProperties;
import com.cloud.NetworkCloudDrive.Repositories.InformationRepository;
import com.cloud.NetworkCloudDrive.Repositories.SQLiteFileRepository;
import com.cloud.NetworkCloudDrive.Repositories.SQLiteFolderRepository;
import com.cloud.NetworkCloudDrive.Utilities.FileUtility;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystemException;
import java.util.List;
import java.util.Optional;

@Service
public class InformationService implements InformationRepository {
    private final SQLiteFileRepository sqLiteFileRepository;
    private final SQLiteFolderRepository sqLiteFolderRepository;
    private final FileStorageProperties fileStorageProperties;
    private final FileUtility fileUtility;

    public InformationService(
            SQLiteFileRepository sqLiteFileRepository,
            SQLiteFolderRepository sqLiteFolderRepository,
            FileStorageProperties fileStorageProperties,
            FileUtility fileUtility) {
        this.sqLiteFileRepository = sqLiteFileRepository;
        this.sqLiteFolderRepository = sqLiteFolderRepository;
        this.fileStorageProperties = fileStorageProperties;
        this.fileUtility = fileUtility;
    }

    @Override
    public String getFolderPathAsString(long folderId) throws Exception {
        return folderId != 0
                ?
                fileUtility.resolvePathFromIdString(getFolderMetadata(folderId).getPath())
                :
                fileStorageProperties.getOnlyUserName();
    }

    @Transactional
    @Override
    public FileMetadata getFileMetadataByFolderIdAndName(long folderId, String name, long userid) throws FileSystemException {
        // dummy metadata for search
        FileMetadata dummyFileMetadata = new FileMetadata();
        dummyFileMetadata.setName(name);
        dummyFileMetadata.setFolderId(folderId);
        dummyFileMetadata.setUserid(userid);
        dummyFileMetadata.setMimiType(null);
        dummyFileMetadata.setSize(null);
        dummyFileMetadata.setId(null);
        dummyFileMetadata.setCreatedAt(null);
        Example<FileMetadata> fileMetadataExample = Example.of(dummyFileMetadata);
        Optional<FileMetadata> optionalFileMetadata = sqLiteFileRepository.findOne(fileMetadataExample);
        if (optionalFileMetadata.isEmpty())
            throw new FileSystemException("File not found in database. Is database synced?");
        return optionalFileMetadata.get();
    }

    @Transactional
    @Override
    public FolderMetadata getFolderMetadataByFolderIdAndName(long folderId, String name, List<Long> skipList)
            throws FileSystemException, FileNotFoundException {
        String idPath;
        if (folderId != 0) {
            Optional<FolderMetadata> optionalParentFolderMetadata = sqLiteFolderRepository.findById(folderId);
            if (optionalParentFolderMetadata.isEmpty())
                throw new FileNotFoundException("Invalid folderId or Folder is not synced with database");
            idPath = optionalParentFolderMetadata.get().getPath();
        } else {
            idPath = "0";
        }
        List<FolderMetadata> findAllByPathList = sqLiteFolderRepository.findAllByPathContainsIgnoreCase(idPath);
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
    @Transactional
    public FileMetadata getFileMetadata(long id) throws Exception {
        Optional<FileMetadata> checkFile = sqLiteFileRepository.findById(id);
        if (checkFile.isEmpty()) throw new FileNotFoundException("File with Id " + id + " does not exist");
        FileMetadata retrievedFile = checkFile.get();
        File fileCheck = new File(
                fileStorageProperties.getBasePath() +
                        getFolderPathAsString(retrievedFile.getFolderId()) +
                        File.separator +
                        retrievedFile.getName());
        if (!fileCheck.exists())
            throw new IOException(String.format("File could not be found on the computer! File path: %s", fileCheck.getPath()));
        retrievedFile.setSize(fileCheck.length()); //bytes
        return retrievedFile;
    }

    @Override
    public FolderMetadata getFolderMetadata(long fileId) throws IOException {
        Optional<FolderMetadata> folderMetadata = sqLiteFolderRepository.findById(fileId);
        if (folderMetadata.isEmpty())
            throw new FileNotFoundException("Folder with Id " + fileId + " does not exist");
        FolderMetadata folder = folderMetadata.get();
        File getFolder = new File(fileStorageProperties.getBasePath() + fileUtility.resolvePathFromIdString(folder.getPath()));
        if (!getFolder.exists())
            throw new FileAlreadyExistsException(String.format("Folder with name at path %s does not exist", getFolder.getPath()));
        return folder;
    }
}
