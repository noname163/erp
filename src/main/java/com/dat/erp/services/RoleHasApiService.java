package com.dat.erp.services;

import com.dat.erp.dto.request.RoleHasApiRequest;
import com.dat.erp.dto.response.PagedResponse;
import com.dat.erp.dto.response.RoleHasApiResponse;

public interface RoleHasApiService {
    public String assignApisToRole(RoleHasApiRequest roleHasApiRequest);

    public PagedResponse<RoleHasApiResponse> getApisByRoleId(String roleCode, Integer page, Integer size, String sortBy,
            String sortDir);
}
