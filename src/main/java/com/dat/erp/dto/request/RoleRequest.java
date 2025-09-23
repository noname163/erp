package com.dat.erp.dto.request;

import lombok.Data;

@Data
public class RoleRequest {
    private String name;
    private Boolean create;
    private Boolean read;
    private Boolean update;
    private Boolean delete;
    private String description;
    private Integer level;
}
