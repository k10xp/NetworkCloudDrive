package com.cloud.NetworkCloudDrive.Sessions;

import com.cloud.NetworkCloudDrive.DAO.SQLiteDAO;
import com.cloud.NetworkCloudDrive.DTO.UserDetailsDTO;
import com.cloud.NetworkCloudDrive.Enum.UserRole;
import com.cloud.NetworkCloudDrive.Models.UserEntity;
import com.cloud.NetworkCloudDrive.Repositories.SQLiteUserEntityRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.annotation.SessionScope;

import java.security.Principal;
import java.util.Optional;

@SessionScope
@Component
public class UserSession {
    private long id;
    private String name;
    private UserRole role;
    private final SQLiteDAO sqLiteDAO;
    private final Logger logger = LoggerFactory.getLogger(UserSession.class);

    public UserSession(SQLiteDAO sqLiteDAO) {
        this.sqLiteDAO = sqLiteDAO;
    }

    @Transactional
    public UserDetailsDTO initializeUserSessionDetails(Principal principal) throws UsernameNotFoundException {
        UserDetailsDTO userDetailsDTO = sqLiteDAO.getUserIDNameAndRoleByMail(principal.getName());
        this.id = userDetailsDTO.getId();
        this.name = userDetailsDTO.getUsername();
        this.role = userDetailsDTO.getUserRole();
        logger.info("SESSION INFO: ID={} NAME={} ROLE={}", userDetailsDTO.getId(), userDetailsDTO.getUsername(), userDetailsDTO.getUserRole());
        return userDetailsDTO;
    }

    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public UserRole getRole() {
        return role;
    }
    public void setRole(UserRole role) {
        this.role = role;
    }
}
