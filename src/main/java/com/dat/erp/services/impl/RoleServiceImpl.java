package com.dat.erp.services.impl;

import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.dat.erp.dto.request.RoleRequest;
import com.dat.erp.dto.response.PagedResponse;
import com.dat.erp.dto.response.RoleResponse;
import com.dat.erp.entities.Role;
import com.dat.erp.mapper.interfaces.RoleMapper;
import com.dat.erp.repositories.customrepositories.RoleRepository;
import com.dat.erp.services.RoleService;
import com.dat.erp.utils.PageableUtils;

@Service
public class RoleServiceImpl implements RoleService {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private RoleMapper roleMapper;

    @Override
    public String createRole(RoleRequest roleRequest) {
        Optional.ofNullable(roleRequest).orElseThrow(() -> new RuntimeException("RoleRequest cannot be null"));
        Role role = roleMapper.toEntity(roleRequest);
        role.setCode("ROLE-" + UUID.randomUUID());
        roleRepository.save(role);
        return role.getCode();
    }

    @Override
    public PagedResponse<RoleResponse> getRoleResponses(String searchValue, Integer page, Integer size,
            String sortBy, String sortDir) {
        Pageable pageable = PageableUtils.create(page, size, sortBy, sortDir);
        Page<Role> roles = roleRepository.findAll(pageable);
        return PageableUtils.mapPage(roles, roleMapper::toResponse, sortDir);
    }

}
