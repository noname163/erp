package com.dat.erp.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import jakarta.servlet.http.HttpServletResponse;

class CookieUtilsTest {

    private static final String COOKIE_SCHEME = "COOKIE_SCHEME";

    @Test
    void addTokenCookie_setsSecureHttpOnlyCookieWithOneHourMaxAge() {
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);

        CookieUtils.addTokenCookie(response, "token");

        verify(response).addHeader(eq("Set-Cookie"),
                Mockito.argThat(value -> value.contains("AUTH_TOKEN=token")
                        && value.contains("HttpOnly")
                        && value.contains("Secure")
                        && value.contains("Path=/")
                        && value.contains("SameSite=None")
                        && value.contains("Max-Age=3600")));
    }

    @Test
    void addTokenCookie_setsNonSecureCookieWhenSchemeIsHttp() {
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        String previousCookieScheme = System.getProperty(COOKIE_SCHEME);

        try {
            System.setProperty(COOKIE_SCHEME, "http");

            CookieUtils.addTokenCookie(response, "token");

            verify(response).addHeader(eq("Set-Cookie"),
                    Mockito.argThat(value -> value.contains("AUTH_TOKEN=token")
                            && !value.contains("Secure")
                            && value.contains("SameSite=Lax")));
        } finally {
            if (previousCookieScheme == null) {
                System.clearProperty(COOKIE_SCHEME);
            } else {
                System.setProperty(COOKIE_SCHEME, previousCookieScheme);
            }
        }
    }

    @Test
    void clearTokenCookie_setsExpiredCookieWithConsistentAttributes() {
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);

        CookieUtils.clearTokenCookie(response);

        verify(response).addHeader(eq("Set-Cookie"),
                Mockito.argThat(value -> value.contains("AUTH_TOKEN=")
                        && value.contains("HttpOnly")
                        && value.contains("Path=/")
                        && value.contains("Max-Age=0")));
    }
}
