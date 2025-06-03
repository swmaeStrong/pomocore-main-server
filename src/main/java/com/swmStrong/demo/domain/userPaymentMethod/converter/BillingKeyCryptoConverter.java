package com.swmStrong.demo.domain.userPaymentMethod.converter;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.jasypt.util.text.AES256TextEncryptor;

@Converter
public class BillingKeyCryptoConverter implements AttributeConverter<String, String> {

    private final AES256TextEncryptor encryptor;

    public BillingKeyCryptoConverter(
            AES256TextEncryptor encryptor
    ) {
        this.encryptor = encryptor;
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
