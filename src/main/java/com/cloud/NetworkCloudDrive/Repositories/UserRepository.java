package com.cloud.NetworkCloudDrive.Repositories;

import com.cloud.NetworkCloudDrive.Models.User;

import java.sql.SQLException;

public interface UserRepository {
    boolean loginUser(String name, String mail, String password);
    boolean registerUser(String name, String mail, String password) throws SQLException;
    String generateToken();
    boolean logOutUser();
    User currentUserDetails();
    boolean updatePassword();
    boolean updateMail();
    boolean updateName();
    boolean deleteUser();
    boolean elevateUserPrivileges();
}
