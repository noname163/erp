package com.dat.erp.services.impl;

import java.security.SecureRandom;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.dat.erp.services.PasswordGenerator;

@Service
public class SecurePasswordGenerator implements PasswordGenerator {
    private static final char[] DEFAULT_ALPHABET = ("ABCDEFGHJKLMNPQRSTUVWXYZ"
            + "abcdefghijkmnopqrstuvwxyz"
            + "23456789"
            + "!@#$%&*+-_").toCharArray();

    private final SecureRandom secureRandom = new SecureRandom();
    private final int length;

    public SecurePasswordGenerator(@Value("${security.defaultPassword.length:12}") int length) {
        this.length = Math.max(8, length);
    }

    @Override
    public String generate() {
        char[] out = new char[length];
        for (int i = 0; i < length; i++) {
            out[i] = DEFAULT_ALPHABET[secureRandom.nextInt(DEFAULT_ALPHABET.length)];
        }
        return new String(out);
    }
}

