package com.dat.erp.dto.response;

import java.util.List;

import lombok.Data;

@Data
public class RoleDetailResponse extends RoleResponse {
    private List<RoleHasApiResponse> permission;
    private String comanyName;
    private String companyCode;
}
