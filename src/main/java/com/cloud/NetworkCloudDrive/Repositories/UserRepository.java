package com.cloud.NetworkCloudDrive.Repositories;

import com.cloud.NetworkCloudDrive.DTO.CurrentUserDTO;
import com.cloud.NetworkCloudDrive.Models.UserEntity;

import java.sql.SQLException;

public interface UserRepository {
    boolean loginUser(String name, String mail, String password) throws SQLException;
    UserEntity registerUser(String name, String mail, String password) throws SecurityException;
    CurrentUserDTO currentUserDetails(String mail);
    boolean updatePassword(UserEntity user, String newPassword);
    boolean updateMail(UserEntity user, String newMail);
    boolean updateName(UserEntity user, String newName);
    boolean deleteUser(UserEntity user);
    boolean elevateUserPrivileges();
}
