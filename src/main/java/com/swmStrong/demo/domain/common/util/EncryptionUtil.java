package com.swmStrong.demo.domain.common.util;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.util.DigestUtils;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;


public class EncryptionUtil {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 16;

    @Value("${encrypt.secretKey}")
    private static String secretKeyString;

    @Value("${encrypt.salt}")
    private static String salt;

    private static SecretKey getSecretKey() {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] key = digest.digest((secretKeyString + salt).getBytes(StandardCharsets.UTF_8));
            return new SecretKeySpec(key, ALGORITHM);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate SHA-256 key", e);
        }
    }

    private static SecretKey getLegacySecretKey() {
        byte[] key = DigestUtils.md5Digest((secretKeyString + salt).getBytes());
        byte[] expandedKey = new byte[32];
        System.arraycopy(key, 0, expandedKey, 0, 16);
        System.arraycopy(key, 0, expandedKey, 16, 16);
        return new SecretKeySpec(expandedKey, ALGORITHM);
    }

    public static String encrypt(String plainText) {
        if (plainText == null || plainText.isEmpty()) {
            return plainText;
        }

        try {
            SecretKey secretKey = getSecretKey();
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            // IV 생성
            byte[] iv = new byte[GCM_IV_LENGTH];
            new SecureRandom().nextBytes(iv);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);

            // 암호화
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            // IV + 암호문 결합
            byte[] encryptedWithIv = new byte[GCM_IV_LENGTH + encrypted.length];
            System.arraycopy(iv, 0, encryptedWithIv, 0, GCM_IV_LENGTH);
            System.arraycopy(encrypted, 0, encryptedWithIv, GCM_IV_LENGTH, encrypted.length);
            return Base64.getEncoder().encodeToString(encryptedWithIv);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String reencryptIfNeeded(String data) {
        if (data == null || data.isEmpty()) {
            return data;
        }

        try {
            byte[] encryptedWithIv = Base64.getDecoder().decode(data);
            
            // Validate minimum length
            if (encryptedWithIv.length < GCM_IV_LENGTH + GCM_TAG_LENGTH) {
                // Data is not encrypted, encrypt it
                return encrypt(data);
            }

            // Try to decrypt with new key
            try {
                decryptWithKey(encryptedWithIv, getSecretKey());
                // If successful, data is already encrypted with new key
                return data;
            } catch (Exception e) {
                // Try to decrypt with legacy key and re-encrypt with new key
                try {
                    String decrypted = decryptWithKey(encryptedWithIv, getLegacySecretKey());
                    return encrypt(decrypted);
                } catch (Exception legacyException) {
                    // Data is not encrypted, encrypt it
                    return encrypt(data);
                }
            }
        } catch (Exception e) {
            // If base64 decode fails, data is not encrypted
            return encrypt(data);
        }
    }

    public static String decrypt(String encryptedText) {
        if (encryptedText == null || encryptedText.isEmpty()) {
            return encryptedText;
        }

        try {
            byte[] encryptedWithIv = Base64.getDecoder().decode(encryptedText);
            
            // Validate minimum length for IV + encrypted data + tag
            if (encryptedWithIv.length < GCM_IV_LENGTH + GCM_TAG_LENGTH) {
                throw new IllegalArgumentException("Encrypted data is too short");
            }

            // Try new SHA256 key first
            try {
                return decryptWithKey(encryptedWithIv, getSecretKey());
            } catch (Exception e) {
                // If SHA256 fails, try legacy MD5 key for backward compatibility
                try {
                    return decryptWithKey(encryptedWithIv, getLegacySecretKey());
                } catch (Exception legacyException) {
                    throw new RuntimeException("Failed to decrypt with both new and legacy keys", e);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String decryptWithKey(byte[] encryptedWithIv, SecretKey secretKey) throws Exception {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);

        byte[] iv = new byte[GCM_IV_LENGTH];
        System.arraycopy(encryptedWithIv, 0, iv, 0, GCM_IV_LENGTH);
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);

        byte[] encrypted = new byte[encryptedWithIv.length - GCM_IV_LENGTH];
        System.arraycopy(encryptedWithIv, GCM_IV_LENGTH, encrypted, 0, encrypted.length);

        cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);
        byte[] decrypted = cipher.doFinal(encrypted);

        return new String(decrypted, StandardCharsets.UTF_8);
    }


}
