package com.dat.erp.utils;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class JwtUtilsTest {

    @Test
    void generateAndExtractClaims() {
        EnvironmentVariable env = new EnvironmentVariable();
        env.setJwtSecret("01234567890123456789012345678901");
        env.setJwtExpirationMs(60_000);
        JwtUtils jwtUtils = new JwtUtils(env);

        String token = jwtUtils.generateToken("user@example.com", "ACC-1");

        assertThat(jwtUtils.extractEmail(token)).isEqualTo("user@example.com");
        assertThat(jwtUtils.extractAccountCode(token)).isEqualTo("ACC-1");
        assertThat(jwtUtils.validateToken(token, "ACC-1")).isTrue();
        assertThat(jwtUtils.validateToken(token, "ACC-2")).isFalse();
    }

    @Test
    void validateToken_invalidToken_returnsFalse() {
        EnvironmentVariable env = new EnvironmentVariable();
        env.setJwtSecret("01234567890123456789012345678901");
        env.setJwtExpirationMs(60_000);
        JwtUtils jwtUtils = new JwtUtils(env);

        assertThat(jwtUtils.validateToken("not-a-jwt", "ACC-1")).isFalse();
    }

    @Test
    void validateToken_expiredWhenExpirationMsNegative() {
        EnvironmentVariable env = new EnvironmentVariable();
        env.setJwtSecret("01234567890123456789012345678901");
        env.setJwtExpirationMs(-1);
        JwtUtils jwtUtils = new JwtUtils(env);

        String token = jwtUtils.generateToken("user@example.com", "ACC-1");

        assertThat(jwtUtils.validateToken(token, "ACC-1")).isFalse();
    }
}

