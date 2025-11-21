package com.cloud.NetworkCloudDrive;

import com.cloud.NetworkCloudDrive.Models.FileMetadata;
import com.cloud.NetworkCloudDrive.Models.FolderMetadata;
import com.cloud.NetworkCloudDrive.Repositories.SQLiteFileRepository;
import com.cloud.NetworkCloudDrive.Repositories.SQLiteFolderRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ContextConfiguration(classes = NetworkCloudDriveApplication.class)
class NetworkCloudDriveApplicationTests {

    @Autowired
    EntityManager entityManager;
    @Autowired
    SQLiteFolderRepository sqLiteFolderRepository;
    @Autowired
    SQLiteFileRepository sqLiteFileRepository;

    //TODO Implement CreateFolder Test then check its status code (for github workflows)
    // or list
    @Test
    void contextLoads() {
        System.out.println("Operating System: " + System.getProperty("os.name"));
    }


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

        // Act
        FolderMetadata savedFolderMetadata = sqLiteFolderRepository.save(folderMetadata);

        // Assert
        Assertions.assertNotNull(savedFolderMetadata);
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

        // Act
        FileMetadata savedFileMetadata = sqLiteFileRepository.save(fileMetadata);

        // Assert
        Assertions.assertNotNull(savedFileMetadata);
    }
}
