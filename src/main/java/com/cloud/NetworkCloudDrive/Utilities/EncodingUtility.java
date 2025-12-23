package com.cloud.NetworkCloudDrive.Utilities;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Component
public class EncodingUtility {

    public EncodingUtility() {}

    /**
     * Encode User folder using BASE32
     * @param userId    currently logged-in user's ID
     * @param username  currently logged-in user's name
     * @param mail  currently logged-in user's mail
     * @return  BASE32 encoded user folder name
     */
    public String encodeBase32UserFolderName(long userId, String username, String mail) {
        String encode = userId + ":" + username + ":" + mail;
        return Base64.getUrlEncoder().withoutPadding().encodeToString(encode.getBytes());
    }

    /**
     * Decode BASE32 encoded file/folder names
     * @param base32String  BASE32 encoded file/folder name
     * @return  decoded BASE32 string
     */
    public String decodeBase32StringNoPadding(String base32String) {
        byte[] decodedString = Base64.getUrlDecoder().decode(base32String.getBytes());
        return new String(decodedString);
    }

    /**
     * Decode BASE32 encoded file/folder names
     * @param base32String  BASE32 encoded file/folder name
     * @return  decoded BASE32 string split by ":"
     */
    public String[] decodedBase32SplitArray(String base32String) {
        return decodeBase32StringNoPadding(base32String).split(":");
    }

    /**
     * Encode Folder names in BASE32
     * @param folderId  folder's ID
     * @param folderName    folder's name
     * @param userId    currently logged-in user's ID
     * @return  BASE32 encoded Folder name
     */
    public String encodeBase32FolderName(long folderId, String folderName, long userId) {
        String encode = folderId + ":" + folderName + ":" + userId;
        return Base64.getUrlEncoder().withoutPadding().encodeToString(encode.getBytes());
    }

    /**
     * Encode File names in BASE32
     * @param fileId    file's ID
     * @param fileName  file's name
     * @param userId    currently logged-in user's ID
     * @return  BASE32 encoded File name
     */
    public String encodeBase32FileName(long fileId, String fileName, long userId) {
        return encodeBase32FolderName(fileId, fileName, userId);
    }

    // Hashes
    /**
     * Hashes string in given algorithms
     * @param originalString    string to hash
     * @param algorithm algorithm of hash (Ex. SHA-256, SHA-512)
     * @return  hashed string
     * @throws NoSuchAlgorithmException Invalid algorithm
     */
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
