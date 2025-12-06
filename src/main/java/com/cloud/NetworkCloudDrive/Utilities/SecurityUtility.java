package com.cloud.NetworkCloudDrive.Utilities;

import com.cloud.NetworkCloudDrive.DAO.SQLiteDAO;
import com.cloud.NetworkCloudDrive.Enum.UserRole;
import com.cloud.NetworkCloudDrive.Models.UserEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class SecurityUtility implements UserDetailsService {
    private final SQLiteDAO sqLiteDAO;
    private final Logger logger = LoggerFactory.getLogger(SecurityUtility.class);

    public SecurityUtility(SQLiteDAO sqLiteDAO) {
        this.sqLiteDAO = sqLiteDAO;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity returnedUser = sqLiteDAO.findUserByName(username);
        logger.info("Built user:\nid={}\nname={}\nmail={}\nrole={}\nEND",
                returnedUser.getId(),returnedUser.getName(), returnedUser.getMail(), getUserRole(returnedUser.getRole()));
        return User.builder().
                username(returnedUser.getName()).
                password(returnedUser.getPassword()).
                roles(getUserRole(returnedUser.getRole())).
                build();
    }

    private String getUserRole(UserRole userRole) {
        switch (userRole) {
            case GUEST -> {
                return "GUEST";
            }
            case NORMAL_USER -> {
                return "USER";
            }
            case ADMIN -> {
                return "ADMIN";
            }
        }
        return "GUEST";
    }
}
