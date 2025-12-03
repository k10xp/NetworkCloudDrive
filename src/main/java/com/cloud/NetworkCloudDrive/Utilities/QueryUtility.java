package com.cloud.NetworkCloudDrive.Utilities;

import com.cloud.NetworkCloudDrive.Models.FileMetadata;
import com.cloud.NetworkCloudDrive.Models.FolderMetadata;
import com.cloud.NetworkCloudDrive.Models.User;
import com.cloud.NetworkCloudDrive.Repositories.SQLiteFileRepository;
import com.cloud.NetworkCloudDrive.Repositories.SQLiteFolderRepository;
import com.cloud.NetworkCloudDrive.Repositories.SQLiteUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.FileSystemException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

// Basically DAO but for multiple types*
@Component
public class QueryUtility {
    private final Logger logger = LoggerFactory.getLogger(QueryUtility.class);
    private final SQLiteFolderRepository sqLiteFolderRepository;
    private final SQLiteFileRepository sqLiteFileRepository;
    private final SQLiteUserRepository sqLiteUserRepository;

    public QueryUtility(
            SQLiteFolderRepository sqLiteFolderRepository,
            SQLiteFileRepository sqLiteFileRepository,
            SQLiteUserRepository sqLiteUserRepository) {
        this.sqLiteFolderRepository = sqLiteFolderRepository;
        this.sqLiteFileRepository = sqLiteFileRepository;
        this.sqLiteUserRepository = sqLiteUserRepository;
    }

    // DAO stuff

    // Delete
    @Transactional
    public void deleteFolder(FolderMetadata folder) {
        sqLiteFolderRepository.delete(folder);
    }

    @Transactional
    public void deleteFile(FileMetadata file) {
        sqLiteFileRepository.delete(file);
    }

    @Transactional
    public void deleteUser(User user) {
        sqLiteUserRepository.delete(user);
    }

    // Add/Update
    @Transactional
    public FolderMetadata saveFolder(FolderMetadata folder) {
        return sqLiteFolderRepository.save(folder);
    }

    @Transactional
    public FileMetadata saveFile(FileMetadata file) {
        return sqLiteFileRepository.save(file);
    }

    @Transactional
    public User saveUser(User user) {
        return sqLiteUserRepository.save(user);
    }

    @Transactional
    public void saveAllFolders(List<FolderMetadata> folderMetadata) {
        sqLiteFolderRepository.saveAll(folderMetadata);
    }

    @Transactional
    public void saveAllFiles(List<FileMetadata> fileMetadata) {
        sqLiteFileRepository.saveAll(fileMetadata);
    }

    // Database service layer
    @Transactional
    public List<FolderMetadata> findAllContainingSectionOfIdPathIgnoreCase(String idPath) {
        return sqLiteFolderRepository.findAllByPathContainsIgnoreCase(idPath);
    }

    @Transactional
    public FileMetadata queryFileMetadata(long fileId) throws SQLException {
        Optional<FileMetadata> fileMetadata = sqLiteFileRepository.findById(fileId);
        if (fileMetadata.isEmpty())
            throw new SQLException("File with Id " + fileId + " does not exist");
        return fileMetadata.get();
    }

    @Transactional
    public FolderMetadata queryFolderMetadata(long folderId) throws SQLException {
        Optional<FolderMetadata> folderMetadata = sqLiteFolderRepository.findById(folderId);
        if (folderMetadata.isEmpty())
            throw new SQLException("Folder with Id " + folderId + " does not exist");
        return folderMetadata.get();
    }

    @Transactional
    public List<FolderMetadata> getChildrenFoldersInDirectory(String idPath) throws SQLException {
        List<FolderMetadata> findAllByPathList = sqLiteFolderRepository.findAllByPathContainsIgnoreCase(idPath);
        if (findAllByPathList.isEmpty())
            throw new SQLException("Can't find folders at idPath " + idPath + " in database");
        return findAllByPathList;
    }

    @Transactional
    public List<FolderMetadata> findAllByIdInSQLFolderMetadata(List<Long> folderIdList) {
        return sqLiteFolderRepository.findAllById(folderIdList);
    }

    @Transactional
    public FileMetadata getFileMetadataByFolderIdNameAndUserId(long folderId, String name, long userId) throws FileSystemException {
        // dummy metadata for search
        FileMetadata dummyFileMetadata = new FileMetadata();
        dummyFileMetadata.setName(name);
        dummyFileMetadata.setFolderId(folderId);
        dummyFileMetadata.setUserid(userId);
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
    public FolderMetadata getFolderMetadataFromIdPathAndName(String idPath, String name, long userId) throws FileSystemException {
        logger.info("dummy\nname: {}\npath: {}\nuserid: {}", name, idPath, userId);
        // dummy metadata for search
        FolderMetadata dummyFolderMetadata = new FolderMetadata();
        dummyFolderMetadata.setName(name);
        dummyFolderMetadata.setPath(idPath);
        dummyFolderMetadata.setId(null);
        dummyFolderMetadata.setCreatedAt(null);
        dummyFolderMetadata.setUserid(userId); //current logged in user id
        Example<FolderMetadata> folderMetadataExample = Example.of(dummyFolderMetadata);
        Optional<FolderMetadata> optionalFolderMetadata = sqLiteFolderRepository.findOne(folderMetadataExample);
        if (optionalFolderMetadata.isEmpty())
            throw new FileSystemException("Folder not found in database. Is database synced?");
        return optionalFolderMetadata.get();
    }
}
