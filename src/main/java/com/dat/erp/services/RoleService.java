package com.dat.erp.services;

import java.util.List;

import com.dat.erp.dto.response.SelectionOptionResponse;

public interface RoleService {
    public List<SelectionOptionResponse> getRoleOptionsByCompanyCode(String name, Boolean isPublic, String companyCode);
}
