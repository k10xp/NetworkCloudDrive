package com.cloud.NetworkCloudDrive.Controllers;

import com.cloud.NetworkCloudDrive.DAO.SQLiteDAO;
import com.cloud.NetworkCloudDrive.Models.JSONErrorResponse;
import com.cloud.NetworkCloudDrive.Models.JSONResponse;
import com.cloud.NetworkCloudDrive.Properties.FileStorageProperties;
import com.cloud.NetworkCloudDrive.Sessions.UserSession;
import com.cloud.NetworkCloudDrive.Utilities.FileUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;

@RestController
@RequestMapping(path = "/api/maintenance")
public class MaintenanceController {
    private final Logger logger = LoggerFactory.getLogger(MaintenanceController.class);
    private final UserSession userSession;
    private final SQLiteDAO sqLiteDAO;
    private final FileUtility fileUtility;
    private final FileStorageProperties fileStorageProperties;

    public MaintenanceController(UserSession userSession, SQLiteDAO sqLiteDAO, FileUtility fileUtility, FileStorageProperties fileStorageProperties) {
        this.userSession = userSession;
        this.sqLiteDAO = sqLiteDAO;
        this.fileUtility = fileUtility;
        this.fileStorageProperties = fileStorageProperties;
    }

    @GetMapping("scan")
    public @ResponseBody ResponseEntity<?> scanDirectory(@RequestParam long folderid) {
        try {
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON)
                    .body(new JSONResponse(fileUtility.traverseFileTree(
                            Path.of(fileStorageProperties.getFullPath(fileUtility.getFolderPath(folderid))))));
        } catch (Exception e) {
            return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON)
                    .body(new JSONErrorResponse(e, "error scanning"));
        }
    }
}
