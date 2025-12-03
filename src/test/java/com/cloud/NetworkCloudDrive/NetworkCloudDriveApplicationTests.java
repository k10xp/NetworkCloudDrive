package com.cloud.NetworkCloudDrive;

import com.cloud.NetworkCloudDrive.Enum.UserRole;
import com.cloud.NetworkCloudDrive.Models.FileMetadata;
import com.cloud.NetworkCloudDrive.Models.FolderMetadata;
import com.cloud.NetworkCloudDrive.Models.User;
import com.cloud.NetworkCloudDrive.DAO.SQLiteDAO;
import com.cloud.NetworkCloudDrive.Utilities.FileUtility;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.FileSystemException;
import java.sql.SQLException;

@SpringBootTest
@TestPropertySource(locations = "classpath:/application-test.properties")
class NetworkCloudDriveApplicationTests {

    private final Logger logger = LoggerFactory.getLogger(NetworkCloudDriveApplicationTests.class);

    private FolderMetadata last_saved_folder_metadata;

    @Autowired
    EntityManager entityManager;

    @Autowired
    SQLiteDAO sqLiteDAO;

    @Autowired
    FileUtility fileUtility;

    @Test
    @Order(1)
    void contextLoads() {
        logger.info("Operating System: {}", System.getProperty("os.name"));
    }

    //TODO test to check if "findbyId" is working after save tests succeed

    // JPA TESTS
    @Test
    @Order(2)
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
        FolderMetadata savedFolderMetadata = sqLiteDAO.saveFolder(folderMetadata);

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
        last_saved_folder_metadata = savedFolderMetadata;
        Assertions.assertEquals(folderMetadata, savedFolderMetadata);
    }

    @Test
    @Transactional
    @Order(3)
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
        FileMetadata savedFileMetadata = sqLiteDAO.saveFile(fileMetadata);

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
    @Order(4)
    @Transactional
    public void User_Save_ReturnSavedUser() {
        // Arrange
        User user = new User();
        user.setName("unit_test_username");
        user.setMail("unit_test_username@test.com");
        user.setPassword("unhashed_password_for_unit_test");
        user.setRole(UserRole.GUEST);
        logger.info("Arranged User details: name {} mail {} and password {}. Extra details: registered at {}, last login {} and role {}",
                user.getName(),
                user.getMail(),
                user.getPassword(),
                user.getRegisteredAt(),
                user.getLastLogin(),
                user.getRole()
        );

        // Act
        User savedUser = sqLiteDAO.saveUser(user);

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

    @Test
    @Transactional
    @Order(5)
    public void File_Utility_Reserve_Path_From_ID_Path_Returns_Path() {
        String filePath = "";
        try {
            // get latest ID
            FolderMetadata folderMetadata = sqLiteDAO.queryFolderMetadata(1L);
            filePath = fileUtility.resolvePathFromIdString(folderMetadata.getPath());
        } catch (FileSystemException e) {
            logger.error("Failed to resolve path for Unit Testing. {}", e.getMessage());
            Assertions.fail(e.getMessage());
        } catch (SQLException e) {
            logger.error("Failed to get folder metadata for Unit Testing. {}", e.getMessage());
            Assertions.fail(e.getMessage());
        }
        Assertions.assertEquals("test_user1/folderMetadata_test", filePath);
    }
}
