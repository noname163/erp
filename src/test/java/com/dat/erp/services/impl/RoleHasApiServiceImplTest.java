package com.dat.erp.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.dat.erp.dto.request.RoleHasApiRequest;
import com.dat.erp.dto.response.PagedResponse;
import com.dat.erp.dto.response.RoleHasApiResponse;
import com.dat.erp.entities.Role;
import com.dat.erp.entities.RoleHasApi;
import com.dat.erp.mapper.interfaces.RoleHasApiMapper;
import com.dat.erp.repositories.customrepositories.RoleHasApiRepository;
import com.dat.erp.repositories.customrepositories.RoleRepository;

class RoleHasApiServiceImplTest {

    @Mock
    private RoleHasApiRepository roleHasApiRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private RoleHasApiMapper roleHasApiMapper;

    @InjectMocks
    private RoleHasApiServiceImpl roleHasApiService;

    private Role role;
    private RoleHasApiRequest request;
    private RoleHasApi roleHasApi;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        role = new Role();
        role.setCode("ADMIN");

        request = new RoleHasApiRequest();
        request.setRoleCode("ADMIN");

        roleHasApi = new RoleHasApi();
        roleHasApi.setRole(role);
    }

    // -------------------------------
    // assignApisToRole
    // -------------------------------
    @Test
    void testAssignApisToRole_Success() {
        when(roleRepository.findByCode("ADMIN")).thenReturn(Optional.of(role));
        when(roleHasApiMapper.toEntity(request)).thenReturn(roleHasApi);

        String result = roleHasApiService.assignApisToRole(request);

        assertEquals("APIs assigned to role successfully", result);
        verify(roleHasApiRepository).save(roleHasApi);
    }

    @Test
    void testAssignApisToRole_RequestNull() {
        when(roleRepository.findByCode("ADMIN")).thenReturn(Optional.of(role));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> roleHasApiService.assignApisToRole(null));

        assertEquals("RoleHasApiRequest cannot be null", ex.getMessage());
        verify(roleHasApiRepository, never()).save(any());
    }

    // -------------------------------
    // getApisByRoleId
    // -------------------------------
    @Test
    void testGetApisByRoleId_Success() {
        RoleHasApi roleHasApi = new RoleHasApi();
        Page<RoleHasApi> page = new PageImpl<>(Collections.singletonList(roleHasApi));
        when(roleHasApiRepository.findByRoleCode(eq("ADMIN"), any(Pageable.class)))
                .thenReturn(page);

        RoleHasApiResponse response = new RoleHasApiResponse();
        when(roleHasApiMapper.toResponse(roleHasApi)).thenReturn(response);

        PagedResponse<RoleHasApiResponse> result = roleHasApiService.getApisByRoleId("ADMIN", 0, 10, "id", "asc");

        assertNotNull(result);
        assertEquals(1, result.getData().size());
    }

    // -------------------------------
    // getUserPermissionByUserCode
    // -------------------------------
    @Test
    void testGetUserPermissionByUserCode_Success() {
        List<Object[]> mockResults = Arrays.asList(
                new Object[] { "API_1", 1 },
                new Object[] { "API_2", 2 });
        when(roleHasApiRepository.getCurrentUserPermission("U123"))
                .thenReturn(mockResults);

        Map<String, Integer> result = roleHasApiService.getUserPermissionByUserCode("U123");

        assertEquals(2, result.size());
        assertEquals(1, result.get("API_1"));
        assertEquals(2, result.get("API_2"));
    }
}
