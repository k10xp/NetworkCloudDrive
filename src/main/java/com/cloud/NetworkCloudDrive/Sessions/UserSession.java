package com.cloud.NetworkCloudDrive.Sessions;

import com.cloud.NetworkCloudDrive.DAO.SQLiteDAO;
import com.cloud.NetworkCloudDrive.DTO.CurrentUserDTO;
import com.cloud.NetworkCloudDrive.Enum.UserRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.annotation.SessionScope;

import java.security.Principal;

@SessionScope
@Component
public class UserSession {
    private long id;
    private String name;
    private String mail;
    private UserRole role;
    private final SQLiteDAO sqLiteDAO;
    private final Logger logger = LoggerFactory.getLogger(UserSession.class);

    public UserSession(SQLiteDAO sqLiteDAO) {
        this.sqLiteDAO = sqLiteDAO;
    }

    @Transactional
    public CurrentUserDTO initializeUserSessionDetails(Principal principal) throws UsernameNotFoundException {
        CurrentUserDTO userDetailsDTO = sqLiteDAO.getUserIDNameAndRoleByMail(principal.getName());
        this.id = userDetailsDTO.getId();
        this.name = userDetailsDTO.getName();
        this.mail = userDetailsDTO.getMail();
        this.role = userDetailsDTO.getRole();
        logger.info("SESSION INFO: ID={} NAME={} MAIL={} ROLE={}",
                userDetailsDTO.getId(), userDetailsDTO.getName(), userDetailsDTO.getMail(), userDetailsDTO.getRole());
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
    public String getMail() {
        return mail;
    }
    public void setMail(String mail) {
        this.mail = mail;
    }
    public UserRole getRole() {
        return role;
    }
    public void setRole(UserRole role) {
        this.role = role;
    }
}
