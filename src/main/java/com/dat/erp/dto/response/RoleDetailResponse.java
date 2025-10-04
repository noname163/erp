package com.dat.erp.dto.response;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class RoleDetailResponse extends RoleResponse {
    private List<RoleHasApiResponse> permission;
    private String comanyName;
    private String companyCode;
}
