package com.dat.erp.mapper.decorator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dat.erp.dto.request.RoleHasApiRequest;
import com.dat.erp.dto.response.RoleHasApiResponse;
import com.dat.erp.entities.Role;
import com.dat.erp.entities.RoleHasApi;
import com.dat.erp.entities.SystemApi;
import com.dat.erp.mapper.interfaces.RoleHasApiMapper;
import com.dat.erp.repositories.customrepositories.RoleRepository;
import com.dat.erp.repositories.customrepositories.SystemApiRepository;

@Component
public abstract class RoleHasApiMapperDecorator implements RoleHasApiMapper {
    @Autowired
    private RoleHasApiMapper delegate;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private SystemApiRepository systemApiRepository;

    @Override
    public RoleHasApi toEntity(RoleHasApiRequest request) {
        RoleHasApi roleHasApi = delegate.toEntity(request);
        Role role = roleRepository.findByCode(request.getRoleCode())
                .orElseThrow(
                        () -> new RuntimeException("Role not found with code: " + request.getRoleCode()));
        SystemApi systemApi = systemApiRepository.findByCode(request.getApiCode()).orElseThrow(
                () -> new RuntimeException("Api not found with code: " + request.getRoleCode()));
        roleHasApi.setApi(systemApi);
        roleHasApi.setRole(role);
        return roleHasApi;
    }

    @Override
    public RoleHasApiResponse toResponse(RoleHasApi entity) {
        return delegate.toResponse(entity);
    }

}
