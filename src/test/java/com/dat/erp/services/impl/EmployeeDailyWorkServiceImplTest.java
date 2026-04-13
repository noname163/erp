package com.dat.erp.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mapstruct.factory.Mappers;

import com.dat.erp.constants.DailyWorkUnit;
import com.dat.erp.constants.DayType;
import com.dat.erp.constants.Messages;
import com.dat.erp.dto.request.EmployeeDailyWorkRequest;
import com.dat.erp.dto.response.salary.DailyWorkForSalaryResponse;
import com.dat.erp.entities.Account;
import com.dat.erp.entities.DailyWork;
import com.dat.erp.entities.UserProfile;
import com.dat.erp.exceptions.BadRequestException;
import com.dat.erp.exceptions.ConflictException;
import com.dat.erp.exceptions.ResourceNotFoundException;
import com.dat.erp.mapper.interfaces.EmployeeDailyWorkMapper;
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

    @Spy
    private EmployeeDailyWorkMapper employeeDailyWorkMapper = Mappers.getMapper(EmployeeDailyWorkMapper.class);

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
        request.setWorkType(DayType.NORMAL);
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
        when(userProfileRepository.findAllByCodeInAndIsDeletedFalseWithAccount(anyCollection()))
                .thenReturn(List.of(employee));

        when(dailyWorkRepository.findExistingByUserProfileCodesAndWorkingDates(
                anyCollection(),
                anyCollection()))
                .thenReturn(List.of());
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
        copy.setWorkType(DayType.NORMAL);

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
        when(userProfileRepository.findAllByCodeInAndIsDeletedFalseWithAccount(anyCollection()))
                .thenReturn(List.of(employee));
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
        when(userProfileRepository.findAllByCodeInAndIsDeletedFalseWithAccount(anyCollection()))
                .thenReturn(List.of(employee));
        when(dailyWorkRepository.findExistingByUserProfileCodesAndWorkingDates(
                anyCollection(),
                anyCollection()))
                .thenReturn(List.of(DailyWork.builder()
                        .userProfile(employee)
                        .workingDate(LocalDate.of(2026, 1, 25))
                        .build()));

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

        when(userProfileRepository.findAllByCodeInAndIsDeletedFalseWithAccount(anyCollection()))
                .thenReturn(List.of());

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
        when(userProfileRepository.findAllByCodeInAndIsDeletedFalseWithAccount(anyCollection()))
                .thenReturn(List.of(employee));

        when(dailyWorkRepository.findExistingByUserProfileCodesAndWorkingDates(
                anyCollection(),
                anyCollection()))
                .thenReturn(List.of());

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> employeeDailyWorkService.createEmployeeDailyWorks(requests));
        assertEquals(Messages.ERROR_DAILY_WORK_START_END_TIME_INVALID, ex.getMessage());
        verify(dailyWorkRepository, never()).saveAll(anyList());
    }

    @Test
    void getEmployeeDailyWorksByEmployeeCodes_returnsMappedResponsesByEmployeeCode() {
        Account currentUserAccount = new Account();
        currentUserAccount.setCode("ACC-1");
        currentUserAccount.setCompanyCode("CMP-1");
        when(securityContextService.getCurrentUser()).thenReturn(new CustomUserDetails(currentUserAccount, null));

        UserProfile employeeOne = new UserProfile();
        employeeOne.setCode("EMP001");
        UserProfile employeeTwo = new UserProfile();
        employeeTwo.setCode("EMP002");

        DailyWork employeeOneNormal = DailyWork.builder()
                .userProfile(employeeOne)
                .workType(DayType.NORMAL)
                .hoursWorked(new BigDecimal("2.50"))
                .build();
        DailyWork employeeOneNormalExtra = DailyWork.builder()
                .userProfile(employeeOne)
                .workType(DayType.NORMAL)
                .hoursWorked(new BigDecimal("1.50"))
                .build();
        DailyWork employeeOneWeekend = DailyWork.builder()
                .userProfile(employeeOne)
                .workType(DayType.WEEKEND_WORK)
                .hoursWorked(new BigDecimal("4"))
                .build();
        DailyWork employeeTwoHoliday = DailyWork.builder()
                .userProfile(employeeTwo)
                .workType(DayType.HOLIDAY_WORK)
                .hoursWorked(new BigDecimal("10"))
                .build();

        when(dailyWorkRepository.findAllForSalaryByCompanyCodeAndEmployeeCodes("CMP-1", List.of("EMP001", "EMP002")))
                .thenReturn(List.of(employeeOneNormal, employeeOneWeekend, employeeOneNormalExtra, employeeTwoHoliday));

        Map<String, List<DailyWorkForSalaryResponse>> result = employeeDailyWorkService
                .getEmployeeDailyWorksByEmployeeCodes(List.of(" EMP001 ", "EMP002", "EMP001"));

        assertEquals(2, result.size());
        assertEquals(List.of(
                new DailyWorkForSalaryResponse(DayType.NORMAL, 4),
                new DailyWorkForSalaryResponse(DayType.WEEKEND_WORK, 4)), result.get("EMP001"));
        assertEquals(List.of(new DailyWorkForSalaryResponse(DayType.HOLIDAY_WORK, 10)), result.get("EMP002"));
    }
}
