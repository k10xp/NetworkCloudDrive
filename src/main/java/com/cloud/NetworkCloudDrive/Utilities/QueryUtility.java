package com.cloud.NetworkCloudDrive.Utilities;

import com.cloud.NetworkCloudDrive.Models.FileMetadata;
import com.cloud.NetworkCloudDrive.Models.FolderMetadata;
import com.cloud.NetworkCloudDrive.Repositories.SQLiteFileRepository;
import com.cloud.NetworkCloudDrive.Repositories.SQLiteFolderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileNotFoundException;
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

    public QueryUtility(SQLiteFolderRepository sqLiteFolderRepository, SQLiteFileRepository sqLiteFileRepository) {
        this.sqLiteFolderRepository = sqLiteFolderRepository;
        this.sqLiteFileRepository = sqLiteFileRepository;
    }

    // DAO stuff
    @Transactional
    public void deleteFolder(FolderMetadata folder) {
        sqLiteFolderRepository.delete(folder);
    }

    @Transactional
    public void deleteFile(FileMetadata file) {
        sqLiteFileRepository.delete(file);
    }

    @Transactional
    public void saveFolder(FolderMetadata folder) {
        sqLiteFolderRepository.delete(folder);
    }

    @Transactional
    public void saveFile(FileMetadata file) {
        sqLiteFileRepository.save(file);
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
    public FileMetadata getFileMetadataByFolderIdNameAndUserId(long folderId, String name, long userid) throws FileSystemException {
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
}
