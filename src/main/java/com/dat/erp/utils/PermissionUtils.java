package com.dat.erp.utils;

import com.dat.erp.dto.request.RoleHasApiRequest;
import com.dat.erp.dto.response.RoleHasApiResponse;

public class PermissionUtils {

    private static final int CREATE = 1; // 2^0
    private static final int READ = 1 << 1; // 2^1 = 2
    private static final int UPDATE = 1 << 2; // 2^2 = 4
    private static final int DELETE = 1 << 3; // 2^3 = 8

    /**
     * Convert RoleRequest permissions into an integer bitmask
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

        return permissions;
    }

    /**
     * Decode integer back into roleHasApiRequest booleans
     */
    public static void fromInt(RoleHasApiResponse roleHasApiResponse, int permissions) {
        roleHasApiResponse.setCreate((permissions & CREATE) != 0);
        roleHasApiResponse.setRead((permissions & READ) != 0);
        roleHasApiResponse.setUpdate((permissions & UPDATE) != 0);
        roleHasApiResponse.setDelete((permissions & DELETE) != 0);
    }
}
