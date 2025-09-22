package com.dat.erp.services;

import com.dat.erp.dto.request.RoleRequest;
import com.dat.erp.dto.response.PagedResponse;
import com.dat.erp.dto.response.RoleResponse;

public interface RoleService {
    public String createRole(RoleRequest roleRequest);

    public PagedResponse<RoleResponse> getRoleResponses(String searchValue, Integer page, Integer size, String sortBy,
            String sortDir);
}
