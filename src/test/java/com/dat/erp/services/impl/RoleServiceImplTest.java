package com.dat.erp.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.dat.erp.dto.request.RoleRequest;
import com.dat.erp.dto.response.PagedResponse;
import com.dat.erp.dto.response.RoleResponse;
import com.dat.erp.entities.Role;
import com.dat.erp.mapper.interfaces.RoleMapper;
import com.dat.erp.repositories.customrepositories.RoleRepository;

class RoleServiceImplTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private RoleMapper roleMapper;

    @InjectMocks
    private RoleServiceImpl roleService;

    private RoleRequest roleRequest;
    private Role role;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        roleRequest = new RoleRequest();
        roleRequest.setName("Admin");

        role = new Role();
        role.setName("Admin");
    }

    // -----------------------------
    // createRole
    // -----------------------------
    @Test
    void testCreateRole_Success() {
        when(roleMapper.toEntity(roleRequest)).thenReturn(role);

        String result = roleService.createRole(roleRequest);

        assertEquals("Role created successfully", result);
        assertEquals("ADMIN", role.getCode()); // code should be generated
        verify(roleRepository).save(role);
    }

    @Test
    void testCreateRole_RequestNull() {
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> roleService.createRole(null));
        assertEquals("RoleRequest cannot be null", ex.getMessage());
        verify(roleRepository, never()).save(any());
    }

    // -----------------------------
    // getRoleResponses
    // -----------------------------
    @Test
    void testGetRoleResponses_Success() {
        Page<Role> page = new PageImpl<>(Collections.singletonList(role));
        when(roleRepository.findAll(any(Pageable.class))).thenReturn(page);

        RoleResponse response = new RoleResponse();
        when(roleMapper.toResponse(role)).thenReturn(response);

        PagedResponse<RoleResponse> result = roleService.getRoleResponses("search", 0, 10, "id", "asc");

        assertNotNull(result);
        assertEquals(1, result.getData().size());
        assertSame(response, result.getData().get(0));
    }
}
