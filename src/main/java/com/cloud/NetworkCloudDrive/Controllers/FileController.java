package com.cloud.NetworkCloudDrive.Controllers;

import com.cloud.NetworkCloudDrive.Models.FileMetadata;
import com.cloud.NetworkCloudDrive.Models.FolderMetadata;
import com.cloud.NetworkCloudDrive.Models.JSONResponse;
import com.cloud.NetworkCloudDrive.Services.FileSystemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(path = "/api/file")
public class FileController {
    private final FileSystemService fileSystemService;
    private final Logger logger = LoggerFactory.getLogger(FileController.class);

    public FileController(FileSystemService fileSystemService) {
        this.fileSystemService = fileSystemService;
    }

    @PostMapping("upload")
    public ResponseEntity<?> UploadFile(@RequestParam MultipartFile file, @RequestParam long pathId) {
        try {
            logger.info("filename before upload: {}", file.getOriginalFilename());
            FolderMetadata parentFolder = fileSystemService.getFolder(pathId);
            FileMetadata fileUpload = fileSystemService.UploadFile(file, parentFolder);
            return ResponseEntity.ok().body(fileUpload);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return ResponseEntity.badRequest().body(new JSONResponse("Failed to upload file", false));
    }

    @PostMapping("download")
    public ResponseEntity<?> DownloadFile(@RequestParam long fileId) {
        try {
            FileMetadata metadata = fileSystemService.GetFileMetadata(fileId);
            Resource file = fileSystemService.getFile(metadata);
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

}
