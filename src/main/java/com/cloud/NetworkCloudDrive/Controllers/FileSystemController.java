package com.cloud.NetworkCloudDrive.Controllers;

import com.cloud.NetworkCloudDrive.DTO.UpdateFileNameDTO;
import com.cloud.NetworkCloudDrive.DTO.UpdateFilePathDTO;
import com.cloud.NetworkCloudDrive.DTO.UpdateFolderNameDTO;
import com.cloud.NetworkCloudDrive.DTO.UpdateFolderPathDTO;
import com.cloud.NetworkCloudDrive.Models.FileMetadata;
import com.cloud.NetworkCloudDrive.Models.FolderMetadata;
import com.cloud.NetworkCloudDrive.Models.JSONErrorResponse;
import com.cloud.NetworkCloudDrive.Models.JSONResponse;
import com.cloud.NetworkCloudDrive.Properties.FileStorageProperties;
import com.cloud.NetworkCloudDrive.Services.FileSystemService;
import com.cloud.NetworkCloudDrive.Services.InformationService;
import com.cloud.NetworkCloudDrive.Services.UserService;
import com.cloud.NetworkCloudDrive.Utilities.FileUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.FileSystemException;
import java.nio.file.Path;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping(path = "/api/filesystem")
public class FileSystemController {
    private final FileSystemService fileSystemService;
    private final UserService userService;
    private final FileUtility fileUtility;
    private final FileStorageProperties fileStorageProperties;
    private final InformationService informationService;
    private final Logger logger = LoggerFactory.getLogger(FileSystemController.class);

    public FileSystemController(
            FileSystemService fileSystemService,
            FileStorageProperties fileStorageProperties,
            InformationService informationService,
            UserService userService,
            FileUtility fileUtility) {
        this.fileSystemService = fileSystemService;
        this.fileStorageProperties = fileStorageProperties;
        this.informationService = informationService;
        this.fileUtility = fileUtility;
        this.userService = userService;
    }

    @PostMapping(value = "file/rename", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody ResponseEntity<JSONResponse> updateFileName(@RequestBody UpdateFileNameDTO updateFileNameDTO) {
        try {
            FileMetadata oldFile = informationService.getFileMetadata(updateFileNameDTO.getFile_id());
            String oldName = oldFile.getName();
            String updatedPath = fileSystemService.updateFileName(updateFileNameDTO.getName(), oldFile);
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).
                    body(new JSONResponse(
                            String.format(
                                    "Updated file with Id %d from %s to %s. Updated path %s",
                                    updateFileNameDTO.getFile_id(), oldName, updateFileNameDTO.getName(), updatedPath),
                            true));
        } catch (Exception e) {
            logger.error("Cannot update name: {}", e.getMessage());
            return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON).
                    body(new JSONErrorResponse(
                            String.format("Failed to update file with Id %d: %s", updateFileNameDTO.getFile_id(), e.getMessage()),
                            e.getClass().getName(),false));
        }
    }

    @PostMapping(value = "folder/rename", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody ResponseEntity<JSONResponse> updateFolderName(@RequestBody UpdateFolderNameDTO updateFolderNameDTO) {
        try {
            FolderMetadata oldFolder = informationService.getFolderMetadata(updateFolderNameDTO.getFolder_id());
            String oldName = oldFolder.getName();
            String updatedPath = fileSystemService.updateFolderName(updateFolderNameDTO.getName(), oldFolder);
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).
                    body(new JSONResponse(
                            String.format(
                                    "Updated folder name with Id %d from %s to %s. Updated path %s",
                                    updateFolderNameDTO.getFolder_id(), oldName, updateFolderNameDTO.getName(), updatedPath),
                            true));
        } catch (Exception e) {
            logger.error("Cannot update folder name. {}", e.getMessage());
            return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON).
                    body(new JSONErrorResponse(
                            String.format("Failed to update folder with Id %d: %s", updateFolderNameDTO.getFolder_id(), e.getMessage()),
                            e.getClass().getName(),false));
        }
    }

    @PostMapping(value = "folder/move", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody ResponseEntity<JSONResponse> moveFile(@RequestBody UpdateFolderPathDTO updateFolderPathDTO) {
        try {
            FolderMetadata folderToMove = informationService.getFolderMetadata(updateFolderPathDTO.getFormer_folder_id());
            String oldPath = fileUtility.resolvePathFromIdString(folderToMove.getPath());
            String newPath = fileSystemService.moveFolder(folderToMove, updateFolderPathDTO.getDestination_folder_id());
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).
                    body(new JSONResponse(
                            String.format("Moved folder with Id %d from %s to %s", updateFolderPathDTO.getFormer_folder_id(), oldPath, newPath),
                            true));
        } catch (Exception e) {
            logger.error("Cannot move folder. {}", e.getMessage());
            return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON).
                    body(new JSONErrorResponse(
                            String.format("Failed to move folder with Id %d: %s", updateFolderPathDTO.getFormer_folder_id(), e.getMessage()),
                            e.getClass().getName(),false));
        }
    }

    @PostMapping(value = "file/move", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody ResponseEntity<JSONResponse> moveFile(@RequestBody UpdateFilePathDTO updateFilePathDTO) {
        try {
            FileMetadata fileToMove = informationService.getFileMetadata(updateFilePathDTO.getFile_id());
            String oldPath = (updateFilePathDTO.getFolder_id() != 0 ?
                    fileUtility.resolvePathFromIdString(informationService.getFolderMetadata(updateFilePathDTO.getFolder_id()).getPath())
                    :
                    fileStorageProperties.getOnlyUserName());
            logger.info("old path controller {}", oldPath);
            String newPath = fileSystemService.moveFile(fileToMove, updateFilePathDTO.getFolder_id());
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).
                    body(new JSONResponse(
                            String.format("Moved file with Id %d from %s to %s", updateFilePathDTO.getFile_id(), oldPath, newPath),
                            true));
        } catch (Exception e) {
            logger.error("Cannot move name: {}", e.getMessage());
            return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON).
                    body(new JSONErrorResponse(
                            String.format("Failed to move file with Id %d: %s", updateFilePathDTO.getFile_id(), e.getMessage()),
                            e.getClass().getName(),false));
        }
    }

    @PostMapping(value = "folder/remove", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody ResponseEntity<JSONResponse> removeFolder(@RequestParam long folderid) {
        try {
            FolderMetadata folderToRemove = informationService.getFolderMetadata(folderid);
            String oldPath = fileSystemService.removeFolder(folderToRemove);
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).
                    body(new JSONResponse(
                            String.format("Folder with Id %d at path %s was successfully removed", folderToRemove.getId(), oldPath),
                            true));
        } catch (Exception e) {
            logger.error("Cannot remove folder #{}: {}", folderid, e.getMessage());
            return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON).
                    body(new JSONErrorResponse(String.format("Failed remove folder with Id %d", folderid), e.getClass().getName(),false));
        }
    }

    @PostMapping(value = "file/remove", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody ResponseEntity<JSONResponse> removeFile(@RequestParam long fileid) {
        try {
            FileMetadata fileToRemove = informationService.getFileMetadata(fileid);
            String oldPath = fileSystemService.removeFile(fileToRemove);
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).
                    body(new JSONResponse(
                            String.format("file with Id %d at path %s was successfully removed", fileToRemove.getId(), oldPath),
                            true));
        } catch (Exception e) {
            logger.error("Cannot remove file #{}: {}", fileid, e.getMessage());
            return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON).
                    body(new JSONErrorResponse(
                            String.format("Failed remove file with Id %d: %s", fileid, e.getMessage()),
                            e.getClass().getName(), false));
        }
    }

    @GetMapping(value = "list", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody ResponseEntity<?> listFiles(@RequestParam long folderid, Principal principal) {
        try {
            List<Path> fileList = fileUtility.getFileAndFolderPathsFromFolder(fileUtility.getFolderPath(folderid));
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).
                    body(fileSystemService.getListOfMetadataFromPath(
                            fileList, folderid, userService.currentUserDetails(principal.getName()).getId()));
        } catch (FileSystemException fileSystemException) {
            logger.error("Some folders couldn't be found at folder with Id {}, reason: {}", folderid, fileSystemException.getMessage());
            return ResponseEntity.internalServerError().contentType(MediaType.APPLICATION_JSON).
                    body(new JSONErrorResponse(String.format("Some folders couldn't be found at folder with Id %d", folderid),
                            fileSystemException.getClass().getName(), false));
        } catch (Exception e) {
            logger.error("Failed to list items in folder with Id {}, reason: {}", folderid, e.getMessage());
            return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON).body(
                    new JSONErrorResponse(
                            String.format("Failed to list items inside folder with Id %d", folderid),
                            e.getClass().getName(), false));
        }
    }
}
