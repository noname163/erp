package com.dat.erp.services.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dat.erp.constants.ListCodeTypeEnum;
import com.dat.erp.constants.Messages;
import com.dat.erp.dto.request.EmployeeHasWorkScheduleRequest;
import com.dat.erp.dto.searchs.EmployeeScheduleFilter;
import com.dat.erp.entities.Company;
import com.dat.erp.entities.EmployeeHasWorkSchedule;
import com.dat.erp.entities.EmployeeInformation;
import com.dat.erp.entities.WorkSchedule;
import com.dat.erp.exceptions.ConflictException;
import com.dat.erp.mapper.interfaces.EmployeeHasWorkScheduleMapper;
import com.dat.erp.repositories.customrepositories.EmployeeHasWorkScheduleRepository;
import com.dat.erp.repositories.customrepositories.EmployeeInformationRepository;
import com.dat.erp.repositories.customrepositories.WorkScheduleRepository;
import com.dat.erp.services.SecurityContextService;
import com.dat.erp.systemconfigs.CustomUserDetails;

@ExtendWith(MockitoExtension.class)
class EmployeeHasWorkScheduleServiceImplTest {

    @Mock private EmployeeHasWorkScheduleRepository employeeHasWorkScheduleRepository;
    @Mock private WorkScheduleRepository workScheduleRepository;
    @Mock private EmployeeInformationRepository employeeInformationRepository;
    @Mock private SecurityContextService securityContextService;
    @Mock private EmployeeHasWorkScheduleMapper employeeHasWorkScheduleMapper;

    @InjectMocks private EmployeeHasWorkScheduleServiceImpl service;

    private CustomUserDetails currentUser;

    @BeforeEach
    void setup() {
        EmployeeInformation emp = new EmployeeInformation();
        emp.setCompany(new Company());
        currentUser = new CustomUserDetails(emp);
        lenient().when(securityContextService.getCurrentUser()).thenReturn(currentUser);
    }

    @Test
    void createAssignments_employeeType_conflict_throws() {
        EmployeeHasWorkScheduleRequest req = EmployeeHasWorkScheduleRequest.builder()
                .type(ListCodeTypeEnum.EMPLOYEE)
                .code("WRS-1")
                .codes(List.of("EMP-1"))
                .build();

        when(employeeInformationRepository.findByCodeInAndCompany(anyList(), any(Company.class)))
                .thenReturn(List.of(new EmployeeInformation()));
        when(workScheduleRepository.findByCode("WRS-1")).thenReturn(Optional.of(new WorkSchedule()));
        when(employeeHasWorkScheduleRepository.existsByEmployee_CodeAndWorkSchedule_Code(any(), any()))
                .thenReturn(true);

        ConflictException ex = assertThrows(ConflictException.class, () -> service.createEmployeeHasWorkSchedule(req));
        assertEquals(Messages.ERROR_EMPLOYEE_SCHEDULE_EXISTS, ex.getMessage());
    }

    @Test
    void createAssignments_employeeType_success_savesAllAndReturnsMessage() {
        EmployeeHasWorkScheduleRequest req = EmployeeHasWorkScheduleRequest.builder()
                .type(ListCodeTypeEnum.EMPLOYEE)
                .code("WRS-1")
                .codes(List.of("EMP-1", "EMP-2"))
                .build();

        when(employeeInformationRepository.findByCodeInAndCompany(anyList(), any(Company.class)))
                .thenReturn(List.of(new EmployeeInformation(), new EmployeeInformation()));
        when(workScheduleRepository.findByCode("WRS-1")).thenReturn(Optional.of(new WorkSchedule()));
        when(employeeHasWorkScheduleRepository.existsByEmployee_CodeAndWorkSchedule_Code(any(), any()))
                .thenReturn(false);

        String result = service.createEmployeeHasWorkSchedule(req);

        assertEquals(Messages.EMPLOYEE_HAS_WORK_SCHEDULE_CREATED, result);
        verify(employeeHasWorkScheduleRepository, times(1)).saveAll(anyList());
    }

    @Test
    void getEmployeeSchedule_buildsPageableAndReturnsPagedResponse() {
        EmployeeScheduleFilter filter = new EmployeeScheduleFilter(LocalDate.now(), LocalDate.now(), null, null);
        // repository findAll(spec, pageable) is used; return empty page
        org.springframework.data.domain.Page<com.dat.erp.entities.EmployeeHasWorkSchedule> emptyPage =
                new org.springframework.data.domain.PageImpl<>(java.util.List.of());
        when(employeeHasWorkScheduleRepository.findAll(
                any(org.springframework.data.jpa.domain.Specification.class),
                any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(emptyPage);

        assertDoesNotThrow(() -> service.getEmployeeSchedule(filter, 10, 0, null, null));
    }
}
