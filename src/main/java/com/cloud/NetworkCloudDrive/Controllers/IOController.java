package com.cloud.NetworkCloudDrive.Controllers;

import com.cloud.NetworkCloudDrive.DTOs.CreateFolderDTO;
import com.cloud.NetworkCloudDrive.Models.FileMetadata;
import com.cloud.NetworkCloudDrive.Models.JSONResponse;
import com.cloud.NetworkCloudDrive.Services.IOService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/io")
public class IOController {
    private final IOService ioService;

    public IOController(IOService fileService) {
        this.ioService = fileService;
    }

    @GetMapping(value = "/get", produces = MediaType.ALL_VALUE)
    public @ResponseBody ResponseEntity<Object> getFile(@RequestParam String name) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body("Not yet implemented test value = " + name);
    }

    @GetMapping(value = "/dir", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody ResponseEntity<Object> getFileDetails(@RequestParam long pathId) {
        FileMetadata file = ioService.getFileDetails(pathId);
        if (file == null) {
            return ResponseEntity.badRequest().
                    contentType(MediaType.APPLICATION_JSON).
                    body(new JSONResponse("File does not exist", false));
        }
        return ResponseEntity.ok().
                contentType(MediaType.APPLICATION_JSON).
                body(file);
    }

    @PostMapping(value = "/create", produces = MediaType.APPLICATION_JSON_VALUE)
    public JSONResponse CreateFolder(@RequestBody CreateFolderDTO folder) {
        boolean success = ioService.CreateFolder(folder.getPath());
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
