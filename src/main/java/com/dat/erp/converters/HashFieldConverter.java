package com.dat.erp.converters;

import com.dat.erp.constants.EncryptionMode;

import jakarta.persistence.Converter;

@Converter
public class HashFieldConverter extends EncryptConverter {
    public HashFieldConverter() {
        super(EncryptionMode.HASH);
    }
}
