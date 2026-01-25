package com.dat.erp.utils;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class EnvironmentVariableTest {

    @Test
    void getWhitelistAsList_splitsByComma() {
        EnvironmentVariable env = new EnvironmentVariable();
        env.setWhitelist("/a,/b,/c");

        assertThat(env.getWhitelistAsList()).containsExactly("/a", "/b", "/c");
    }

    @Test
    void getCorsAllowedOriginsAsList_splitsByCommaAndTrims() {
        EnvironmentVariable env = new EnvironmentVariable();
        env.setCorsAllowedOrigins(" http://localhost:5173,https://example.com  ,");

        assertThat(env.getCorsAllowedOriginsAsList()).containsExactly("http://localhost:5173", "https://example.com");
    }
}
