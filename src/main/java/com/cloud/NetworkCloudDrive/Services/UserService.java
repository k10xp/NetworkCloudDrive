package com.cloud.NetworkCloudDrive.Services;

import com.cloud.NetworkCloudDrive.Enum.UserRole;
import com.cloud.NetworkCloudDrive.Models.User;
import com.cloud.NetworkCloudDrive.Repositories.UserRepository;
import com.cloud.NetworkCloudDrive.DAO.SQLiteDAO;
import org.springframework.stereotype.Service;

import java.sql.SQLException;

@Service
public class UserService implements UserRepository {
    private SQLiteDAO sqLiteDAO;

    public UserService(SQLiteDAO sqLiteDAO) {
        this.sqLiteDAO = sqLiteDAO;
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
    public boolean registerUser(String name, String mail, String password) throws SQLException {
        User userLogin = new User();
        userLogin.setName(name);
        userLogin.setMail(mail);
        userLogin.setPassword(password);
        userLogin.setRole(UserRole.GUEST);
        if (sqLiteDAO.checkIfUserExists(userLogin.getName(), userLogin.getMail())) {
            throw new SQLException(String.format("User with name %s and mail %s", userLogin.getName(), userLogin.getMail()));
        }
        sqLiteDAO.saveUser(userLogin);
        return true;
    }

    @Override
    public boolean loginUser(String name, String mail, String password) {
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
    public boolean elevateUserPrivileges() {
        return false;
    }
}
