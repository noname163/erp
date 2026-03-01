package com.dat.erp.services.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.dat.erp.dto.response.SelectionOptionResponse;
import com.dat.erp.entities.Role;
import com.dat.erp.repositories.customrepositories.RoleRepository;
import com.dat.erp.services.RoleService;

@Service
public class RoleServiceImpl implements RoleService {
    private final RoleRepository roleRepository;

    public RoleServiceImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public List<SelectionOptionResponse> getRoleOptionsByCompanyCode(String name, Boolean isPublic,
            String companyCode) {
        List<Role> roles = roleRepository.findOptionsByFilters(name, isPublic, companyCode);
        List<SelectionOptionResponse> options = new ArrayList<>(roles.size());
        for (Role role : roles) {
            SelectionOptionResponse option = new SelectionOptionResponse();
            option.setCode(role.getCode());
            option.setName(role.getName());
            options.add(option);
        }
        return options;
    }
}
