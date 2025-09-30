package com.dat.erp.services.impl;

import com.dat.erp.constants.CommonStatus;
import com.dat.erp.dto.request.EmployeeInformationRequest;
import com.dat.erp.dto.response.EmployeeInformationResponse;
import com.dat.erp.dto.response.PagedResponse;
import com.dat.erp.entities.EmployeeInformation;
import com.dat.erp.mapper.interfaces.EmployeeMapper;
import com.dat.erp.repositories.customrepositories.EmployeeInformationRepository;
import com.dat.erp.utils.PageableUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class EmployeeInformationServiceImplTest {

    @Mock
    private EmployeeInformationRepository employeeInformationRepository;

    @Mock
    private EmployeeMapper employeeMapper;

    @InjectMocks
    private EmployeeInformationServiceImpl employeeInformationService;

    private EmployeeInformationRequest request;
    private EmployeeInformation entity;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        request = new EmployeeInformationRequest();
        request.setNickname("John Doe");

        entity = new EmployeeInformation();
        entity.setNickname("John Doe");
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
        Page<EmployeeInformation> page = new PageImpl<>(Collections.singletonList(entity));
        when(employeeInformationRepository.findAll(any(Pageable.class))).thenReturn(page);

        EmployeeInformationResponse response = new EmployeeInformationResponse();
        when(employeeMapper.toResponse(entity)).thenReturn(response);

        PagedResponse<EmployeeInformationResponse> result = employeeInformationService
                .getListEmployeeInformationResponse(
                        "name", "John", 0, 10, "id", "asc");

        assertNotNull(result);
        assertEquals(1, result.getData().size());
        assertSame(response, result.getData().get(0));
    }
}
