package com.cloud.NetworkCloudDrive;

import com.cloud.NetworkCloudDrive.Enum.UserRole;
import com.cloud.NetworkCloudDrive.Models.FileMetadata;
import com.cloud.NetworkCloudDrive.Models.FolderMetadata;
import com.cloud.NetworkCloudDrive.Models.User;
import com.cloud.NetworkCloudDrive.Repositories.SQLiteFileRepository;
import com.cloud.NetworkCloudDrive.Repositories.SQLiteFolderRepository;
import com.cloud.NetworkCloudDrive.Repositories.SQLiteUserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@TestPropertySource(locations = "classpath:/application-test.properties")
class NetworkCloudDriveApplicationTests {

    private final Logger logger = LoggerFactory.getLogger(NetworkCloudDriveApplicationTests.class);

    @Autowired
    EntityManager entityManager;

    @Autowired
    SQLiteFolderRepository sqLiteFolderRepository;

    @Autowired
    SQLiteFileRepository sqLiteFileRepository;

    @Autowired
    SQLiteUserRepository sqLiteUserRepository;


    @Test
    void contextLoads() {
        logger.info("Operating System: {}", System.getProperty("os.name"));
    }

    //TODO test to check if "findbyId" is working after save tests succeed

    // JPA TESTS
    @Test
    @Transactional
    public void FolderMetadata_Save_ReturnSavedFolderMetadata() {
        // Arrange
        FolderMetadata folderMetadata = new FolderMetadata();
        entityManager.persist(folderMetadata);
        folderMetadata.setUserid(0L);
        folderMetadata.setName("folderMetadata_test");
        folderMetadata.setPath("0/" + folderMetadata.getId());
        logger.info("Arranged Folder Metadata: name {} ID path {} and belongs to user {}. Extra details: created at {}",
                folderMetadata.getName(), folderMetadata.getPath(), folderMetadata.getUserid(), folderMetadata.getCreatedAt());
        // Act
        FolderMetadata savedFolderMetadata = sqLiteFolderRepository.save(folderMetadata);

        if (savedFolderMetadata != null)
            logger.info(
                "Saved Folder Metadata ID {}: name {} ID path {} and belongs to user {}. Extra details: created at {}",
                savedFolderMetadata.getId(),
                savedFolderMetadata.getName(),
                savedFolderMetadata.getPath(),
                savedFolderMetadata.getUserid(),
                savedFolderMetadata.getCreatedAt()
            );

        // Assert
        Assertions.assertEquals(folderMetadata, savedFolderMetadata);
    }

    @Test
    @Transactional
    public void FileMetadata_Save_ReturnSavedFileMetadata() {
        // Arrange
        FileMetadata fileMetadata = new FileMetadata();
        fileMetadata.setUserid(0L);
        fileMetadata.setName("fileMetadata_test.txt");
        fileMetadata.setMimiType("text/plain");
        fileMetadata.setFolderId(0L);
        logger.info("Arranged File Metadata: name {} inside folder ID {} and belongs to user {}. Extra details: mimetype {} and created at {}",
                fileMetadata.getName(),
                fileMetadata.getFolderId(),
                fileMetadata.getUserid(),
                fileMetadata.getMimiType(),
                fileMetadata.getCreatedAt());

        // Act
        FileMetadata savedFileMetadata = sqLiteFileRepository.save(fileMetadata);

        if (savedFileMetadata != null)
            logger.info(
                "Saved File Metadata ID {}: name {} inside folder ID {} and belongs to user {}. Extra details: mimetype {} and created at {}",
                savedFileMetadata.getId(),
                savedFileMetadata.getName(),
                savedFileMetadata.getFolderId(),
                savedFileMetadata.getUserid(),
                savedFileMetadata.getMimiType(),
                savedFileMetadata.getCreatedAt()
            );

        // Assert
        Assertions.assertEquals(fileMetadata, savedFileMetadata);
    }

    @Test
    @Transactional
    public void User_Save_ReturnSavedUser() {
        // Arrange
        User user = new User();
        user.setName("unit_test_username");
        user.setMail("unit_test_username@test.com");
        user.setPassword("unhashed_password_for_unit_test");
        user.setRole(UserRole.NORMAL_USER);
        logger.info("Arranged User details: name {} mail {} and password {}. Extra details: registered at {}, last login {} and role {}",
                user.getName(),
                user.getMail(),
                user.getPassword(),
                user.getRegisteredAt(),
                user.getLastLogin(),
                user.getRole()
        );

        // Act
        User savedUser = sqLiteUserRepository.save(user);

        if (savedUser != null)
            logger.info(
                "Saved User ID {} details: name {} mail {} and password {}. Extra details: registered at {}, last login {} and role {}",
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getMail(),
                savedUser.getPassword(),
                savedUser.getRegisteredAt(),
                savedUser.getLastLogin(),
                savedUser.getRole()
            );

        // Assert
        Assertions.assertEquals(user, savedUser);
    }
}
