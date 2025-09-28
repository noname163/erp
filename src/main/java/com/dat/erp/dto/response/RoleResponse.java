package com.dat.erp.dto.response;

import lombok.Data;

@Data
public class RoleResponse {
    private Long id;
    private String name;
    private String code;
    private String description;
    private Integer level;
}
