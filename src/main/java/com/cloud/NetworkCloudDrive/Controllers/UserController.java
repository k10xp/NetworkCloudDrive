package com.cloud.NetworkCloudDrive.Controllers;

import com.cloud.NetworkCloudDrive.DTO.UserDTO;
import com.cloud.NetworkCloudDrive.Models.JSONErrorResponse;
import com.cloud.NetworkCloudDrive.Models.JSONMapResponse;
import com.cloud.NetworkCloudDrive.Models.JSONObjectResponse;
import com.cloud.NetworkCloudDrive.Models.UserEntity;
import com.cloud.NetworkCloudDrive.Services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping(path = "/api/user")
public class UserController {
    private final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    //TODO replace with 2 endpoints, one for failure to login and other for success.
    // On success initialize SessionScope user details and update lastlogin
    @PostMapping("login")
    public @ResponseBody ResponseEntity<?> login(@RequestBody UserDTO userDTO) {
        try {
            if (!userService.loginUser(userDTO.getName(), userDTO.getMail(), userDTO.getPassword())) {
                throw new SecurityException("Wrong password");
            }
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).
                    body(new JSONMapResponse("User logged in successfully", true,
                            Map.of("username", userDTO.getName(), "mail", userDTO.getMail())));
        } catch (Exception e) {
            logger.error("Failed to login, reason: {}", e.getMessage());
            return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON).
                    body(new JSONErrorResponse("Failed to login, reason: " + e.getMessage(), e.getClass().getName(), false));
        }
    }

    @PostMapping("register")
    public @ResponseBody ResponseEntity<?> register(@RequestBody UserDTO userDTO) {
        try {
            UserEntity registeredUserEntity = userService.registerUser(userDTO.getName(), userDTO.getMail(), userDTO.getPassword());
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).
                    body(new JSONMapResponse("User successfully registered",
                            true,
                            Map.of(
                            "id", registeredUserEntity.getId(),
                            "username", registeredUserEntity.getName(),
                            "mail", registeredUserEntity.getMail(),
                            "role", registeredUserEntity.getRole(),
                            "registeredAt", registeredUserEntity.getRegisteredAt()
                            )));
        } catch (SecurityException e) {
            logger.error("Failed to register user reason: {}", e.getMessage());
            return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON).
                    body(new JSONErrorResponse(
                            "Failed to register user, reason: " + e.getMessage(), e.getClass().getName(), false));
        }
    }

    @GetMapping("info")
    public @ResponseBody ResponseEntity<?> info(Principal principal) {
        try {
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).
                    body(new JSONObjectResponse(
                            "Currently authenticated user info", userService.currentUserDetails(principal.getName()),
                            true));
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON).
                    body(new JSONErrorResponse(
                            "Failed to get user details, reason: " + e.getMessage(), e.getClass().getName(), false));
        }
    }
}
