package com.dat.erp.utils;

import java.security.SecureRandom;
import java.util.UUID;

public final class UuidV7 {

    private static final SecureRandom RANDOM = new SecureRandom();

    private UuidV7() {
    }

    public static String generate() {
        return generateUuid().toString();
    }

    public static UUID generateUuid() {
        long timestampMillis = System.currentTimeMillis();
        byte[] randomBytes = new byte[10];
        RANDOM.nextBytes(randomBytes);

        long randomA = ((randomBytes[0] & 0x0FL) << 8)
                | (randomBytes[1] & 0xFFL);
        long mostSignificantBits = ((timestampMillis & 0xFFFFFFFFFFFFL) << 16)
                | 0x7000L
                | randomA;

        long leastSignificantBits = 0L;
        for (int i = 2; i < randomBytes.length; i++) {
            leastSignificantBits |= (randomBytes[i] & 0xFFL) << ((9 - i) * 8);
        }
        leastSignificantBits &= 0x3FFFFFFFFFFFFFFFL;
        leastSignificantBits |= 0x8000000000000000L;

        return new UUID(mostSignificantBits, leastSignificantBits);
    }
}
