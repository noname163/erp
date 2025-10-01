package com.dat.erp.dto.response;

import lombok.Data;

@Data
public class RoleHasApiResponse {
    private Long id;
    private String code;
    private String roleName;
    private String endpoint;
    private Boolean create;
    private Boolean read;
    private Boolean update;
    private Boolean delete;
    private Boolean viewAll;
    private Boolean viewOwnedOnly;
}
