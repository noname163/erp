package com.dat.erp.utils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

public class CookieUtils {

    public static void addTokenCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie("AUTH_TOKEN", token);
        cookie.setHttpOnly(true); // prevent JS access
        cookie.setSecure(true); // only HTTPS
        cookie.setPath("/"); // available for the whole app
        cookie.setMaxAge(60 * 60); // 1 hour expiration
        response.addCookie(cookie);
    }
}
