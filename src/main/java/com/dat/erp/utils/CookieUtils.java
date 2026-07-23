package com.dat.erp.utils;

import java.time.Duration;

import org.springframework.http.ResponseCookie;

import jakarta.servlet.http.HttpServletResponse;

public class CookieUtils {

    private static final String AUTH_TOKEN = "AUTH_TOKEN";
    private static final String COOKIE_SECURE_ENV = "COOKIE_SECURE";
    private static final String COOKIE_SCHEME_ENV = "COOKIE_SCHEME";
    private static final String COOKIE_SAME_SITE_ENV = "COOKIE_SAME_SITE";

    public static void addTokenCookie(HttpServletResponse response, String token) {
        boolean secure = isSecureCookieEnabled();
        String sameSite = resolveSameSite(secure);

        ResponseCookie cookie = ResponseCookie.from(AUTH_TOKEN, token)
                .httpOnly(true)
                .secure(secure)
                .path("/")
                .maxAge(Duration.ofHours(1))
                .sameSite(sameSite)
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
    }

    public static void clearTokenCookie(HttpServletResponse response) {
        boolean secure = isSecureCookieEnabled();
        String sameSite = resolveSameSite(secure);

        ResponseCookie cookie = ResponseCookie.from(AUTH_TOKEN, "")
                .httpOnly(true)
                .secure(secure)
                .path("/")
                .maxAge(Duration.ZERO)
                .sameSite(sameSite)
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
    }

    private static boolean isSecureCookieEnabled() {
        String explicitSecureFlag = readFromEnvironmentOrProperty(COOKIE_SECURE_ENV);
        if (explicitSecureFlag != null && !explicitSecureFlag.isBlank()) {
            return Boolean.parseBoolean(explicitSecureFlag.trim());
        }

        String scheme = readFromEnvironmentOrProperty(COOKIE_SCHEME_ENV);
        if (scheme == null || scheme.isBlank()) {
            return true;
        }

        return !"http".equalsIgnoreCase(scheme.trim());
    }

    private static String readFromEnvironmentOrProperty(String key) {
        String envValue = System.getenv(key);
        if (envValue != null) {
            return envValue;
        }

        return System.getProperty(key);
    }

    private static String resolveSameSite(boolean secureCookie) {
        String configuredSameSite = readFromEnvironmentOrProperty(COOKIE_SAME_SITE_ENV);
        if (configuredSameSite != null && !configuredSameSite.isBlank()) {
            return configuredSameSite.trim();
        }

        return secureCookie ? "None" : "Lax";
    }
}
