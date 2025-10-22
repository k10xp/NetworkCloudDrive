package com.cloud.NetworkCloudDrive.Controllers;

import com.cloud.NetworkCloudDrive.Models.FileMetadata;
import com.cloud.NetworkCloudDrive.Models.JSONResponse;
import com.cloud.NetworkCloudDrive.Services.IOService;
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
    private final IOService ioService;
    private final Logger logger = LoggerFactory.getLogger(FileController.class);

    public FileController(IOService ioService) {
        this.ioService = ioService;
    }

    @PostMapping("upload")
    public ResponseEntity<?> UploadFile(@RequestParam MultipartFile file) {
        try {
            logger.info("filename before upload: {}", file.getOriginalFilename());
            FileMetadata fileUpload = ioService.UploadFile(file);
            return ResponseEntity.ok().body(fileUpload);
        } catch (Exception e) {
            logger.error("File controller, {}", e.getMessage());
        }
        return ResponseEntity.badRequest().body(new JSONResponse("Failed to upload file", "api/file/upload", false));
    }

    @PostMapping("download")
    public ResponseEntity<?> DownloadFile(@RequestParam long fileId) {
        try {
            FileMetadata metadata = ioService.GetFileMetadata(fileId);
            Resource file = ioService.DownloadFile(fileId);
            return ResponseEntity.ok().header(
                    HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"" + metadata.getMimiType() + "\" ")
                    .contentType(MediaType.parseMediaType(metadata.getMimiType())).contentLength(metadata.getSize()).body(file);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new JSONResponse("Failed to download file", false));
        }
    }

}
