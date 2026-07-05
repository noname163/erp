package com.dat.erp.utils;

import java.math.BigDecimal;

import com.dat.erp.entities.UserProfile;
import com.dat.erp.exceptions.BadRequestException;
import com.dat.erp.exceptions.ForbiddenException;
import com.dat.erp.systemconfigs.CustomUserDetails;

public class CustomStringUtils {
    private static final String FORBIDDEN_MESSAGE = "AUTH_403_001: Forbidden";

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

    public static String trimToNull(String rawValue) {
        String value = rawValue == null ? null : rawValue.trim();
        return (value == null || value.isBlank()) ? null : value;
    }

    public static String resolveScopedEmployeeCode(CustomUserDetails currentUser, String requestedEmployeeCode) {
        if (!isEmployee(currentUser)) {
            return normalizeCode(requestedEmployeeCode);
        }

        UserProfile currentProfile = currentUser.getUserProfile();
        String currentEmployeeCode = currentProfile == null ? null : normalizeCode(currentProfile.getCode());
        if (currentEmployeeCode == null) {
            throw new ForbiddenException(FORBIDDEN_MESSAGE);
        }

        String normalizedRequestedEmployeeCode = normalizeCode(requestedEmployeeCode);
        if (normalizedRequestedEmployeeCode != null && !currentEmployeeCode.equals(normalizedRequestedEmployeeCode)) {
            throw new ForbiddenException(FORBIDDEN_MESSAGE);
        }

        return currentEmployeeCode;
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

    private static boolean isEmployee(CustomUserDetails currentUser) {
        if (currentUser == null || currentUser.getAccount() == null || currentUser.getAccount().getRole() == null) {
            return false;
        }
        String roleName = currentUser.getAccount().getRole().getName();
        return roleName != null && "EMPLOYEE".equalsIgnoreCase(roleName.trim());
    }
}
