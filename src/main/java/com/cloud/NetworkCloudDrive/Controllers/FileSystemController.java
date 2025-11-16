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
import com.cloud.NetworkCloudDrive.Services.InformationService;
import com.cloud.NetworkCloudDrive.Utilities.FileUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@RestController
@RequestMapping(path = "/api/filesystem")
public class FileSystemController {
    private final FileSystemService fileSystemService;
    private final FileUtility fileUtility;
    private final FileStorageProperties fileStorageProperties;
    private final InformationService informationService;
    private final Logger logger = LoggerFactory.getLogger(FileSystemController.class);

    public FileSystemController(
            FileSystemService fileSystemService,
            FileStorageProperties fileStorageProperties,
            InformationService informationService,
            FileUtility fileUtility) {
        this.fileSystemService = fileSystemService;
        this.fileStorageProperties = fileStorageProperties;
        this.informationService = informationService;
        this.fileUtility = fileUtility;
    }

    @PostMapping(value = "file/rename", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody ResponseEntity<JSONResponse> updateFileName(@RequestBody UpdateFileNameDTO updateFileNameDTO) {
        try {
            FileMetadata oldFile = informationService.getFileMetadata(updateFileNameDTO.getFileid());
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
                            String.format("Failed to update file with Id %d: %s", updateFileNameDTO.getFileid(), e.getMessage()), false));
        }
    }

    @PostMapping(value = "folder/rename", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody ResponseEntity<JSONResponse> updateFolderName(@RequestBody UpdateFolderNameDTO updateFolderNameDTO) {
        try {
            FolderMetadata oldFolder = informationService.getFolderMetadata(updateFolderNameDTO.getFolderid());
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
                            String.format(
                                    "Failed to update folder with Id %d: %s",
                                    updateFolderNameDTO.getFolderid(),
                                    e.getMessage()),
                            false));
        }
    }

    @PostMapping(value = "folder/move", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody ResponseEntity<JSONResponse> moveFile(@RequestBody UpdateFolderPathDTO updateFolderPathDTO) {
        try {
            FolderMetadata folderToMove = informationService.getFolderMetadata(updateFolderPathDTO.getFormerFolderid());
            FolderMetadata destinationFolder = informationService.getFolderMetadata(updateFolderPathDTO.getDestinationFolderid());
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
                            String.format(
                                    "Failed to move folder with Id %d: %s",
                                    updateFolderPathDTO.getFormerFolderid(),
                                    e.getMessage()),
                            false));
        }
    }

    @PostMapping(value = "file/move", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody ResponseEntity<JSONResponse> moveFile(@RequestBody UpdateFilePathDTO updateFilePathDTO) {
        try {
            FileMetadata fileToMove = informationService.getFileMetadata(updateFilePathDTO.getFileid());
            String oldPath = informationService.getFolderPathAsString(fileToMove.getFolderId());
            logger.info("old path controller {}", oldPath);
            String newPath = informationService.getFolderPathAsString(updateFilePathDTO.getFolderid());
            fileToMove.setFolderId(updateFilePathDTO.getFolderid());
            fileSystemService.moveFile(fileToMove, newPath, oldPath);
            return ResponseEntity.ok().
                    contentType(MediaType.APPLICATION_JSON).
                    body(new JSONResponse(
                            String.format(
                                    "Moved file with Id %d from %s to %s",
                                    updateFilePathDTO.getFileid(),
                                    oldPath,
                                    newPath),
                            true));
        } catch (Exception e) {
            logger.error("Cannot move name: {}", e.getMessage());
            return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON).
                    body(new JSONResponse(
                            String.format(
                                    "Failed to move file with Id %d: %s",
                                    updateFilePathDTO.getFileid(),
                                    e.getMessage()),
                            false));
        }
    }

    @PostMapping(value = "folder/remove", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody ResponseEntity<JSONResponse> removeFolder(@RequestParam long folderid) {
        try {
            FolderMetadata folderToRemove = informationService.getFolderMetadata(folderid);
            String oldPath = fileUtility.resolvePathFromIdString(folderToRemove.getPath());
            fileSystemService.removeFolder(folderToRemove);
            return ResponseEntity.ok().
                    contentType(MediaType.APPLICATION_JSON).
                    body(new JSONResponse(
                            String.format(
                                    "Folder with Id %d at path %s was successfully removed",
                                    folderToRemove.getId(),
                                    oldPath),
                            true));
        } catch (Exception e) {
            logger.error("Cannot remove folder #{}: {}",folderid, e.getMessage());
            return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON).
                    body(new JSONResponse(
                            String.format(
                                    "Failed remove folder with Id %d",
                                    folderid),
                            false));
        }
    }

    @PostMapping(value = "file/remove", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody ResponseEntity<JSONResponse> removeFile(@RequestParam long fileid) {
        try {
            FileMetadata fileToRemove = informationService.getFileMetadata(fileid);
            String oldPath = fileUtility.resolvePathFromIdString(
                    informationService.getFolderMetadata(fileToRemove.getFolderId()).getPath()) +
                    File.separator +
                    fileToRemove.getName();
            fileSystemService.removeFile(fileToRemove);
            return ResponseEntity.ok().
                    contentType(MediaType.APPLICATION_JSON).
                    body(new JSONResponse(
                            String.format(
                                    "file with Id %d at path %s was successfully removed",
                                    fileToRemove.getId(),
                                    oldPath),
                            true));
        } catch (Exception e) {
            logger.error("Cannot remove file #{}: {}",fileid, e.getMessage());
            return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON).
                    body(new JSONResponse(
                            String.format(
                                    "Failed remove file with Id %d: %s",
                                    fileid,
                                    e.getMessage()),
                            false));
        }
    }

    @GetMapping(value = "list", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody ResponseEntity<?> listFiles(@RequestParam long folderid) {
        try {
            List<Path> fileList = fileUtility.getFileAndFolderPathsFromFolder((folderid != 0 ?
                    fileUtility.resolvePathFromIdString(informationService.getFolderMetadata(folderid).getPath())
                    :
                    fileStorageProperties.getOnlyUserName()));
            return ResponseEntity.ok().
                    contentType(MediaType.APPLICATION_JSON).
                    body(fileSystemService.getListOfMetadataFromPath(fileList, folderid));
        } catch (FileSystemException fileSystemException) {
            return ResponseEntity.internalServerError().
                    contentType(MediaType.APPLICATION_JSON).
                    body(new JSONResponse(fileSystemException.getMessage(), false));
        } catch (Exception e) {
            return ResponseEntity.badRequest().
                    contentType(MediaType.APPLICATION_JSON).
                    body(new JSONResponse(e.getMessage(), false));
        }
    }
}
