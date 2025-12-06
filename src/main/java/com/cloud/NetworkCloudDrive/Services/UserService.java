package com.cloud.NetworkCloudDrive.Services;

import com.cloud.NetworkCloudDrive.Enum.UserRole;
import com.cloud.NetworkCloudDrive.Models.User;
import com.cloud.NetworkCloudDrive.Repositories.UserRepository;
import com.cloud.NetworkCloudDrive.DAO.SQLiteDAO;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.SQLException;

@Service
public class UserService implements UserRepository {
    private final SQLiteDAO sqLiteDAO;
    private final PasswordEncoder passwordEncoder;

    public UserService(SQLiteDAO sqLiteDAO, PasswordEncoder passwordEncoder) {
        this.sqLiteDAO = sqLiteDAO;
        this.passwordEncoder = passwordEncoder;
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
    public User registerUser(String name, String mail, String password) throws SecurityException {
        User userLogin = new User();
        userLogin.setName(name);
        userLogin.setMail(mail);
        userLogin.setPassword(passwordEncoder.encode(password));
        userLogin.setRole(UserRole.GUEST);
        if (sqLiteDAO.checkIfUserExists(userLogin.getName(), userLogin.getMail())) {
            throw new SecurityException(String.format("User with name %s and mail %s already exists", name, mail));
        }
        return sqLiteDAO.saveUser(userLogin);
    }

    @Override
    public boolean loginUser(String name, String mail, String password) throws SQLException {
        return passwordEncoder.matches(password, sqLiteDAO.findUserWithNameAndMail(name, mail).getPassword());
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
