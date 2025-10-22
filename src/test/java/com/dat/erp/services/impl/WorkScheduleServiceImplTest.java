package com.dat.erp.services.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dat.erp.constants.CodePrefixes;
import com.dat.erp.constants.Messages;
import com.dat.erp.constants.ShiftType;
import com.dat.erp.dto.request.WorkScheduleRequest;
import com.dat.erp.entities.Company;
import com.dat.erp.entities.EmployeeInformation;
import com.dat.erp.entities.WorkSchedule;
import com.dat.erp.exceptions.ConflictException;
import com.dat.erp.mapper.interfaces.WorkScheduleMapper;
import com.dat.erp.repositories.customrepositories.WorkScheduleRepository;
import com.dat.erp.services.EmployeeHasWorkScheduleService;
import com.dat.erp.services.SecurityContextService;
import com.dat.erp.systemconfigs.CustomUserDetails;

@ExtendWith(MockitoExtension.class)
class WorkScheduleServiceImplTest {

    @Mock
    private WorkScheduleRepository workScheduleRepository;
    @Mock
    private WorkScheduleMapper workScheduleMapper;
    @Mock
    private SecurityContextService securityContextService;
    @Mock
    private EmployeeHasWorkScheduleService employeeHasWorkScheduleService;

    @InjectMocks
    private WorkScheduleServiceImpl service;

    private CustomUserDetails currentUser;

    @BeforeEach
    void setup() {
        EmployeeInformation emp = new EmployeeInformation();
        emp.setCompany(new Company());
        currentUser = new CustomUserDetails(emp);
        when(securityContextService.getCurrentUser()).thenReturn(currentUser);
    }

    @Test
    void createWorkSchedule_conflict_throwsConflictException() {
        WorkScheduleRequest req = new WorkScheduleRequest();
        req.setShiftDate(LocalDate.now());
        req.setQuantity(5);
        req.setShiftType(ShiftType.HOUR);

        when(workScheduleRepository.findByShiftDateAndShiftTypeAndQuantityAndCompany(
                any(LocalDate.class), any(ShiftType.class), anyInt(), any(Company.class)))
                .thenReturn(Optional.of(new WorkSchedule()));

        ConflictException ex = assertThrows(ConflictException.class,
                () -> service.createWorkScheduleService(req));
        assertEquals(Messages.ERROR_WORK_SCHEDULE_EXISTS, ex.getMessage());
    }

    @Test
    void createWorkSchedule_success_returnsCodeWithPrefix() {
        WorkScheduleRequest req = new WorkScheduleRequest();
        req.setShiftDate(LocalDate.now());
        req.setQuantity(3);
        req.setShiftType(ShiftType.PRODUCT);

        when(workScheduleRepository.findByShiftDateAndShiftTypeAndQuantityAndCompany(
                any(LocalDate.class), any(ShiftType.class), anyInt(), any(Company.class)))
                .thenReturn(Optional.empty());

        WorkSchedule mapped = new WorkSchedule();
        when(workScheduleMapper.toEntity(any())).thenReturn(mapped);
        when(workScheduleRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        String code = service.createWorkScheduleService(req);
        assertNotNull(code);
        assertTrue(code.startsWith(CodePrefixes.WORK_SCHEDULE));
    }
}
