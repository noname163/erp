package com.dat.erp.utils;

import java.util.Map;

import org.springframework.util.AntPathMatcher;

import com.dat.erp.dto.request.RoleHasApiRequest;
import com.dat.erp.dto.response.RoleHasApiResponse;

public class PermissionUtils {

    private static final int CREATE = 1; // 2^0
    private static final int READ = 1 << 1; // 2^1 = 2
    private static final int UPDATE = 1 << 2; // 2^2 = 4
    private static final int DELETE = 1 << 3; // 2^3 = 8
    private static final int VIEWALL = 1 << 4;
    private static final int VIEWOWNEDONLY = 1 << 5;
    private static final AntPathMatcher pathMatcher = new AntPathMatcher();

    /**
     * Convert RoleRequest permissions into an integer bitmask
     */
    /**
     * 
     * @param roleHasApiRequest
     * @return
     */
    public static int toInt(RoleHasApiRequest roleHasApiRequest) {
        int permissions = 0;

        if (Boolean.TRUE.equals(roleHasApiRequest.getCreate())) {
            permissions |= CREATE;
        }
        if (Boolean.TRUE.equals(roleHasApiRequest.getRead())) {
            permissions |= READ;
        }
        if (Boolean.TRUE.equals(roleHasApiRequest.getUpdate())) {
            permissions |= UPDATE;
        }
        if (Boolean.TRUE.equals(roleHasApiRequest.getDelete())) {
            permissions |= DELETE;
        }
        if (Boolean.TRUE.equals(roleHasApiRequest.getViewAll())) {
            permissions |= VIEWALL;
        }
        if (Boolean.TRUE.equals(roleHasApiRequest.getViewOwnedOnly())) {
            permissions |= VIEWOWNEDONLY;
        }

        return permissions;
    }

    /**
     * Decode integer back into roleHasApiRequest booleans
     */
    /**
     * 
     * @param roleHasApiResponse
     * @param permissions
     */
    public static void fromInt(RoleHasApiResponse roleHasApiResponse, int permissions) {
        roleHasApiResponse.setCreate((permissions & CREATE) != 0);
        roleHasApiResponse.setRead((permissions & READ) != 0);
        roleHasApiResponse.setUpdate((permissions & UPDATE) != 0);
        roleHasApiResponse.setDelete((permissions & DELETE) != 0);
        roleHasApiResponse.setViewAll((permissions & VIEWALL) != 0);
        roleHasApiResponse.setViewOwnedOnly((permissions & VIEWOWNEDONLY) != 0);
    }

    /**
     * 
     * @param permissions
     * @return
     */
    public static Boolean isViewAll(int permissions) {
        return (permissions & VIEWALL) != 0;
    }

    /**
     * 
     * @param permissions
     * @return
     */
    public static Boolean isViewOwnedOnly(int permissions) {
        return (permissions & VIEWOWNEDONLY) != 0;
    }

    private static int mapHttpMethodToPermission(String method) {
        return switch (method.toUpperCase()) {
            case "POST" -> CREATE;
            case "GET" -> READ;
            case "PUT", "PATCH" -> UPDATE;
            case "DELETE" -> DELETE;
            default -> 0; // unknown methods won't match
        };
    }

    /**
     * Check if the user has permission on a given URL + HTTP method.
     *
     * @param userPermissions
     *            Map of API pattern -> permission bitmask
     * @param url
     *            The request URI (e.g., /api/user/123)
     * @param httpMethod
     *            The HTTP method (GET, POST, etc.)
     * @return true if user has the required permission, false otherwise
     */
    public static boolean hasPermission(Map<String, Integer> userPermissions,
            String url,
            String httpMethod) {
        if (userPermissions == null || userPermissions.isEmpty()) {
            return false;
        }

        int requiredPermission = mapHttpMethodToPermission(httpMethod);
        if (requiredPermission == 0) {
            return false; // skip if method not mapped
        }

        for (Map.Entry<String, Integer> entry : userPermissions.entrySet()) {
            String pattern = entry.getKey();
            int userPermission = entry.getValue();

            if (pathMatcher.match(pattern, url)) {
                return (userPermission & requiredPermission) == requiredPermission;
            }
        }
        return false;
    }

    public static boolean hasViewAllPermission(Map<String, Integer> userPermissions,
            String url) {
        if (userPermissions == null || userPermissions.isEmpty()) {
            return false;
        }

        for (Map.Entry<String, Integer> entry : userPermissions.entrySet()) {
            String pattern = entry.getKey();
            int userPermission = entry.getValue();

            if (pathMatcher.match(pattern, url)) {
                return (userPermission & VIEWALL) == VIEWALL;
            }
        }
        return false;
    }

    public static boolean hasViewOwnedPermission(Map<String, Integer> userPermissions,
            String url) {
        if (userPermissions == null || userPermissions.isEmpty()) {
            return false;
        }

        for (Map.Entry<String, Integer> entry : userPermissions.entrySet()) {
            String pattern = entry.getKey();
            int userPermission = entry.getValue();

            if (pathMatcher.match(pattern, url)) {
                return (userPermission & VIEWOWNEDONLY) == VIEWOWNEDONLY;
            }
        }
        return false;
    }
}
