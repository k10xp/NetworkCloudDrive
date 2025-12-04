package com.cloud.NetworkCloudDrive.Controllers;

import com.cloud.NetworkCloudDrive.DTO.UserDTO;
import com.cloud.NetworkCloudDrive.Services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping(path = "/api/user")
public class UserController {
    private final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Temporary endpoint
    @PostMapping("login")
    public @ResponseBody ResponseEntity<?> login(@RequestBody UserDTO userDTO) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).
                body(Map.of("message", "login in progress...",
                        "username", userDTO.getName(),
                        "mail", userDTO.getMail(),
                        "passwordHehe", userDTO.getPassword()));
    }

    // Temporary endpoint
    @PostMapping("register")
    public @ResponseBody ResponseEntity<?> register(@RequestBody UserDTO userDTO) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).
                body(Map.of("message", "register in progress...",
                        "username", userDTO.getName(),
                        "mail", userDTO.getMail(),
                        "passwordHehe", userDTO.getPassword()));
    }
}
