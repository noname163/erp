package com.dat.erp.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class CryptoUtilsTest {

    @Test
    void encryptDecrypt_roundTrip() {
        String encrypted = CryptoUtils.encrypt("hello");
        assertThat(encrypted).isNotBlank();
        assertThat(CryptoUtils.decrypt(encrypted)).isEqualTo("hello");
    }

    @Test
    void decrypt_invalidCipher_throwsRuntimeException() {
        assertThrows(RuntimeException.class, () -> CryptoUtils.decrypt("not-base64"));
    }

    @Test
    void hashAndVerifyHash_matches() {
        String hash = CryptoUtils.hash("password");
        assertThat(CryptoUtils.verifyHash("password", hash)).isTrue();
        assertThat(CryptoUtils.verifyHash("wrong", hash)).isFalse();
    }
}

