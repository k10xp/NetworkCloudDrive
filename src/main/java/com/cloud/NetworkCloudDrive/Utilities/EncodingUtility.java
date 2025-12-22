package com.cloud.NetworkCloudDrive.Utilities;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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

    public String[] decodedBase32SplitArray(String base32String) {
        return decodeBase32StringNoPadding(base32String).split(":");
    }

    public String encodeBase32FolderName(long folderId, String folderName, long userId) {
        String encode = folderId + ":" + folderName + ":" + userId;
        return Base64.getUrlEncoder().withoutPadding().encodeToString(encode.getBytes());
    }

    public String encodeBase32FileName(long fileId, String fileName, long userId) {
        return encodeBase32FolderName(fileId, fileName, userId);
    }

    // Hashes
    public String hashString(String originalString, String algorithm) throws NoSuchAlgorithmException {
        // Source - https://stackoverflow.com/a/5531479
        // Posted by Jon Skeet, modified by community. See post 'Timeline' for change history
        // Retrieved 2025-12-22, License - CC BY-SA 4.0
        if (originalString == null)
            throw new NullPointerException("String to hash is null");
        if (algorithm == null)
            throw new NullPointerException("No hash algorithm given");
        MessageDigest digest = MessageDigest.getInstance(algorithm);
        byte[] hash = digest.digest(originalString.getBytes(StandardCharsets.UTF_8));
        return new String(hash);
    }
}
