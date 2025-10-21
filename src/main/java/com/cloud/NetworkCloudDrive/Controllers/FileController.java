package com.cloud.NetworkCloudDrive.Controllers;

import com.cloud.NetworkCloudDrive.Models.FileMetadata;
import com.cloud.NetworkCloudDrive.Models.JSONResponse;
import com.cloud.NetworkCloudDrive.Services.IOService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

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
            FileMetadata fileUpload = ioService.UploadImage(file);
            return ResponseEntity.ok().body(fileUpload);
        } catch (Exception e) {
            logger.error("File controller, {}", e.getMessage());
        }
        return ResponseEntity.badRequest().body(new JSONResponse("Failed to upload file", "api/file/upload", false));
    }

}
