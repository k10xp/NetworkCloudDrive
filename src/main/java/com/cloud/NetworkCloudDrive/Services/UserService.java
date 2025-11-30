package com.cloud.NetworkCloudDrive.Services;

import com.cloud.NetworkCloudDrive.Models.User;
import com.cloud.NetworkCloudDrive.Repositories.UserRepository;
import com.cloud.NetworkCloudDrive.Utilities.QueryUtility;
import org.springframework.stereotype.Service;

@Service
public class UserService implements UserRepository {
    private QueryUtility queryUtility;

    public UserService(QueryUtility queryUtility) {
        this.queryUtility = queryUtility;
    }

    @Override
    public User currentUserDetails() {
        return null;
    }

    @Override
    public boolean logOutUser() {
        return false;
    }

    @Override
    public String generateToken() {
        return "";
    }

    @Override
    public boolean registerUser() {
        return false;
    }

    @Override
    public boolean loginUser() {
        return false;
    }

    @Override
    public boolean updatePassword() {
        return false;
    }

    @Override
    public boolean updateName() {
        return false;
    }

    @Override
    public boolean updateMail() {
        return false;
    }

    @Override
    public boolean deleteUser() {
        return false;
    }

    @Override
    public boolean elevateUserToAdmin() {
        return false;
    }
}
