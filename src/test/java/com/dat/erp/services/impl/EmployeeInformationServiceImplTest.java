package com.dat.erp.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import com.dat.erp.constants.CommonStatus;
import com.dat.erp.dto.request.EmployeeInformationRequest;
import com.dat.erp.dto.response.EmployeeInformationDetailResponse;
import com.dat.erp.dto.response.EmployeeInformationResponse;
import com.dat.erp.dto.response.PagedResponse;
import com.dat.erp.entities.Company;
import com.dat.erp.entities.EmployeeInformation;
import com.dat.erp.entities.UserInformation;
import com.dat.erp.exceptions.ResourceNotFoundException;
import com.dat.erp.mapper.interfaces.EmployeeMapper;
import com.dat.erp.repositories.customrepositories.EmployeeInformationRepository;
import com.dat.erp.repositories.specifications.EmployeeSpecifications;
import com.dat.erp.utils.PageableUtils;

import jakarta.persistence.EntityNotFoundException;

class EmployeeInformationServiceImplTest {

    @Mock
    private EmployeeInformationRepository employeeInformationRepository;

    @Mock
    private EmployeeMapper employeeMapper;

    private UserInformation userInformation;

    @InjectMocks
    private EmployeeInformationServiceImpl employeeInformationService;

    private EmployeeInformationRequest request;
    private EmployeeInformation entity;

    private EmployeeInformation employee;
    private EmployeeInformationDetailResponse response;
    private Company company;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        request = new EmployeeInformationRequest();
        request.setNickname("John Doe");

        entity = new EmployeeInformation();
        entity.setNickname("John Doe");
        company = new Company();
        company.setCode("COMPANY_001");
        userInformation = new UserInformation();
        userInformation.setDateOfBirth("2001-09-05");
        employee = new EmployeeInformation();
        employee.setCode("EMP001");
        employee.setUser(userInformation);

        entity.setUser(userInformation);
        entity.setCompany(company);
        response = new EmployeeInformationDetailResponse();
        response.setCode("EMP001");
    }

    // -----------------------------
    // createEmployeeInformation
    // -----------------------------
    @Test
    void testCreateEmployeeInformation_Success() {
        when(employeeMapper.toEntity(request)).thenReturn(entity);
        String code = employeeInformationService.createEmployeeInformation(request);

        assertNotNull(code);
        assertTrue(code.startsWith("EMPI-"));
        assertEquals(CommonStatus.ACTIVATE, entity.getEmploymentStatus());
        verify(employeeInformationRepository).save(entity);
    }

    // -----------------------------
    // getListEmployeeInformationResponse
    // -----------------------------
    @Test
    void testGetListEmployeeInformationResponse_Success() {
        // Mock pageable and specification
        Pageable pageable = PageRequest.of(0, 10, Sort.by("code").ascending());
        Specification<EmployeeInformation> spec = mock(Specification.class);

        // Mock static calls
        try (MockedStatic<PageableUtils> pageableUtilsMock = mockStatic(PageableUtils.class);
                MockedStatic<EmployeeSpecifications> employeeSpecMock = mockStatic(EmployeeSpecifications.class)) {

            // Mock PageableUtils.create()
            pageableUtilsMock.when(() -> PageableUtils.create(0, 10, "code", "asc"))
                    .thenReturn(pageable);

            // Mock EmployeeSpecifications.build()
            employeeSpecMock.when(() -> EmployeeSpecifications.build("DEP01", "COMP01", "MAN01", "john"))
                    .thenReturn(spec);

            // Mock repository and mapper
            Page<EmployeeInformation> employeePage = new PageImpl<>(List.of(employee), pageable, 1);
            when(employeeInformationRepository.findAll(spec, pageable)).thenReturn(employeePage);
            when(employeeMapper.toResponse(employee)).thenReturn(response);

            // Mock PageableUtils.mapPage()
            PagedResponse<EmployeeInformationResponse> expectedPagedResponse = PagedResponse
                    .<EmployeeInformationResponse>builder()
                    .data(List.of(response))
                    .page(0)
                    .size(10)
                    .totalElements(1)
                    .totalPages(1)
                    .last(true)
                    .message("Success")
                    .success(true)
                    .build();

            pageableUtilsMock.when(() -> PageableUtils.mapPage(eq(employeePage), any(), eq("Success")))
                    .thenReturn(expectedPagedResponse);

            // Execute
            PagedResponse<EmployeeInformationResponse> result = employeeInformationService
                    .getListEmployeeInformationResponse(
                            "COMP01", "DEP01", "MAN01", "john", 0, 10, "code", "asc");

            // Verify results
            assertNotNull(result);
            assertEquals("Success", result.getMessage());
            assertEquals(1, result.getData().size());
            assertEquals("EMP001", result.getData().get(0).getCode());

            // Verify interactions
            pageableUtilsMock.verify(() -> PageableUtils.create(0, 10, "code", "asc"));
            employeeSpecMock.verify(() -> EmployeeSpecifications.build("DEP01", "COMP01", "MAN01", "john"));
            verify(employeeInformationRepository).findAll(spec, pageable);
        }
    }

    @Test
    void testGetListEmployeeInformationResponse_EmptyResult() {
        Pageable pageable = PageRequest.of(0, 10);
        Specification<EmployeeInformation> spec = mock(Specification.class);

        try (MockedStatic<PageableUtils> pageableUtilsMock = mockStatic(PageableUtils.class);
                MockedStatic<EmployeeSpecifications> employeeSpecMock = mockStatic(EmployeeSpecifications.class)) {

            pageableUtilsMock.when(() -> PageableUtils.create(0, 10, "id", "desc"))
                    .thenReturn(pageable);
            employeeSpecMock.when(() -> EmployeeSpecifications.build(null, null, null, null))
                    .thenReturn(spec);

            Page<EmployeeInformation> emptyPage = new PageImpl<>(List.of(), pageable, 0);
            when(employeeInformationRepository.findAll(spec, pageable)).thenReturn(emptyPage);

            PagedResponse<EmployeeInformationResponse> emptyResponse = PagedResponse
                    .<EmployeeInformationResponse>builder()
                    .data(List.of())
                    .page(0)
                    .size(0)
                    .totalElements(0)
                    .totalPages(0)
                    .last(true)
                    .message("Success")
                    .success(true)
                    .build();
            pageableUtilsMock.when(() -> PageableUtils.mapPage(eq(emptyPage), any(), eq("Success")))
                    .thenReturn(emptyResponse);

            PagedResponse<EmployeeInformationResponse> result = employeeInformationService
                    .getListEmployeeInformationResponse(
                            null, null, null, null, 0, 10, "id", "desc");

            assertNotNull(result);
            assertTrue(result.getData().isEmpty());
            assertEquals("Success", result.getMessage());
        }
    }

    @Test
    void testEmployeeInformationDetailResponse_Success() {
        when(employeeInformationRepository.findDetailedByCode("EMP001"))
                .thenReturn(Optional.of(employee));
        when(employeeMapper.toResponseDetail(employee)).thenReturn(response);

        EmployeeInformationDetailResponse result = employeeInformationService
                .employeeInformationDetailResponse("EMP001");

        assertNotNull(result);
        assertEquals("EMP001", result.getCode());
        verify(employeeInformationRepository).findDetailedByCode("EMP001");
        verify(employeeMapper).toResponseDetail(employee);
    }

    @Test
    void testEmployeeInformationDetailResponse_NotFound() {
        when(employeeInformationRepository.findDetailedByCode("EMP404"))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            employeeInformationService.employeeInformationDetailResponse("EMP404");
        });
    }

    // --- updateEmployeeInformation() ---

    @Test
    void testUpdateEmployeeInformation_Success() {
        EmployeeInformationRequest request = new EmployeeInformationRequest();
        when(employeeInformationRepository.findByCode("EMP001"))
                .thenReturn(Optional.of(employee));

        String result = employeeInformationService.updateEmployeeInformation(request, "EMP001");

        assertEquals("EMP001", result);
        verify(employeeInformationRepository).findByCode("EMP001");
        verify(employeeMapper).updateEmployeeFromDto(request, employee);
    }

    @Test
    void testUpdateEmployeeInformation_NotFound() {
        EmployeeInformationRequest request = new EmployeeInformationRequest();
        when(employeeInformationRepository.findByCode("EMP404"))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            employeeInformationService.updateEmployeeInformation(request, "EMP404");
        });
    }

    // --- deleteEmployeeByCode() ---

    @Test
    void testDeleteEmployeeByCode() {
        String result = employeeInformationService.deleteEmployeeByCode("EMP001");

        assertEquals("EMP001", result);
        verify(employeeInformationRepository).deleteByCode("EMP001");
    }

    // --- deleteEmployeeByCodes() ---

    @Test
    void testDeleteEmployeeByCodes() {
        List<String> codes = List.of("EMP001", "EMP002");

        List<String> result = employeeInformationService.deleteEmployeeByCodes(codes);

        assertEquals(codes, result);
        verify(employeeInformationRepository).deleteByCodes(codes);
    }
}
