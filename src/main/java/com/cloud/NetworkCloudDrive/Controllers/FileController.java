package com.cloud.NetworkCloudDrive.Controllers;

import com.cloud.NetworkCloudDrive.DTOs.CreateFolderDTO;
import com.cloud.NetworkCloudDrive.Models.FileMetadata;
import com.cloud.NetworkCloudDrive.Models.FolderMetadata;
import com.cloud.NetworkCloudDrive.Models.JSONResponse;
import com.cloud.NetworkCloudDrive.Properties.FileStorageProperties;
import com.cloud.NetworkCloudDrive.Services.FileService;
import com.cloud.NetworkCloudDrive.Services.FileSystemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping(path = "/api/file")
public class FileController {
    private final FileSystemService fileSystemService;
    private final FileService fileService;
    private final FileStorageProperties fileStorageProperties;
    private final Logger logger = LoggerFactory.getLogger(FileController.class);

    public FileController(FileSystemService fileSystemService, FileStorageProperties fileStorageProperties, FileService fileService) {
        this.fileService = fileService;
        this.fileSystemService = fileSystemService;
        this.fileStorageProperties = fileStorageProperties;
    }

    @PostMapping("upload")
    public ResponseEntity<?> UploadFile(@RequestParam MultipartFile[] files, @RequestParam long folderid) {
        try {
            if (files.length == 0) throw new NullPointerException();
            String folderPath;
            if (folderid != 0) {
                FolderMetadata parentFolder = fileSystemService.getFolderMetadata(folderid);
                folderPath = parentFolder.getPath();
            } else {
                folderPath = fileStorageProperties.getOnlyUserName();
            }
            List<FileMetadata> fileUpload = fileService.UploadFile(files, folderPath);
            return ResponseEntity.ok().body(fileUpload);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return ResponseEntity.badRequest().body(new JSONResponse("Failed to upload file", false));
    }

    @GetMapping("download")
    public ResponseEntity<?> DownloadFile(@RequestParam long fileid) {
        try {
            FileMetadata metadata = fileSystemService.getFileMetadata(fileid);
            Resource file = fileService.getFile(metadata);
            logger.info("passed controller");
            return ResponseEntity.ok().header(
                    HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"" + metadata.getMimiType() + "\" ")
                    .contentType(MediaType.parseMediaType(metadata.getMimiType())).contentLength(metadata.getSize()).body(file);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResponseEntity.internalServerError().body(new JSONResponse("Failed to download file", false));
        }
    }

    @PostMapping(value = "folder/create", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<JSONResponse> CreateFolder(@RequestBody CreateFolderDTO folderDTO) {
        try {
            fileSystemService.createFolder(folderDTO.getPath());
            return ResponseEntity.ok().body(new JSONResponse(
                    String.format("Folder at path %s was successfully created", folderDTO.getPath()),
                    true));
        } catch (Exception e) {
            logger.error("Error creating folder at path: {}. {}", folderDTO.getPath(),e.getMessage());
            return ResponseEntity.internalServerError().body(new JSONResponse(
                    String.format("Error creating folder at path %s.", folderDTO.getPath()),
                    false));
        }
    }
}
