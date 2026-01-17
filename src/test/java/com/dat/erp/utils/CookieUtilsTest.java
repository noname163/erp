package com.dat.erp.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

class CookieUtilsTest {

    @Test
    void addTokenCookie_setsSecureHttpOnlyCookieWithOneHourMaxAge() {
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);

        CookieUtils.addTokenCookie(response, "token");

        ArgumentCaptor<Cookie> captor = ArgumentCaptor.forClass(Cookie.class);
        verify(response).addCookie(captor.capture());
        Cookie cookie = captor.getValue();

        assertThat(cookie.getName()).isEqualTo("AUTH_TOKEN");
        assertThat(cookie.getValue()).isEqualTo("token");
        assertThat(cookie.isHttpOnly()).isTrue();
        assertThat(cookie.getSecure()).isTrue();
        assertThat(cookie.getPath()).isEqualTo("/");
        assertThat(cookie.getMaxAge()).isEqualTo(60 * 60);
    }
}

