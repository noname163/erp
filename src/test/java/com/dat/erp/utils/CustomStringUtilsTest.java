package com.dat.erp.utils;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class CustomStringUtilsTest {

    @Test
    void normalizeUrl_handlesNullAndEmpty() {
        assertThat(CustomStringUtils.normalizeUrl(null)).isNull();
        assertThat(CustomStringUtils.normalizeUrl("")).isEqualTo("");
    }

    @Test
    void normalizeUrl_addsLeadingSlashAndKeepsTwoSegments() {
        assertThat(CustomStringUtils.normalizeUrl("api/auth/login")).isEqualTo("/api/auth");
        assertThat(CustomStringUtils.normalizeUrl("/api")).isEqualTo("/api");
        assertThat(CustomStringUtils.normalizeUrl("/api/auth/login")).isEqualTo("/api/auth");
    }
}

