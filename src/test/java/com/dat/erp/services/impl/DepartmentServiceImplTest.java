package com.dat.erp.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
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

import com.dat.erp.constants.CommonStatus;
import com.dat.erp.constants.Messages;
import com.dat.erp.dto.request.DepartmentRequest;
import com.dat.erp.dto.response.PagedResponse;
import com.dat.erp.dto.response.department.DepartmentResponse;
import com.dat.erp.entities.Account;
import com.dat.erp.entities.Department;
import com.dat.erp.exceptions.ConflictException;
import com.dat.erp.mapper.interfaces.DepartmentMapper;
import com.dat.erp.repositories.customrepositories.DepartmentRepository;
import com.dat.erp.services.CodeGenerator;
import com.dat.erp.services.SecurityContextService;
import com.dat.erp.systemconfigs.CustomUserDetails;

class DepartmentServiceImplTest {

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private DepartmentMapper departmentMapper;

    @Mock
    private CodeGenerator codeGenerator;

    @Mock
    private SecurityContextService securityContextService;

    @InjectMocks
    private DepartmentServiceImpl departmentService;

    private DepartmentRequest request;
    private Department department;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        request = new DepartmentRequest();
        request.setName("HR");

        department = new Department();
        department.setName("HR");
    }

    // -----------------------------
    // createDepartment
    // -----------------------------
    @Test
    void testCreateDepartment_Success() {
        when(departmentMapper.toEntity(request)).thenReturn(department);
        when(departmentRepository.existsByNameAndCompanyCode("HR", "CMP-1")).thenReturn(false);
        when(codeGenerator.nextCode("DPM-")).thenReturn("DPM-000001");

        Account account = new Account();
        com.dat.erp.testutils.EntityTestData.setCode(account, "USR-1");
        com.dat.erp.testutils.EntityTestData.setCompanyCode(account, "CMP-1");
        doReturn(new CustomUserDetails(account, null)).when(securityContextService).getCurrentUser();
        when(securityContextService.getCurrentCompanyCode()).thenReturn("CMP-1");

        String code = departmentService.createDepartment(request);

        assertEquals("DPM-000001", code);
        assertEquals(CommonStatus.ACTIVATE, department.getStatus());
        verify(departmentRepository).save(department);
    }

    @Test
    void testCreateDepartment_ConflictByName() {
        when(departmentMapper.toEntity(request)).thenReturn(department);
        when(departmentRepository.existsByNameAndCompanyCode("HR", "CMP-1")).thenReturn(true);
        when(securityContextService.getCurrentCompanyCode()).thenReturn("CMP-1");

        ConflictException ex = assertThrows(ConflictException.class,
                () -> departmentService.createDepartment(request));

        assertEquals(Messages.ERROR_DEPARTMENT_NAME_EXISTS, ex.getMessage());
        verify(departmentRepository, never()).save(any());
    }

    @Test
    void testGetDepartmentByCompanyCode_Success() {
        Page<Department> page = new PageImpl<>(Collections.singletonList(department));
        when(departmentRepository.findAll(any(Pageable.class))).thenReturn(page);

        DepartmentResponse response = new DepartmentResponse();
        when(departmentMapper.toResponse(department)).thenReturn(response);

        PagedResponse<DepartmentResponse> result = departmentService.getDepartmentByCompanyCode("name", "HR", 0, 10,
                "id", "asc");

        assertNotNull(result);
        assertEquals(1, result.getData().size());
        assertEquals(response, result.getData().get(0));
    }

    @Test
    void testCreateDefaultDepartment_WhenExists_ReturnsExistingCode() {
        Department existing = new Department();
        com.dat.erp.testutils.EntityTestData.setCode(existing, "DPM-EXISTING");
        when(departmentRepository.findByNameAndCompanyCode(eq("HR"), eq("CMP-1")))
                .thenReturn(java.util.Optional.of(existing));

        request.setCompanyCode("CMP-1");
        request.setName("HR");

        String code = departmentService.createDefaultDepartment(request, "ACC-ADMIN");

        assertEquals("DPM-EXISTING", code);
        verify(departmentRepository, never()).save(any());
    }
}
