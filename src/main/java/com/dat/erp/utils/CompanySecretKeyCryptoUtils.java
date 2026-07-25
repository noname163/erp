package com.dat.erp.utils;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public final class CompanySecretKeyCryptoUtils {
    private static final int GCM_IV_LENGTH_BYTES = 12;
    private static final int GCM_TAG_LENGTH_BITS = 128;

    private static final SecureRandom secureRandom = new SecureRandom();

    private CompanySecretKeyCryptoUtils() {
    }

    public static String encrypt(String plainText, String companySecretKey) {
        if (plainText == null) {
            return null;
        }
        if (companySecretKey == null || companySecretKey.isBlank()) {
            throw new IllegalArgumentException("companySecretKey is invalid");
        }
        try {
            byte[] iv = new byte[GCM_IV_LENGTH_BYTES];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, deriveAesKey(companySecretKey),
                    new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));

            byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            byte[] packed = ByteBuffer.allocate(iv.length + cipherText.length).put(iv).put(cipherText).array();
            return Base64.getEncoder().encodeToString(packed);
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed", e);
        }
    }

    public static String decrypt(String encryptedBase64, String companySecretKey) {
        if (encryptedBase64 == null) {
            return null;
        }
        if (companySecretKey == null || companySecretKey.isBlank()) {
            throw new IllegalArgumentException("companySecretKey is invalid");
        }
        try {
            byte[] packed = Base64.getDecoder().decode(encryptedBase64);
            if (packed.length < GCM_IV_LENGTH_BYTES + 1) {
                throw new IllegalArgumentException("cipherText is invalid");
            }

            byte[] iv = new byte[GCM_IV_LENGTH_BYTES];
            byte[] cipherText = new byte[packed.length - GCM_IV_LENGTH_BYTES];
            System.arraycopy(packed, 0, iv, 0, GCM_IV_LENGTH_BYTES);
            System.arraycopy(packed, GCM_IV_LENGTH_BYTES, cipherText, 0, cipherText.length);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, deriveAesKey(companySecretKey),
                    new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));

            byte[] plain = cipher.doFinal(cipherText);
            return new String(plain, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Decryption failed", e);
        }
    }

    public static BigDecimal decryptAmount(String encryptedAmount, String companySecretKey) {
        if (encryptedAmount == null || encryptedAmount.isBlank()) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(CompanySecretKeyCryptoUtils.decrypt(encryptedAmount, companySecretKey));
    }

    private static SecretKey deriveAesKey(String companySecretKey) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(companySecretKey.getBytes(StandardCharsets.UTF_8));
            return new SecretKeySpec(hash, 0, 16, "AES"); // 128-bit key
        } catch (Exception e) {
            throw new RuntimeException("Key derivation failed", e);
        }
    }
}
