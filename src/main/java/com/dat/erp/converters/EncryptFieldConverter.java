package com.dat.erp.converters;

import com.dat.erp.constants.EncryptionMode;

import jakarta.persistence.Converter;

@Converter
public class EncryptFieldConverter extends EncryptConverter {
    public EncryptFieldConverter() {
        super(EncryptionMode.ENCRYPT);
    }
}
