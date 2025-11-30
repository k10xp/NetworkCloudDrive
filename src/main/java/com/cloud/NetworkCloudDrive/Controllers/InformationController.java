package com.cloud.NetworkCloudDrive.Controllers;

import com.cloud.NetworkCloudDrive.Models.FileMetadata;
import com.cloud.NetworkCloudDrive.Models.FolderMetadata;
import com.cloud.NetworkCloudDrive.Models.JSONErrorResponse;
import com.cloud.NetworkCloudDrive.Models.JSONResponse;
import com.cloud.NetworkCloudDrive.Properties.FileStorageProperties;
import com.cloud.NetworkCloudDrive.Services.InformationService;
import com.cloud.NetworkCloudDrive.Utilities.FileUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;

@RestController
@RequestMapping(path = "/api/info")
public class InformationController {
    private final FileUtility fileUtility;
    private final FileStorageProperties fileStorageProperties;
    private final InformationService informationService;
    private final Logger logger = LoggerFactory.getLogger(InformationController.class);

    public InformationController(
            FileUtility fileUtility,
            InformationService informationService,
            FileStorageProperties fileStorageProperties) {
        this.fileUtility = fileUtility;
        this.informationService = informationService;
        this.fileStorageProperties = fileStorageProperties;
    }

    @GetMapping(value = "get/filemetadata", produces = MediaType.ALL_VALUE)
    public @ResponseBody ResponseEntity<?> getFile(@RequestParam long fileid) {
        try {
            FileMetadata fileMetadata = informationService.getFileMetadata(fileid);
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(fileMetadata);
        } catch (Exception e) {
            logger.error("Failed to get file metadata for fileId: {}. {}", fileid, e.getMessage());
            return ResponseEntity.internalServerError().contentType(MediaType.APPLICATION_JSON).
                    body(new JSONErrorResponse(
                            String.format("Failed to get file metadata for fileId: %d. %s", fileid, e.getMessage()),
                            e.getClass().getName(), false));
        }
    }

    @GetMapping(value = "get/foldermetadata", produces = MediaType.ALL_VALUE)
    public @ResponseBody ResponseEntity<?> getFolder(@RequestParam long folderid) {
        try {
            FolderMetadata folderMetadata;
            if (folderid != 0) {
                folderMetadata = informationService.getFolderMetadata(folderid);
                folderMetadata.setPath(fileUtility.resolvePathFromIdString(folderMetadata.getPath()));
            } else {
                File folderRootMetadata = new File(fileStorageProperties.getFullPath());
                folderMetadata = new FolderMetadata(folderRootMetadata.getName(), folderRootMetadata.getPath());
                folderMetadata.setId(folderid);
                folderMetadata.setUserid(0L); //placeholder
            }
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(folderMetadata);
        } catch (Exception e) {
            logger.error("Failed to get folder metadata for fileId: {}. {}", folderid, e.getMessage());
            return ResponseEntity.internalServerError().contentType(MediaType.APPLICATION_JSON).
                    body(new JSONErrorResponse(String.format("Failed to get folder metadata for fileId: %d. %s", folderid, e.getMessage()),
                            e.getClass().getName(),
                            false));
        }
    }
}
