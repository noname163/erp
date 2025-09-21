package com.dat.erp.converters;

import com.dat.erp.constants.EncryptionMode;
import com.dat.erp.utils.CryptoUtils;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class EncryptConverter implements AttributeConverter<String, String> {

    private final EncryptionMode mode;

    // Default constructor required by JPA
    public EncryptConverter() {
        this.mode = EncryptionMode.ENCRYPT;
    }

    // Custom constructor if you want different mode
    public EncryptConverter(EncryptionMode mode) {
        this.mode = mode;
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null)
            return null;

        return switch (mode) {
            case ENCRYPT -> CryptoUtils.encrypt(attribute);
            case HASH -> CryptoUtils.hash(attribute);
        };
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null)
            return null;

        return switch (mode) {
            case ENCRYPT -> CryptoUtils.decrypt(dbData);
            case HASH -> dbData; // cannot be decrypted
        };
    }
}
