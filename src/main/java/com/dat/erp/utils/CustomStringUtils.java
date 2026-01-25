package com.dat.erp.utils;

import java.math.BigDecimal;

import com.dat.erp.exceptions.BadRequestException;

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

    public static String normalizeCode(String rawValue) {
        String value = rawValue == null ? null : rawValue.trim();
        return (value == null || value.isBlank()) ? null : value;
    }

    public static BigDecimal parsePositiveBigDecimal(String rawValue, String errorMessage) {
        if (rawValue == null || rawValue.isBlank()) {
            throw new BadRequestException(errorMessage);
        }
        try {
            BigDecimal value = new BigDecimal(rawValue.trim());
            if (value.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BadRequestException(errorMessage);
            }
            return value;
        } catch (NumberFormatException ex) {
            throw new BadRequestException(errorMessage);
        }
    }
}
