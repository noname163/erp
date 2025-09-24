package com.dat.erp.dto.response;

import lombok.Data;

@Data
public class RoleHasApiResponse {
    private Long id;
    private String roleName;
    private Boolean create;
    private Boolean read;
    private Boolean update;
    private Boolean delete;
    private String endpoint;
}
