package com.cloud.NetworkCloudDrive.Controllers;

import com.cloud.NetworkCloudDrive.DTOs.UpdateFileNameDTO;
import com.cloud.NetworkCloudDrive.Models.FileMetadata;
import com.cloud.NetworkCloudDrive.Models.FolderMetadata;
import com.cloud.NetworkCloudDrive.Models.JSONResponse;
import com.cloud.NetworkCloudDrive.Properties.FileStorageProperties;
import com.cloud.NetworkCloudDrive.Services.FileSystemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@RestController
@RequestMapping(path = "/api/filesystem")
public class FileSystemController {
    private final FileSystemService fileSystemService;
    private final FileStorageProperties fileStorageProperties;
    private final Logger logger = LoggerFactory.getLogger(FileSystemController.class);

    public FileSystemController(FileSystemService fileSystemService, FileStorageProperties fileStorageProperties) {
        this.fileSystemService = fileSystemService;
        this.fileStorageProperties = fileStorageProperties;
    }

    @PostMapping(value = "update", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody ResponseEntity<JSONResponse> updateFileName(@RequestBody UpdateFileNameDTO updateFileNameDTO, @RequestParam long fileid) {
        try {
            FileMetadata oldFile = fileSystemService.GetFileMetadata(fileid);
            fileSystemService.UpdateFileName(updateFileNameDTO.getSetName(), oldFile);
            return ResponseEntity.ok().
                    contentType(MediaType.APPLICATION_JSON).
                    body(new JSONResponse(
                            String.format("Updated file with Id %d from %s to %s", fileid, oldFile.getName(), updateFileNameDTO.getSetName()),
                            true));
        } catch (Exception e) {
            logger.error("Cannot update name: {}", e.getMessage());
            return ResponseEntity.badRequest().
                    contentType(MediaType.APPLICATION_JSON).
                    body(new JSONResponse(
                            String.format("Failed to update file with Id %d: %s", fileid, e.getMessage()),
                            false));
        }
    }

    @GetMapping(value = "get/filemetadata", produces = MediaType.ALL_VALUE)
    public @ResponseBody ResponseEntity<?> getFile(@RequestParam long fileid) {
        try {
            FileMetadata fileMetadata = fileSystemService.GetFileMetadata(fileid);
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(fileMetadata);
        } catch (Exception e) {
            logger.error("Failed to get file metadata for fileId: {}. {}", fileid, e.getMessage());
            return ResponseEntity.internalServerError().
                    contentType(MediaType.APPLICATION_JSON).
                    body(new JSONResponse(
                            String.format("Failed to get file metadata for fileId: %d. %s", fileid, e.getMessage()),
                            false));
        }
    }

    @GetMapping(value = "get/foldermetadata", produces = MediaType.ALL_VALUE)
    public @ResponseBody ResponseEntity<?> getFolder(@RequestParam long folderid) {
        try {
            FolderMetadata folderMetadata = fileSystemService.getFolderMetadata(folderid);
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(folderMetadata);
        } catch (Exception e) {
            logger.error("Failed to get folder metadata for fileId: {}. {}", folderid, e.getMessage());
            return ResponseEntity.internalServerError().
                    contentType(MediaType.APPLICATION_JSON).
                    body(new JSONResponse(
                            String.format("Failed to get folder metadata for fileId: %d. %s", folderid, e.getMessage()),
                            false));
        }
    }

    // Conflicted... Could be used for rescan action later
    @GetMapping(value = "list", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody ResponseEntity<?> listFiles(@RequestParam long folderid) { /* use DTO with boolean for base path*/
        try {
            String folderPath;
            if (folderid != 0) {
                FolderMetadata folderMetadata = fileSystemService.getFolderMetadata(folderid);
                folderPath = folderMetadata.getPath();
            } else {
                folderPath = fileStorageProperties.getOnlyUserName();
            }
            List<Path> fileList;
            try(Stream<Path> stream = Files.list(Path.of(fileStorageProperties.getBasePath() +  folderPath))) {
                fileList = stream.toList();
            }
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(fileList);
        } catch (Exception e) {
            return ResponseEntity.badRequest().
                    contentType(MediaType.APPLICATION_JSON).
                    body(new JSONResponse(e.getMessage(), false));
        }
    }

    @GetMapping(value = "dir", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody ResponseEntity<Object> getFileDetails(@RequestParam long pathId) {
        try {
            FileMetadata file = fileSystemService.GetFileMetadata(pathId);
            if (file == null) throw new FileNotFoundException("File does not exist");
            return ResponseEntity.ok().
                    contentType(MediaType.APPLICATION_JSON).
                    body(file);
        } catch (Exception e) {
            return ResponseEntity.badRequest().
                    contentType(MediaType.APPLICATION_JSON).
                    body(new JSONResponse(e.getMessage(), false));
        }
    }
}
