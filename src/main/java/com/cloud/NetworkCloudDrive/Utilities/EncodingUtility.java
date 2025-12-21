package com.cloud.NetworkCloudDrive.Utilities;

import org.springframework.stereotype.Component;

import java.util.Base64;

@Component
public class EncodingUtility {

    public EncodingUtility() {}

    public String encodeBase32UserFolderName(long userId, String username, String mail) {
        String encode = userId + ":" + username + ":" + mail;
        return Base64.getUrlEncoder().withoutPadding().encodeToString(encode.getBytes());
    }

    public String decodeBase32StringNoPadding(String base32String) {
        byte[] decodedString = Base64.getUrlDecoder().decode(base32String.getBytes());
        return new String(decodedString);
    }

    public String encodeBase32FolderName(long folderId, String folderName, long userId) {
        String encode = folderId + ":" + folderName + ":" + userId;
        return Base64.getUrlEncoder().withoutPadding().encodeToString(encode.getBytes());
    }

    public String encodeBase32FileName(long fileId, String fileName, long userId) {
        String encode = fileId + ":" + fileName + ":" + userId;
        return Base64.getUrlEncoder().withoutPadding().encodeToString(encode.getBytes());
    }

    // Universal version
    public String encodeBase32(Object... encodeIn) {
        String encodev2 = "";
        for (Object object : encodeIn) {
            encodev2 += object + ":";
        }
        return Base64.getUrlEncoder().withoutPadding().encodeToString(encodev2.getBytes());
    }
}
