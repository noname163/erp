package com.dat.erp.services.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.dat.erp.dto.response.SelectionOptionResponse;
import com.dat.erp.entities.Role;
import com.dat.erp.mapper.interfaces.RoleMapper;
import com.dat.erp.repositories.customrepositories.RoleRepository;
import com.dat.erp.services.RoleService;

@Service
public class RoleServiceImpl implements RoleService {
    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;

    public RoleServiceImpl(RoleRepository roleRepository, RoleMapper roleMapper) {
        this.roleRepository = roleRepository;
        this.roleMapper = roleMapper;
    }

    @Override
    public List<SelectionOptionResponse> getRoleOptionsByCompanyCode(String name, Boolean isPublic,
            String companyCode) {
        List<Role> roles = roleRepository.findOptionsByFilters(name, isPublic, companyCode);
        return new ArrayList<>(roleMapper.toOptionResponses(roles));
    }
}
