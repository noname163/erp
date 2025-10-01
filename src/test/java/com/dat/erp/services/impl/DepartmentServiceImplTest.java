package com.dat.erp.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.dat.erp.constants.CommonStatus;
import com.dat.erp.dto.request.DepartmentRequest;
import com.dat.erp.dto.response.DepartmentResponse;
import com.dat.erp.dto.response.PagedResponse;
import com.dat.erp.entities.Company;
import com.dat.erp.entities.Department;
import com.dat.erp.exceptions.ResourceNotFoundException;
import com.dat.erp.mapper.interfaces.DepartmentMapper;
import com.dat.erp.repositories.customrepositories.CompanyRepository;
import com.dat.erp.repositories.customrepositories.DepartmentRepository;

class DepartmentServiceImplTest {

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private DepartmentMapper departmentMapper;

    @Mock
    private CompanyRepository companyRepository;

    @InjectMocks
    private DepartmentServiceImpl departmentService;

    private DepartmentRequest request;
    private Department department;
    private Company company;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        request = new DepartmentRequest();
        request.setName("HR");
        request.setCompanyCode("COMP-1");

        department = new Department();
        department.setName("HR");

        company = new Company();
        company.setCode("COMP-1");
    }

    // -----------------------------
    // createDepartment
    // -----------------------------
    @Test
    void testCreateDepartment_Success() {
        when(departmentMapper.toEntity(request)).thenReturn(department);
        when(companyRepository.findByCode("COMP-1")).thenReturn(Optional.of(company));

        String code = departmentService.createDepartment(request);

        assertNotNull(code);
        assertTrue(code.startsWith("DPM-"));
        assertEquals(CommonStatus.ACTIVATE, department.getStatus());
        assertEquals(company, department.getCompany());
        verify(departmentRepository).save(department);
    }

    @Test
    void testCreateDepartment_CompanyNotFound() {
        when(departmentMapper.toEntity(request)).thenReturn(department);
        when(companyRepository.findByCode("COMP-1")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> departmentService.createDepartment(request));

        verify(departmentRepository, never()).save(any());
    }

    // -----------------------------
    // getDepartmentByCompanyCode
    // -----------------------------
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
        assertSame(response, result.getData().get(0));
    }
}
