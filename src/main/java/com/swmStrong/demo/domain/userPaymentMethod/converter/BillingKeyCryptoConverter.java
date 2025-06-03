package com.swmStrong.demo.domain.userPaymentMethod.converter;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.jasypt.util.text.AES256TextEncryptor;

@Converter
public class BillingKeyCryptoConverter implements AttributeConverter<String, String> {
    private static final AES256TextEncryptor encryptor = new AES256TextEncryptor();

    static {
        String key = System.getenv("BILLINGKEY_ENCRYPT_KEY"); // 환경변수에서 가져오기
        encryptor.setPassword(key);
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        return encryptor.encrypt(attribute);
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        return encryptor.decrypt(dbData);
    }
}
