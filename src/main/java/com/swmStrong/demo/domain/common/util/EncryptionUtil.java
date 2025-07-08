package com.swmStrong.demo.domain.common.util;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.util.DigestUtils;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
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
            //TODO: 에러 핸들링
            throw new RuntimeException(e);
        }
    }

    public static String decrypt(String encryptedText) {
        if (encryptedText == null || encryptedText.isEmpty()) {
            return encryptedText;
        }

        try {
            SecretKey secretKey = getSecretKey();
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);

            byte[] encryptedWithIv = Base64.getDecoder().decode(encryptedText);

            byte[] iv = new byte[GCM_IV_LENGTH];
            System.arraycopy(encryptedWithIv, 0, iv, 0, GCM_IV_LENGTH);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);

            byte[] encrypted = new byte[encryptedWithIv.length - GCM_IV_LENGTH];
            System.arraycopy(encryptedWithIv, GCM_IV_LENGTH, encrypted, 0, encrypted.length);

            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);
            byte[] decrypted = cipher.doFinal(encrypted);

            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
