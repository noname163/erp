package com.dat.erp.utils;

public class CustomStringUtils {
    public static String normalizeUrl(String url) {
        if (url == null || url.isEmpty()) {
            return url;
        }

        if (!url.startsWith("/")) {
            url = "/" + url;
        }

        String[] parts = url.split("/");
        if (parts.length >= 3) {
            return "/" + parts[1] + "/" + parts[2]; // /xxx/xxxx
        } else if (parts.length >= 2) {
            return "/" + parts[1]; // /xxx
        } else {
            return url;
        }
    }
}
