package com.dat.erp.dto.response;

import lombok.Data;

@Data
public class RoleResponse {
    private Long id;
    private String name;
    private String code;
    private Boolean create;
    private Boolean read;
    private Boolean update;
    private Boolean delete;
    private String description;
    private Integer level;
}
