package com.dat.erp.dto.request;

import lombok.Data;

@Data
public class RoleHasApiRequest {
    private Long roleId;
    private String endpoint;
}
