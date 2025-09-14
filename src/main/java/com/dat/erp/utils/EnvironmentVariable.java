package com.dat.erp.utils;

import lombok.Getter;

@Getter
public class EnvironmentVariable {
    private final String jwtSecret = System.getenv("JWT_SECRET");
    private final long jwtExpirationMs = Long.parseLong(System.getenv("JWT_EXPIRATION_MS"));
}
