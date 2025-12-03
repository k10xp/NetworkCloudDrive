package com.cloud.NetworkCloudDrive.Repositories;

import com.cloud.NetworkCloudDrive.Models.User;

public interface UserRepository {
    boolean loginUser();
    boolean registerUser();
    String generateToken();
    boolean logOutUser();
    User currentUserDetails();
    boolean updatePassword();
    boolean updateMail();
    boolean updateName();
    boolean deleteUser();
    boolean elevateUserPrivileges();
}
