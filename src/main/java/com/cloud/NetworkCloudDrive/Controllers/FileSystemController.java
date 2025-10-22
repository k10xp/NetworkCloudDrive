package com.cloud.NetworkCloudDrive.Controllers;

import com.cloud.NetworkCloudDrive.DTOs.CreateFolderDTO;
import com.cloud.NetworkCloudDrive.Models.FileMetadata;
import com.cloud.NetworkCloudDrive.Models.JSONResponse;
import com.cloud.NetworkCloudDrive.Services.FileSystemService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.FileNotFoundException;

@RestController
@RequestMapping(path = "/api/filesystem")
public class FileSystemController {
    private final FileSystemService fileSystemService;

    public FileSystemController(FileSystemService fileSystemService) {
        this.fileSystemService = fileSystemService;
    }

    @GetMapping(value = "/get", produces = MediaType.ALL_VALUE)
    public @ResponseBody ResponseEntity<Object> getFile(@RequestParam String name) {
        return ResponseEntity.ok().
                contentType(MediaType.APPLICATION_JSON).
                body(String.format("Not yet implemented test value = %s\n", name));
    }

    @GetMapping(value = "/dir", produces = MediaType.APPLICATION_JSON_VALUE)
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

    @PostMapping(value = "/create", produces = MediaType.APPLICATION_JSON_VALUE)
    public JSONResponse CreateFolder(@RequestBody CreateFolderDTO folder) {
        try {
            fileSystemService.CreateFolder(folder.getPath());
            return new JSONResponse(
                    String.format("Folder at path %s was successfully created", folder.getPath()),
                    "/api/files/create",
                    true);
        } catch (Exception e) {
            return new JSONResponse(
                    String.format("Error creating folder at path %s", folder.getPath()),
                    "/api/files/create",
                    false);
        }
    }
}
