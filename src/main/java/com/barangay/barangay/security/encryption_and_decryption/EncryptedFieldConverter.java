package com.barangay.barangay.security.encryption_and_decryption;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Converter
@Component
public class EncryptedFieldConverter implements AttributeConverter<String, String> {

    private static AesEncryptionService encryptionService;

    @Autowired
    public void setEncryptionService(AesEncryptionService service) {
        EncryptedFieldConverter.encryptionService = service;
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (encryptionService == null) return attribute;
        return encryptionService.encryptSafe(attribute);
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (encryptionService == null) return dbData;
        return encryptionService.decryptSafe(dbData);
    }


}