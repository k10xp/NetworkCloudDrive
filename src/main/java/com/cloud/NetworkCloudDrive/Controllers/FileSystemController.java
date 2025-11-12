package com.cloud.NetworkCloudDrive.Controllers;

import com.cloud.NetworkCloudDrive.DTOs.UpdateFileNameDTO;
import com.cloud.NetworkCloudDrive.DTOs.UpdateFilePathDTO;
import com.cloud.NetworkCloudDrive.DTOs.UpdateFolderNameDTO;
import com.cloud.NetworkCloudDrive.DTOs.UpdateFolderPathDTO;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
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

    @PostMapping(value = "file/rename", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody ResponseEntity<JSONResponse> updateFileName(@RequestBody UpdateFileNameDTO updateFileNameDTO) {
        try {
            FileMetadata oldFile = fileSystemService.getFileMetadata(updateFileNameDTO.getFileid());
            String oldName = oldFile.getName();
            fileSystemService.updateFileName(updateFileNameDTO.getName(), oldFile);
            return ResponseEntity.ok().
                    contentType(MediaType.APPLICATION_JSON).
                    body(new JSONResponse(
                            String.format(
                                    "Updated file with Id %d from %s to %s",
                                    updateFileNameDTO.getFileid(),
                                    oldName,
                                    updateFileNameDTO.getName()),
                            true));
        } catch (Exception e) {
            logger.error("Cannot update name: {}", e.getMessage());
            return ResponseEntity.badRequest().
                    contentType(MediaType.APPLICATION_JSON).
                    body(new JSONResponse(
                            String.format("Failed to update file with Id %d: %s", updateFileNameDTO.getFileid(), e.getMessage()),
                            false));
        }
    }

    @PostMapping(value = "folder/rename", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody ResponseEntity<JSONResponse> updateFolderName(@RequestBody UpdateFolderNameDTO updateFolderNameDTO) {
        try {
            FolderMetadata oldFolder = fileSystemService.getFolderMetadata(updateFolderNameDTO.getFolderid());
            String oldName = oldFolder.getName();
            fileSystemService.updateFolderName(updateFolderNameDTO.getName(), oldFolder);
            return ResponseEntity.ok().
                    contentType(MediaType.APPLICATION_JSON).
                    body(new JSONResponse(
                            String.format(
                                    "Updated folder with Id %d from %s to %s",
                                    updateFolderNameDTO.getFolderid(),
                                    oldName,
                                    updateFolderNameDTO.getName()),
                            true));
        } catch (Exception e) {
            logger.error("Cannot update folder name. {}", e.getMessage());
            return ResponseEntity.badRequest().
                    contentType(MediaType.APPLICATION_JSON).
                    body(new JSONResponse(
                            String.format("Failed to update folder with Id %d: %s", updateFolderNameDTO.getFolderid(), e.getMessage()),
                            false));
        }
    }

    @PostMapping(value = "folder/move", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody ResponseEntity<JSONResponse> moveFile(@RequestBody UpdateFolderPathDTO updateFolderPathDTO) {
        try {
            FolderMetadata folderToMove = fileSystemService.getFolderMetadata(updateFolderPathDTO.getFormerFolderid());
            FolderMetadata destinationFolder = fileSystemService.getFolderMetadata(updateFolderPathDTO.getDestinationFolderid());
            String oldPath = folderToMove.getPath();
            fileSystemService.moveFolder(folderToMove, destinationFolder.getPath());
            return ResponseEntity.ok().
                    contentType(MediaType.APPLICATION_JSON).
                    body(new JSONResponse(
                            String.format(
                                    "Moved folder with Id %d from %s to %s",
                                    updateFolderPathDTO.getFormerFolderid(),
                                    oldPath,
                                    destinationFolder.getPath()),
                            true));
        } catch (Exception e) {
            logger.error("Cannot move folder. {}", e.getMessage());
            return ResponseEntity.badRequest().
                    contentType(MediaType.APPLICATION_JSON).
                    body(new JSONResponse(
                            String.format("Failed to move folder with Id %d: %s", updateFolderPathDTO.getFormerFolderid(), e.getMessage()),
                            false));
        }
    }

    @PostMapping(value = "file/move", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody ResponseEntity<JSONResponse> moveFile(@RequestBody UpdateFilePathDTO updateFilePathDTO) {
        try {
            FileMetadata fileToMove = fileSystemService.getFileMetadata(updateFilePathDTO.getFileid());
            FolderMetadata destinationFolder = fileSystemService.getFolderMetadata(updateFilePathDTO.getFolderid());
            String oldPath = fileToMove.getOwner();
            fileSystemService.moveFile(fileToMove, fileSystemService.resolvePathFromIdString(destinationFolder.getPath()));
            return ResponseEntity.ok().
                    contentType(MediaType.APPLICATION_JSON).
                    body(new JSONResponse(
                            String.format(
                                    "Moved file with Id %d from %s to %s",
                                    updateFilePathDTO.getFileid(),
                                    oldPath,
                                    destinationFolder.getPath()),
                            true));
        } catch (Exception e) {
            logger.error("Cannot move name: {}", e.getMessage());
            return ResponseEntity.badRequest().
                    contentType(MediaType.APPLICATION_JSON).
                    body(new JSONResponse(
                            String.format("Failed to move file with Id %d: %s", updateFilePathDTO.getFileid(), e.getMessage()),
                            false));
        }
    }

    @PostMapping(value = "folder/remove", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody ResponseEntity<JSONResponse> removeFolder(@RequestParam long folderid) {
        try {
            FolderMetadata folderToRemove = fileSystemService.getFolderMetadata(folderid);
            String oldPath = folderToRemove.getPath();
            fileSystemService.removeFolder(folderToRemove);
            return ResponseEntity.ok().
                    contentType(MediaType.APPLICATION_JSON).
                    body(new JSONResponse(
                            String.format(
                                    "Folder with Id %d at path %s was successfully removed",
                                    folderToRemove.getId(),
                                    oldPath
                            ), true));
        } catch (Exception e) {
            logger.error("Cannot remove folder #{}: {}",folderid, e.getMessage());
            return ResponseEntity.badRequest().
                    contentType(MediaType.APPLICATION_JSON).
                    body(new JSONResponse(
                            String.format("Failed remove folder with Id %d: %s", folderid, e.getMessage()),
                            false));
        }
    }

    @PostMapping(value = "file/remove", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody ResponseEntity<JSONResponse> removeFile(@RequestParam long fileid) {
        try {
            FileMetadata fileToRemove = fileSystemService.getFileMetadata(fileid);
            String oldPath = fileSystemService.resolvePathFromIdString(
                    fileSystemService.getFolderMetadata(fileToRemove.getFolderId()).getPath()) +
                    File.separator +
                    fileToRemove.getName();
            fileSystemService.removeFile(fileToRemove);
            return ResponseEntity.ok().
                    contentType(MediaType.APPLICATION_JSON).
                    body(new JSONResponse(
                            String.format(
                                    "file with Id %d at path %s was successfully removed",
                                    fileToRemove.getId(),
                                    oldPath
                            ), true));
        } catch (Exception e) {
            logger.error("Cannot remove file #{}: {}",fileid, e.getMessage());
            return ResponseEntity.badRequest().
                    contentType(MediaType.APPLICATION_JSON).
                    body(new JSONResponse(
                            String.format("Failed remove file with Id %d: %s", fileid, e.getMessage()),
                            false));
        }
    }

    @GetMapping(value = "get/filemetadata", produces = MediaType.ALL_VALUE)
    public @ResponseBody ResponseEntity<?> getFile(@RequestParam long fileid) {
        try {
            FileMetadata fileMetadata = fileSystemService.getFileMetadata(fileid);
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
            folderMetadata.setPath(fileSystemService.resolvePathFromIdString(folderMetadata.getPath()));
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
}
