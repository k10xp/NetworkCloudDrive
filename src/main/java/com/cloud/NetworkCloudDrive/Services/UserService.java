package com.cloud.NetworkCloudDrive.Services;

import com.cloud.NetworkCloudDrive.Enum.UserRole;
import com.cloud.NetworkCloudDrive.Models.User;
import com.cloud.NetworkCloudDrive.Repositories.UserRepository;
import com.cloud.NetworkCloudDrive.DAO.SQLiteDAO;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.SQLException;

@Service
public class UserService implements UserRepository {
    private SQLiteDAO sqLiteDAO;
    private BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder(16);

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
    public boolean registerUser(String name, String mail, String password) {
        User userLogin = new User();
        userLogin.setName(name);
        userLogin.setMail(mail);
        userLogin.setPassword(bCryptPasswordEncoder.encode(password));
        userLogin.setRole(UserRole.GUEST);
        if (sqLiteDAO.checkIfUserExists(userLogin.getName(), userLogin.getMail())) {
            return false;
        }
        sqLiteDAO.saveUser(userLogin);
        return true;
    }

    @Override
    public boolean loginUser(String name, String mail, String password) throws SQLException {
        return bCryptPasswordEncoder.matches(password, sqLiteDAO.findUserWithNameAndMail(name, mail).getPassword());
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
