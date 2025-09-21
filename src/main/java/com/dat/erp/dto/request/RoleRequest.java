package com.dat.erp.dto.request;

import lombok.Data;

@Data
public class RoleRequest {
    private String name;
    private Integer permission;
    private String description;
    private Integer level;
}
