package com.cloud.NetworkCloudDrive.DTO;

import com.cloud.NetworkCloudDrive.Enum.UserRole;

public class UserDetailsDTO {
    private long id;
    private String username;
    private UserRole userRole;

    public UserDetailsDTO(long id, String username, UserRole userRole) {
        this.id = id;
        this.username = username;
        this.userRole = userRole;
    }

    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public UserRole getUserRole() {
        return userRole;
    }
    public void setUserRole(UserRole userRole) {
        this.userRole = userRole;
    }
}
