package com.dat.erp.utils;

import com.dat.erp.dto.request.RoleRequest;
import com.dat.erp.dto.response.RoleResponse;

public class PermissionUtils {

    private static final int CREATE = 1; // 2^0
    private static final int READ = 1 << 1; // 2^1 = 2
    private static final int UPDATE = 1 << 2; // 2^2 = 4
    private static final int DELETE = 1 << 3; // 2^3 = 8

    /**
     * Convert RoleRequest permissions into an integer bitmask
     */
    public static int toInt(RoleRequest roleRequest) {
        int permissions = 0;

        if (Boolean.TRUE.equals(roleRequest.getCreate())) {
            permissions |= CREATE;
        }
        if (Boolean.TRUE.equals(roleRequest.getRead())) {
            permissions |= READ;
        }
        if (Boolean.TRUE.equals(roleRequest.getUpdate())) {
            permissions |= UPDATE;
        }
        if (Boolean.TRUE.equals(roleRequest.getDelete())) {
            permissions |= DELETE;
        }

        return permissions;
    }

    /**
     * Decode integer back into RoleRequest booleans
     */
    public static void fromInt(RoleResponse roleRequest, int permissions) {
        roleRequest.setCreate((permissions & CREATE) != 0);
        roleRequest.setRead((permissions & READ) != 0);
        roleRequest.setUpdate((permissions & UPDATE) != 0);
        roleRequest.setDelete((permissions & DELETE) != 0);
    }
}
