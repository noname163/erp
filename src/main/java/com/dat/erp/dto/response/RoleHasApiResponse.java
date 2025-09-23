package com.dat.erp.dto.response;

import java.util.List;

import lombok.Data;

@Data
public class RoleHasApiResponse {
    private Long id;
    private String roleName;
    private List<String> endpoints;
}
