package com.cloud.NetworkCloudDrive.Controllers;

import com.cloud.NetworkCloudDrive.DTOs.CreateFolderDTO;
import com.cloud.NetworkCloudDrive.Models.FileDetails;
import com.cloud.NetworkCloudDrive.Models.JSONResponse;
import com.cloud.NetworkCloudDrive.Services.FileService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/files")
public class FileController {
    private final FileService fileService;

    public FileController(FileService fileService){
        this.fileService = fileService;
    }

    @GetMapping(value = "/dir", produces = MediaType.APPLICATION_JSON_VALUE)
    public FileDetails getFile(@RequestParam long pathId) {
        return fileService.getFile(pathId);
    }

    @PostMapping(value = "/create", produces = MediaType.APPLICATION_JSON_VALUE)
    public JSONResponse CreateFolder(@RequestBody CreateFolderDTO folder) {
        boolean success = fileService.CreateFolder(folder.getPath());
        if (success)
            return new JSONResponse(
                    String.format("Folder at path %s was successfully created", folder.getPath()),
                    "/api/files/create",
                    true);
        else
            return new JSONResponse(
                    String.format("Error creating folder at path %s", folder.getPath()),
                    "/api/files/create",
                    false);
    }
}
