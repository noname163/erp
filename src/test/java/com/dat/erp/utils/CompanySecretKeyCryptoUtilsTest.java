package com.dat.erp.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class CompanySecretKeyCryptoUtilsTest {

    @Test
    void encryptDecrypt_roundTrip() {
        String secretKey = "company-secret-key";
        String plain = "20000000";

        String encrypted = CompanySecretKeyCryptoUtils.encrypt(plain, secretKey);
        String decrypted = CompanySecretKeyCryptoUtils.decrypt(encrypted, secretKey);

        assertNotEquals(plain, encrypted);
        assertEquals(plain, decrypted);
    }

    @Test
    void decrypt_throwsWhenWrongKey() {
        String encrypted = CompanySecretKeyCryptoUtils.encrypt("123", "key-1");
        assertThrows(RuntimeException.class, () -> CompanySecretKeyCryptoUtils.decrypt(encrypted, "key-2"));
    }
}

