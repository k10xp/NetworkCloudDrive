package com.cloud.NetworkCloudDrive.Properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Set;

@ConfigurationProperties(prefix = "app.file-storage")
@Component
public class FileStorageProperties {
    private String basePath;
    private String OnlyUserName;

    public FileStorageProperties() {
        this.basePath = "./root";
        this.OnlyUserName = "test_user1";
    }

    public String getOnlyUserName() {
        return OnlyUserName;
    }

    public void setOnlyUserName(String onlyUserName) {
        OnlyUserName = onlyUserName;
    }

    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }
}
