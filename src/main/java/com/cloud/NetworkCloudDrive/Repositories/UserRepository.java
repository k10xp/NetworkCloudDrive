package com.cloud.NetworkCloudDrive.Repositories;

import com.cloud.NetworkCloudDrive.Models.User;

import java.sql.SQLException;

public interface UserRepository {
    boolean loginUser(String name, String mail, String password) throws SQLException;
    User registerUser(String name, String mail, String password) throws SecurityException;
    boolean logOutUser();
    User currentUserDetails();
    boolean updatePassword();
    boolean updateMail();
    boolean updateName();
    boolean deleteUser();
    boolean elevateUserPrivileges();
}
