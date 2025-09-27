package com.dat.erp.services.impl;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.dat.erp.dto.request.RoleHasApiRequest;
import com.dat.erp.dto.response.PagedResponse;
import com.dat.erp.dto.response.RoleHasApiResponse;
import com.dat.erp.entities.Role;
import com.dat.erp.entities.RoleHasApi;
import com.dat.erp.mapper.interfaces.RoleHasApiMapper;
import com.dat.erp.repositories.customrepositories.RoleHasApiRepository;
import com.dat.erp.repositories.customrepositories.RoleRepository;
import com.dat.erp.services.RoleHasApiService;
import com.dat.erp.utils.PageableUtils;

@Service
public class RoleHasApiServiceImpl implements RoleHasApiService {
    @Autowired
    private RoleHasApiRepository roleHasApiRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private RoleHasApiMapper roleHasApiMapper;

    @Override
    public String assignApisToRole(RoleHasApiRequest roleHasApiRequest) {
        Optional.ofNullable(roleHasApiRequest)
                .orElseThrow(() -> new RuntimeException("RoleHasApiRequest cannot be null"));
        Role role = roleRepository.findByCode(roleHasApiRequest.getRoleCode())
                .orElseThrow(
                        () -> new RuntimeException("Role not found with code: " + roleHasApiRequest.getRoleCode()));
        RoleHasApi roleHasApi = roleHasApiMapper.toEntity(roleHasApiRequest);
        roleHasApi.setRole(role);
        roleHasApiRepository.save(roleHasApi);
        return "APIs assigned to role successfully";
    }

    @Override
    public PagedResponse<RoleHasApiResponse> getApisByRoleId(String roleCode, Integer page, Integer size,
            String sortBy,
            String sortDir) {
        Pageable pageable = PageableUtils.create(page, size, sortBy, sortDir);

        Page<RoleHasApi> roleHasApis = roleHasApiRepository.findByRoleCode(roleCode, pageable);

        return PageableUtils.mapPage(roleHasApis, roleHasApiMapper::toResponse, sortDir);
    }

    @Override
    public Map<String, Integer> getUserPermissionByUserCode(String userCode) {

        List<Object[]> results = roleHasApiRepository.getCurrentUserPermission(userCode);
        Map<String, Integer> map = results.stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],
                        row -> (Integer) row[1]));

        return map;

    }

}
