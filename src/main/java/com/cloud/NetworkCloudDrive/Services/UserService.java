package com.cloud.NetworkCloudDrive.Services;

import com.cloud.NetworkCloudDrive.DTO.CurrentUserDTO;
import com.cloud.NetworkCloudDrive.Enum.UserRole;
import com.cloud.NetworkCloudDrive.Models.UserEntity;
import com.cloud.NetworkCloudDrive.Repositories.UserRepository;
import com.cloud.NetworkCloudDrive.DAO.SQLiteDAO;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    public CurrentUserDTO currentUserDetails(String mail) {
        UserEntity user = sqLiteDAO.findUserByMail(mail);
        return new CurrentUserDTO(user.getId(), user.getName(), user.getMail(), user.getRole(), user.getLastLogin());
    }

    @Override
    public UserEntity registerUser(String name, String mail, String password) throws SecurityException {
        UserEntity userEntityLogin = new UserEntity();
        userEntityLogin.setName(name);
        userEntityLogin.setMail(mail);
        userEntityLogin.setPassword(passwordEncoder.encode(password));
        userEntityLogin.setRole(UserRole.GUEST);
        if (sqLiteDAO.checkIfUserExistsByMail(userEntityLogin.getMail())) {
            throw new SecurityException(String.format("User with name %s and mail %s already exists", name, mail));
        }
        return sqLiteDAO.saveUser(userEntityLogin);
    }

    @Override
    public boolean loginUser(String name, String mail, String password) throws SQLException {
        return passwordEncoder.matches(password, sqLiteDAO.findUserByMail(mail).getPassword());
    }

    @Override
    public boolean updatePassword(UserEntity user, String newPassword) {
        user.setPassword(newPassword);
        sqLiteDAO.saveUser(user);
        return true;
    }

    @Override
    public boolean updateName(UserEntity user, String newName) {
        user.setName(newName);
        sqLiteDAO.saveUser(user);
        return true;
    }

    @Override
    public boolean updateMail(UserEntity user, String newMail) {
        user.setMail(newMail);
        sqLiteDAO.saveUser(user);
        return true;
    }

    @Override
    public boolean deleteUser(UserEntity user) {
        sqLiteDAO.deleteUser(user);
        return true;
    }

    @Override
    public boolean elevateUserPrivileges() {
        return false;
    }
}
