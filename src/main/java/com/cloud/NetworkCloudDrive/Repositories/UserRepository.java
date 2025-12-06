package com.cloud.NetworkCloudDrive.Repositories;

import com.cloud.NetworkCloudDrive.Models.UserEntity;

import java.sql.SQLException;

public interface UserRepository {
    boolean loginUser(String name, String mail, String password) throws SQLException;
    UserEntity registerUser(String name, String mail, String password) throws SecurityException;
    boolean logOutUser();
    UserEntity currentUserDetails();
    boolean updatePassword();
    boolean updateMail();
    boolean updateName();
    boolean deleteUser();
    boolean elevateUserPrivileges();
}
