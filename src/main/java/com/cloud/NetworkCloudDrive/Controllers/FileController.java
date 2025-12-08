package com.cloud.NetworkCloudDrive.Controllers;

import com.cloud.NetworkCloudDrive.DAO.SQLiteDAO;
import com.cloud.NetworkCloudDrive.DTO.CreateFolderDTO;
import com.cloud.NetworkCloudDrive.DTO.UserDetailsDTO;
import com.cloud.NetworkCloudDrive.Models.*;
import com.cloud.NetworkCloudDrive.DTO.UserDetailsDTO;
import com.cloud.NetworkCloudDrive.Services.FileService;
import com.cloud.NetworkCloudDrive.Services.FileSystemService;
import com.cloud.NetworkCloudDrive.Services.InformationService;
import com.cloud.NetworkCloudDrive.Utilities.FileUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.FileSystemException;
import java.security.Principal;

@RestController
@RequestMapping(path = "/api/file")
public class FileController {
    private final FileSystemService fileSystemService;
    private final FileService fileService;
    private final InformationService informationService;
    private final FileUtility fileUtility;
    private final SQLiteDAO sqLiteDAO;
    private final Logger logger = LoggerFactory.getLogger(FileController.class);

    public FileController(
            FileSystemService fileSystemService,
            FileService fileService,
            InformationService informationService,
            SQLiteDAO sqLiteDAO,
            FileUtility fileUtility) {
        this.fileService = fileService;
        this.fileSystemService = fileSystemService;
        this.informationService = informationService;
        this.fileUtility = fileUtility;
        this.sqLiteDAO = sqLiteDAO;
    }

    @PostMapping("upload")
    public ResponseEntity<?> uploadFile(@RequestParam MultipartFile[] files, @RequestParam long folderid, Principal principal) {
        try {
            if (files.length == 0) throw new NullPointerException();
            UserDetailsDTO userDetailsDTO = sqLiteDAO.getUserIDNameAndRoleByMail(principal.getName());
            String folderPath = fileUtility.getFolderPath(folderid, userDetailsDTO.getUsername());
            return ResponseEntity.ok().body(fileService.uploadFiles(files, folderPath, folderid));
        } catch (Exception e) {
            logger.error("Failed to upload file. {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    new JSONErrorResponse("Failed to upload file",e.getClass().getName(), false));
        }
    }

    @GetMapping("download")
    public ResponseEntity<?> downloadFile(@RequestParam long fileid, Principal principal) {
        try {
            UserDetailsDTO userDetailsDTO = sqLiteDAO.getUserIDNameAndRoleByMail(principal.getName());
            FileMetadata metadata = informationService.getFileMetadata(fileid);
            String actualPath = fileUtility.getFolderPath(metadata.getFolderId(), userDetailsDTO.getUsername());
            logger.info("path requested {}", actualPath);
            Resource file = fileService.getFile(metadata, actualPath);
            return ResponseEntity.ok().
                    header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + metadata.getName() + "\" "). //return filename
                            contentType(MediaType.parseMediaType(metadata.getMimiType())).
                    contentLength(metadata.getSize()).body(file);
        }
        catch (FileSystemException fse) {
            logger.error("Internal error occurred. {}", fse.getMessage());
            return ResponseEntity.internalServerError().body(new JSONResponse("Internal error occurred", false));
        }
        catch (Exception e) {
            logger.error("Failed to download file. {}", e.getMessage());
            return ResponseEntity.internalServerError().body(
                    new JSONErrorResponse("Failed to download file",e.getClass().getName(), false));
        }
    }

    /*
    TODO Security hole when sending a folder with name "../hello" creates above directory. Plus allows you to save files there.
    Can go further and do more later on...
    TODO "/../hello" bypasses as well. Another way to go above is to use "../../hello" which goes to $HOME/IdeaProjects/NetworkCloudDrive
    TODO Fix is to Path.normalize then relativize to check if path is above or below the directory
     */
    @PostMapping(value = "create/folder", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createFolder(@RequestBody CreateFolderDTO folderDTO, Principal principal) {
        try {
            UserDetailsDTO userDetailsDTO = sqLiteDAO.getUserIDNameAndRoleByMail(principal.getName());
            FolderMetadata folderMetadata =
                    fileSystemService.createFolder(
                            folderDTO.getName(), folderDTO.getFolder_id(),
                            userDetailsDTO);
            folderMetadata.setPath(fileUtility.resolvePathFromIdString(folderMetadata.getPath(), userDetailsDTO.getUsername()));
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(folderMetadata);
        } catch (Exception e) {
            logger.error("Error creating folder with name: {}. {}", folderDTO.getName(), e.getMessage());
            return ResponseEntity.internalServerError().body(new JSONErrorResponse(e.getMessage(),e.getClass().getName(), false));
        }
    }
}
