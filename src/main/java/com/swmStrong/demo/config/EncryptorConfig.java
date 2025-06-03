package com.swmStrong.demo.config;

import org.jasypt.util.text.AES256TextEncryptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EncryptorConfig {

    @Value("${encrypt.BILLINGKEY_ENCRYPT_KEY}")
    private String key;

    @Bean
    public AES256TextEncryptor encryptor() {
        AES256TextEncryptor encryptor = new AES256TextEncryptor();
        encryptor.setPassword(key);
        return encryptor;
    }
}
