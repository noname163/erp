package com.dat.erp.services.payroll.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.dat.erp.constants.DayType;
import com.dat.erp.constants.Messages;
import com.dat.erp.constants.PayrollRunStatus;
import com.dat.erp.constants.PayrollStatus;
import com.dat.erp.entities.Account;
import com.dat.erp.entities.DailyWork;
import com.dat.erp.entities.EmployeeSalary;
import com.dat.erp.entities.PayrollPolicy;
import com.dat.erp.entities.PayrollResult;
import com.dat.erp.entities.PayrollRun;
import com.dat.erp.entities.SystemUnit;
import com.dat.erp.entities.UserProfile;
import com.dat.erp.exceptions.BadRequestException;
import com.dat.erp.repositories.customrepositories.DailyWorkRepository;
import com.dat.erp.repositories.customrepositories.EmployeeSalaryRepository;
import com.dat.erp.repositories.customrepositories.PayrollResultRepository;
import com.dat.erp.repositories.customrepositories.PayrollRunRepository;
import com.dat.erp.services.CalendarDateService;
import com.dat.erp.services.CodeGenerator;
import com.dat.erp.services.EmployeePayrollPolicyService;
import com.dat.erp.services.EmployeeSalaryService;
import com.dat.erp.services.SecurityContextService;
import com.dat.erp.services.UserProfileService;
import com.dat.erp.systemconfigs.CustomUserDetails;

class PayrollResultServiceImplTest {

    @Mock
    private UserProfileService userProfileService;

    @Mock
    private EmployeePayrollPolicyService employeePayrollPolicyService;

    @Mock
    private CalendarDateService calendarDateService;

    @Mock
    private EmployeeSalaryRepository employeeSalaryRepository;

    @Mock
    private DailyWorkRepository dailyWorkRepository;

    @Mock
    private PayrollRunRepository payrollRunRepository;

    @Mock
    private PayrollResultRepository payrollResultRepository;

    @Mock
    private EmployeeSalaryService employeeSalaryService;

    @Mock
    private CodeGenerator codeGenerator;

    @Mock
    private SecurityContextService securityContextService;

    private PayrollResultServiceImpl payrollResultService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        payrollResultService = new PayrollResultServiceImpl(
                userProfileService,
                employeePayrollPolicyService,
                calendarDateService,
                employeeSalaryRepository,
                dailyWorkRepository,
                payrollRunRepository,
                payrollResultRepository,
                employeeSalaryService,
                codeGenerator,
                securityContextService);
    }

    @Test
    void generatePayrollResult_createsPreviewResultsForActiveEmployees() {
        LocalDate today = LocalDate.now();
        YearMonth currentMonth = YearMonth.from(today);
        PayrollRun payrollRun = new PayrollRun();
        payrollRun.setCode("PRN-1");
        payrollRun.setPeriod(currentMonth.toString());

        Account currentUserAccount = new Account();
        currentUserAccount.setCode("ACC-1");
        currentUserAccount.setCompanyCode("CMP-1");
        when(securityContextService.getCurrentUser()).thenReturn(new CustomUserDetails(currentUserAccount, null));

        when(userProfileService.getActiveUserProfileCodesOfCurrentCompany()).thenReturn(List.of("USR-1", "USR-2"));

        SystemUnit workingHourUnit = new SystemUnit();
        workingHourUnit.setCode("UNT-1");

        PayrollPolicy firstPolicy = new PayrollPolicy();
        firstPolicy.setCode("PPL-1");
        firstPolicy.setStandardQuantityPerDay(8);
        firstPolicy.setUnit(workingHourUnit);

        PayrollPolicy secondPolicy = new PayrollPolicy();
        secondPolicy.setCode("PPL-2");
        secondPolicy.setStandardQuantityPerDay(8);
        secondPolicy.setUnit(workingHourUnit);

        when(employeePayrollPolicyService.getCompanyPoliciesByEmployeeCodesAndDate(List.of("USR-1", "USR-2"), today))
                .thenReturn(Map.of("USR-1", firstPolicy, "USR-2", secondPolicy));

        UserProfile firstUserProfile = new UserProfile();
        firstUserProfile.setCode("USR-1");
        UserProfile secondUserProfile = new UserProfile();
        secondUserProfile.setCode("USR-2");

        EmployeeSalary firstSalary = EmployeeSalary.builder()
                .userProfile(firstUserProfile)
                .totalAmount("ENC-100")
                .currency("MMK")
                .build();
        EmployeeSalary secondSalary = EmployeeSalary.builder()
                .userProfile(secondUserProfile)
                .totalAmount("ENC-200")
                .currency("USD")
                .build();

        when(employeeSalaryRepository.findActiveByCompanyCodeAndUserProfileCodesAndDate("CMP-1",
                List.of("USR-1", "USR-2"),
                today))
                        .thenReturn(List.of(firstSalary, secondSalary));

        when(codeGenerator.nextCode("PRR-")).thenReturn("PRR-1", "PRR-2");
        when(payrollRunRepository.save(any(PayrollRun.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(payrollResultRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        when(calendarDateService.getCalendarDateTotalsByCompanyCodeAndMonth("CMP-1", currentMonth))
                .thenReturn(Map.of(
                        DayType.NORMAL, 20,
                        DayType.HOLIDAY, 2,
                        DayType.WEEKEND_WORK, 1));

        DailyWork paidLeave = DailyWork.builder()
                .userProfile(firstUserProfile)
                .workType(DayType.PTO_PAID)
                .hoursWorked(new BigDecimal("8"))
                .build();
        DailyWork unpaidLeave = DailyWork.builder()
                .userProfile(secondUserProfile)
                .workType(DayType.UNPAID_LEAVE)
                .hoursWorked(new BigDecimal("16"))
                .build();
        DailyWork normalWork = DailyWork.builder()
                .userProfile(firstUserProfile)
                .workType(DayType.NORMAL)
                .hoursWorked(new BigDecimal("4"))
                .build();

        when(dailyWorkRepository.findAllForSalaryByCompanyCodeAndEmployeeCodesAndWorkingDateBetween(
                "CMP-1",
                List.of("USR-1", "USR-2"),
                currentMonth.atDay(1),
                currentMonth.atEndOfMonth()))
                        .thenReturn(List.of(paidLeave, unpaidLeave, normalWork));

        payrollResultService.generatePayrollResult(payrollRun);

        ArgumentCaptor<List<PayrollResult>> payrollResultsCaptor = ArgumentCaptor.forClass(List.class);
        verify(payrollResultRepository).saveAll(payrollResultsCaptor.capture());
        List<PayrollResult> savedResults = payrollResultsCaptor.getValue();

        assertEquals(2, savedResults.size());

        PayrollResult firstResult = savedResults.get(0);
        assertEquals("PRN-1", firstResult.getPayrollRun().getCode());
        assertEquals("ENC-100", firstResult.getExpectedAmount());
        assertEquals("MMK", firstResult.getCurrency());
        assertEquals(168, firstResult.getExpectedQuantity());
        assertEquals(null, firstResult.getActualQuantity());
        assertEquals(PayrollStatus.RUNNING, firstResult.getSourceType());
        assertFalse(firstResult.getIsRetro());
        assertEquals(workingHourUnit, firstResult.getUnit());

        PayrollResult secondResult = savedResults.get(1);
        assertEquals("ENC-200", secondResult.getExpectedAmount());
        assertEquals("USD", secondResult.getCurrency());
        assertEquals(168, secondResult.getExpectedQuantity());
        assertEquals(null, secondResult.getActualQuantity());
        assertEquals(PayrollStatus.RUNNING, secondResult.getSourceType());

        ArgumentCaptor<PayrollRun> payrollRunCaptor = ArgumentCaptor.forClass(PayrollRun.class);
        verify(payrollRunRepository).save(payrollRunCaptor.capture());
        PayrollRun finalSavedRun = payrollRunCaptor.getValue();
        assertEquals("PRN-1", finalSavedRun.getCode());
        assertEquals(currentMonth.toString(), finalSavedRun.getPeriod());
        assertEquals(PayrollRunStatus.CALCULATED, finalSavedRun.getStatus());

        ArgumentCaptor<List<String>> employeeCodesCaptor = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<List<PayrollResult>> payrollResultsForCalculationCaptor = ArgumentCaptor.forClass(List.class);
        verify(employeeSalaryService).employeeSalaryCalculation(
                employeeCodesCaptor.capture(),
                payrollResultsForCalculationCaptor.capture(),
                eq(today));
        assertEquals(List.of("USR-1", "USR-2"), employeeCodesCaptor.getValue());
        assertEquals(2, payrollResultsForCalculationCaptor.getValue().size());
    }

    @Test
    void generatePayrollResult_missingCompany_throwsBadRequest() {
        Account currentUserAccount = new Account();
        currentUserAccount.setCompanyCode(" ");
        when(securityContextService.getCurrentUser()).thenReturn(new CustomUserDetails(currentUserAccount, null));
        PayrollRun payrollRun = new PayrollRun();

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> payrollResultService.generatePayrollResult(payrollRun));

        assertEquals(Messages.ERROR_CURRENT_USER_COMPANY_MISSING, exception.getMessage());
        verifyNoInteractions(userProfileService, employeePayrollPolicyService, calendarDateService,
                employeeSalaryRepository,
                dailyWorkRepository, payrollRunRepository, payrollResultRepository, employeeSalaryService);
        verify(codeGenerator, never()).nextCode(any());
    }
}
