package com.dat.erp.services.impl;

import com.dat.erp.dto.request.RoleRequest;
import com.dat.erp.dto.response.PagedResponse;
import com.dat.erp.dto.response.RoleResponse;
import com.dat.erp.entities.Role;
import com.dat.erp.mapper.interfaces.RoleMapper;
import com.dat.erp.repositories.customrepositories.RoleRepository;
import com.dat.erp.utils.PageableUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
