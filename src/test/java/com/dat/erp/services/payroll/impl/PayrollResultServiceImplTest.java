package com.dat.erp.services.payroll.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import com.dat.erp.constants.DayType;
import com.dat.erp.constants.Messages;
import com.dat.erp.constants.PayrollRunStatus;
import com.dat.erp.constants.PayrollStatus;
import com.dat.erp.dto.response.PagedResponse;
import com.dat.erp.dto.response.PayrollResultListResponse;
import com.dat.erp.entities.Account;
import com.dat.erp.entities.Company;
import com.dat.erp.entities.DailyWork;
import com.dat.erp.entities.EmployeeSalary;
import com.dat.erp.entities.PayrollPolicy;
import com.dat.erp.entities.PayrollResult;
import com.dat.erp.entities.PayrollRun;
import com.dat.erp.entities.SystemUnit;
import com.dat.erp.entities.UserProfile;
import com.dat.erp.exceptions.BadRequestException;
import com.dat.erp.repositories.customrepositories.CompanyRepository;
import com.dat.erp.repositories.customrepositories.DailyWorkRepository;
import com.dat.erp.repositories.customrepositories.EmployeeSalaryRepository;
import com.dat.erp.repositories.customrepositories.PayrollResultRepository;
import com.dat.erp.repositories.customrepositories.PayrollRunRepository;
import com.dat.erp.repositories.projections.PayrollResultListProjection;
import com.dat.erp.services.CalendarDateService;
import com.dat.erp.services.CodeGenerator;
import com.dat.erp.services.EmployeePayrollPolicyService;
import com.dat.erp.services.EmployeeSalaryService;
import com.dat.erp.services.SecurityContextService;
import com.dat.erp.services.UserProfileService;
import com.dat.erp.systemconfigs.CustomUserDetails;
import com.dat.erp.utils.CompanySecretKeyCryptoUtils;

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
    private CompanyRepository companyRepository;

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
                companyRepository,
                employeeSalaryService,
                codeGenerator,
                securityContextService);
    }

    @Test
    void getPayrollResults_successWithDefaultCreatedDate() {
        Account currentUserAccount = new Account();
        currentUserAccount.setCode("ACC-1");
        currentUserAccount.setCompanyCode("CMP-1");
        when(securityContextService.getCurrentUser()).thenReturn(new CustomUserDetails(currentUserAccount, null));

        Company company = new Company();
        company.setCode("CMP-1");
        company.setSecretKey("secret-key");
        when(companyRepository.findByCode("CMP-1")).thenReturn(java.util.Optional.of(company));

        String encryptedAmount = CompanySecretKeyCryptoUtils.encrypt("1500", "secret-key");
        PayrollResultListProjection projection = new PayrollResultListProjection() {
            @Override
            public String getPayrollRunCode() {
                return "PRN-1";
            }

            @Override
            public String getSalaryName() {
                return "Standard Payroll";
            }

            @Override
            public String getExpectedAmount() {
                return encryptedAmount;
            }

            @Override
            public String getEmployeeName() {
                return "Ann Smith";
            }

            @Override
            public String getActualAmount() {
                return "1450";
            }

            @Override
            public String getCurrency() {
                return "USD";
            }

            @Override
            public Integer getExpectedQuantity() {
                return 168;
            }

            @Override
            public Integer getActualQuantity() {
                return 160;
            }

            @Override
            public String getUnitName() {
                return "Hour";
            }

            @Override
            public PayrollStatus getSourceType() {
                return PayrollStatus.RUNNING;
            }

            @Override
            public Boolean getIsRetro() {
                return Boolean.FALSE;
            }

            @Override
            public String getRetroReason() {
                return null;
            }

            @Override
            public String getPeriod() {
                return null;
            }

            @Override
            public LocalDateTime getCreatedAt() {
                return null;
            }

            @Override
            public String getEmployeeCode() {
                return null;
            }
        };

        LocalDate today = LocalDate.now();
        when(payrollResultRepository.searchByConditions(
                eq("CMP-1"),
                eq("PRN-1"),
                eq(today.atStartOfDay()),
                eq(today.plusDays(1).atStartOfDay()),
                eq(PayrollStatus.RUNNING),
                eq("USR-1"),
                any()))
                        .thenReturn(new PageImpl<>(List.of(projection), PageRequest.of(0, 20), 1));

        PagedResponse<PayrollResultListResponse> response = payrollResultService.getPayrollResults(
                " PRN-1 ",
                null,
                PayrollStatus.RUNNING,
                " USR-1 ",
                0,
                20,
                null,
                "DESC");

        assertNotNull(response);
        assertEquals(1, response.getData().size());
        assertEquals("PRN-1", response.getData().get(0).getPayrollRunCode());
        assertEquals("Standard Payroll", response.getData().get(0).getSalaryName());
        assertEquals("1500", response.getData().get(0).getExpectedAmount());
        assertEquals("Ann Smith", response.getData().get(0).getEmployeeName());
        assertEquals("1450", response.getData().get(0).getActualAmount());
        assertEquals("Hour", response.getData().get(0).getUnitName());
        assertEquals(PayrollStatus.RUNNING, response.getData().get(0).getSourceType());
        assertEquals(Messages.SUCCESS, response.getMessage());
    }

    @Test
    void getPayrollResults_successWithExplicitCreatedDateAndNoOptionalFilters() {
        Account currentUserAccount = new Account();
        currentUserAccount.setCode("ACC-1");
        currentUserAccount.setCompanyCode("CMP-1");
        when(securityContextService.getCurrentUser()).thenReturn(new CustomUserDetails(currentUserAccount, null));

        Company company = new Company();
        company.setCode("CMP-1");
        company.setSecretKey("secret-key");
        when(companyRepository.findByCode("CMP-1")).thenReturn(java.util.Optional.of(company));

        LocalDate createdDate = LocalDate.of(2026, 4, 1);
        when(payrollResultRepository.searchByConditions(
                eq("CMP-1"),
                eq("PRN-2"),
                eq(createdDate.atStartOfDay()),
                eq(createdDate.plusDays(1).atStartOfDay()),
                isNull(),
                isNull(),
                any()))
                        .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 20), 0));

        PagedResponse<PayrollResultListResponse> response = payrollResultService.getPayrollResults(
                "PRN-2",
                createdDate,
                null,
                null,
                0,
                20,
                "employeeName",
                "ASC");

        assertNotNull(response);
        assertEquals(0, response.getData().size());
        assertEquals(Messages.SUCCESS, response.getMessage());
    }

    @Test
    void getPayrollResults_blankPayrollRunCode_throwsBadRequest() {
        Account currentUserAccount = new Account();
        currentUserAccount.setCode("ACC-1");
        currentUserAccount.setCompanyCode("CMP-1");
        when(securityContextService.getCurrentUser()).thenReturn(new CustomUserDetails(currentUserAccount, null));

        Company company = new Company();
        company.setCode("CMP-1");
        company.setSecretKey("secret-key");
        when(companyRepository.findByCode("CMP-1")).thenReturn(java.util.Optional.of(company));

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> payrollResultService.getPayrollResults(
                        "   ",
                        null,
                        null,
                        null,
                        0,
                        20,
                        null,
                        "DESC"));

        assertEquals(Messages.ERROR_PAYROLL_RUN_CODE_INVALID, exception.getMessage());
        verify(payrollResultRepository, never()).searchByConditions(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void generatePayrollResult_createsPreviewResultsForActiveEmployees() {
        YearMonth targetMonth = YearMonth.of(2026, 2);
        LocalDate runDate = targetMonth.atEndOfMonth();
        PayrollRun payrollRun = new PayrollRun();
        payrollRun.setCode("PRN-1");
        payrollRun.setPeriod(targetMonth.toString());

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

        when(employeePayrollPolicyService.getCompanyPoliciesByEmployeeCodesAndDate(List.of("USR-1", "USR-2"), runDate))
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
                runDate))
                        .thenReturn(List.of(firstSalary, secondSalary));

        when(codeGenerator.nextCode("PRR-")).thenReturn("PRR-1", "PRR-2");
        when(payrollRunRepository.save(any(PayrollRun.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(payrollResultRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        when(calendarDateService.getCalendarDateTotalsByCompanyCodeAndMonth("CMP-1", targetMonth))
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
                targetMonth.atDay(1),
                targetMonth.atEndOfMonth()))
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
        assertEquals(targetMonth.toString(), finalSavedRun.getPeriod());
        assertEquals(PayrollRunStatus.CALCULATED, finalSavedRun.getStatus());

        ArgumentCaptor<List<String>> employeeCodesCaptor = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<List<PayrollResult>> payrollResultsForCalculationCaptor = ArgumentCaptor.forClass(List.class);
        verify(employeeSalaryService).employeeSalaryCalculation(
                employeeCodesCaptor.capture(),
                payrollResultsForCalculationCaptor.capture(),
                eq(runDate));
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
                dailyWorkRepository, payrollRunRepository, payrollResultRepository, companyRepository, employeeSalaryService);
        verify(codeGenerator, never()).nextCode(any());
    }
}
