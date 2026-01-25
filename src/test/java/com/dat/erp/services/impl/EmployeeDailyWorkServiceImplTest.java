package com.dat.erp.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.dat.erp.constants.DailyWorkUnit;
import com.dat.erp.constants.DailyWorkWorkType;
import com.dat.erp.constants.Messages;
import com.dat.erp.dto.request.EmployeeDailyWorkRequest;
import com.dat.erp.entities.Account;
import com.dat.erp.entities.DailyWork;
import com.dat.erp.entities.UserProfile;
import com.dat.erp.exceptions.BadRequestException;
import com.dat.erp.exceptions.ConflictException;
import com.dat.erp.exceptions.ResourceNotFoundException;
import com.dat.erp.repositories.customrepositories.DailyWorkRepository;
import com.dat.erp.repositories.customrepositories.UserProfileRepository;
import com.dat.erp.services.CodeGenerator;
import com.dat.erp.services.SecurityContextService;
import com.dat.erp.systemconfigs.CustomUserDetails;

class EmployeeDailyWorkServiceImplTest {

    @Mock
    private DailyWorkRepository dailyWorkRepository;

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private CodeGenerator codeGenerator;

    @Mock
    private SecurityContextService securityContextService;

    @InjectMocks
    private EmployeeDailyWorkServiceImpl employeeDailyWorkService;

    private List<EmployeeDailyWorkRequest> requests;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        EmployeeDailyWorkRequest request = new EmployeeDailyWorkRequest();
        request.setUserProfileCode("EMP001");
        request.setWorkingDate(LocalDate.of(2026, 1, 25));
        request.setStartTime(LocalTime.of(9, 0));
        request.setEndTime(LocalTime.of(18, 0));
        request.setQuantity(1);
        request.setUnit(DailyWorkUnit.DAY);
        request.setWorkType(DailyWorkWorkType.NORMAL);
        request.setUsedPto(false);
        request.setOtTime(2);

        requests = List.of(request);
    }

    @Test
    void createEmployeeDailyWorks_success() {
        Account currentUserAccount = new Account();
        currentUserAccount.setCode("ACC-1");
        currentUserAccount.setCompanyCode("CMP-1");
        when(securityContextService.getCurrentUser()).thenReturn(new CustomUserDetails(currentUserAccount, null));

        Account employeeAccount = new Account();
        employeeAccount.setCompanyCode("CMP-1");
        UserProfile employee = new UserProfile();
        employee.setCode("EMP001");
        employee.setAccount(employeeAccount);
        employee.setIsActive(true);
        when(userProfileRepository.findByCode("EMP001")).thenReturn(Optional.of(employee));

        when(dailyWorkRepository.existsByUserProfile_CodeAndWorkingDateAndIsDeletedFalse(eq("EMP001"), any()))
                .thenReturn(false);
        when(codeGenerator.nextCode("DWK-")).thenReturn("DWK-000001");
        when(dailyWorkRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        String result = employeeDailyWorkService.createEmployeeDailyWorks(requests);

        assertEquals(String.format(Messages.DAILY_WORK_CREATE_SUCCESS, "EMP001"), result);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<DailyWork>> captor = ArgumentCaptor.forClass(List.class);
        verify(dailyWorkRepository).saveAll(captor.capture());
        assertEquals(1, captor.getValue().size());
        DailyWork persisted = captor.getValue().get(0);
        assertEquals("DWK-000001", persisted.getCode());
        assertEquals(LocalDate.of(2026, 1, 25), persisted.getWorkingDate());
        assertNotNull(persisted.getHoursWorked());
    }

    @Test
    void createEmployeeDailyWorks_conflictWhenDuplicateInPayload() {
        EmployeeDailyWorkRequest copy = new EmployeeDailyWorkRequest();
        copy.setUserProfileCode("EMP001");
        copy.setWorkingDate(LocalDate.of(2026, 1, 25));
        copy.setStartTime(LocalTime.of(9, 0));
        copy.setEndTime(LocalTime.of(18, 0));
        copy.setQuantity(1);
        copy.setUnit(DailyWorkUnit.DAY);
        copy.setWorkType(DailyWorkWorkType.NORMAL);

        requests = Arrays.asList(requests.get(0), copy);

        Account currentUserAccount = new Account();
        currentUserAccount.setCode("ACC-1");
        currentUserAccount.setCompanyCode("CMP-1");
        when(securityContextService.getCurrentUser()).thenReturn(new CustomUserDetails(currentUserAccount, null));

        Account employeeAccount = new Account();
        employeeAccount.setCompanyCode("CMP-1");
        UserProfile employee = new UserProfile();
        employee.setCode("EMP001");
        employee.setAccount(employeeAccount);
        employee.setIsActive(true);
        when(userProfileRepository.findByCode("EMP001")).thenReturn(Optional.of(employee));

        when(dailyWorkRepository.existsByUserProfile_CodeAndWorkingDateAndIsDeletedFalse(eq("EMP001"), any()))
                .thenReturn(false);
        when(codeGenerator.nextCode("DWK-")).thenReturn("DWK-000001");

        ConflictException ex = assertThrows(ConflictException.class,
                () -> employeeDailyWorkService.createEmployeeDailyWorks(requests));
        assertEquals(Messages.ERROR_DAILY_WORK_ALREADY_EXISTS, ex.getMessage());
        verify(dailyWorkRepository, never()).saveAll(anyList());
    }

    @Test
    void createEmployeeDailyWorks_conflictWhenAlreadyExistsInDb() {
        Account currentUserAccount = new Account();
        currentUserAccount.setCode("ACC-1");
        currentUserAccount.setCompanyCode("CMP-1");
        when(securityContextService.getCurrentUser()).thenReturn(new CustomUserDetails(currentUserAccount, null));

        Account employeeAccount = new Account();
        employeeAccount.setCompanyCode("CMP-1");
        UserProfile employee = new UserProfile();
        employee.setCode("EMP001");
        employee.setAccount(employeeAccount);
        employee.setIsActive(true);
        when(userProfileRepository.findByCode("EMP001")).thenReturn(Optional.of(employee));

        when(dailyWorkRepository.existsByUserProfile_CodeAndWorkingDateAndIsDeletedFalse(eq("EMP001"), any()))
                .thenReturn(true);

        ConflictException ex = assertThrows(ConflictException.class,
                () -> employeeDailyWorkService.createEmployeeDailyWorks(requests));
        assertEquals(Messages.ERROR_DAILY_WORK_ALREADY_EXISTS, ex.getMessage());
        verify(dailyWorkRepository, never()).saveAll(anyList());
    }

    @Test
    void createEmployeeDailyWorks_notFoundWhenEmployeeMissing() {
        Account currentUserAccount = new Account();
        currentUserAccount.setCode("ACC-1");
        currentUserAccount.setCompanyCode("CMP-1");
        when(securityContextService.getCurrentUser()).thenReturn(new CustomUserDetails(currentUserAccount, null));

        when(userProfileRepository.findByCode("EMP001")).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> employeeDailyWorkService.createEmployeeDailyWorks(requests));
        assertEquals(Messages.ERROR_DAILY_WORK_EMPLOYEE_NOT_FOUND, ex.getMessage());
        verify(dailyWorkRepository, never()).saveAll(anyList());
    }

    @Test
    void createEmployeeDailyWorks_badRequestWhenStartEndInvalid() {
        requests.get(0).setStartTime(LocalTime.of(18, 0));
        requests.get(0).setEndTime(LocalTime.of(9, 0));

        Account currentUserAccount = new Account();
        currentUserAccount.setCode("ACC-1");
        currentUserAccount.setCompanyCode("CMP-1");
        when(securityContextService.getCurrentUser()).thenReturn(new CustomUserDetails(currentUserAccount, null));

        Account employeeAccount = new Account();
        employeeAccount.setCompanyCode("CMP-1");
        UserProfile employee = new UserProfile();
        employee.setCode("EMP001");
        employee.setAccount(employeeAccount);
        employee.setIsActive(true);
        when(userProfileRepository.findByCode("EMP001")).thenReturn(Optional.of(employee));

        when(dailyWorkRepository.existsByUserProfile_CodeAndWorkingDateAndIsDeletedFalse(eq("EMP001"), any()))
                .thenReturn(false);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> employeeDailyWorkService.createEmployeeDailyWorks(requests));
        assertEquals(Messages.ERROR_DAILY_WORK_START_END_TIME_INVALID, ex.getMessage());
        verify(dailyWorkRepository, never()).saveAll(anyList());
    }
}
