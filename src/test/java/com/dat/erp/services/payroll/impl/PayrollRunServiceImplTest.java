package com.dat.erp.services.payroll.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import com.dat.erp.constants.CodePrefixes;
import com.dat.erp.constants.Messages;
import com.dat.erp.constants.PayrollRerunMode;
import com.dat.erp.constants.PayrollRunStatus;
import com.dat.erp.dto.request.PayrollRerunRequest;
import com.dat.erp.dto.response.PayrollRerunResponse;
import com.dat.erp.dto.response.salary.MonthlySalaryCalculationResponse;
import com.dat.erp.dto.response.PagedResponse;
import com.dat.erp.dto.response.PayrollRunResponse;
import com.dat.erp.entities.Account;
import com.dat.erp.entities.Company;
import com.dat.erp.entities.EmployeeSalary;
import com.dat.erp.entities.PayrollResult;
import com.dat.erp.entities.PayrollRun;
import com.dat.erp.entities.UserProfile;
import com.dat.erp.exceptions.BadRequestException;
import com.dat.erp.exceptions.ConflictException;
import com.dat.erp.mapper.interfaces.PayrollRunMapper;
import com.dat.erp.repositories.customrepositories.CompanyRepository;
import com.dat.erp.repositories.customrepositories.EmployeeSalaryRepository;
import com.dat.erp.repositories.customrepositories.PayrollResultDetailRepository;
import com.dat.erp.repositories.customrepositories.PayrollResultRepository;
import com.dat.erp.repositories.customrepositories.PayrollResultSnapshotRepository;
import com.dat.erp.repositories.customrepositories.PayrollRunAuditLogRepository;
import com.dat.erp.repositories.customrepositories.PayrollRunRepository;
import com.dat.erp.services.CodeGenerator;
import com.dat.erp.services.MonthlySalaryCalculationService;
import com.dat.erp.services.SecurityContextService;
import com.dat.erp.services.payroll.PayrollResultDetailService;
import com.dat.erp.services.payroll.PayrollResultService;
import com.dat.erp.systemconfigs.CustomUserDetails;
import com.dat.erp.utils.CompanySecretKeyCryptoUtils;

class PayrollRunServiceImplTest {

    @Mock
    private PayrollRunRepository payrollRunRepository;

    @Mock
    private PayrollResultService payrollResultService;

    @Mock
    private PayrollResultRepository payrollResultRepository;

    @Mock
    private PayrollResultDetailRepository payrollResultDetailRepository;

    @Mock
    private PayrollResultSnapshotRepository payrollResultSnapshotRepository;

    @Mock
    private PayrollRunAuditLogRepository payrollRunAuditLogRepository;

    @Mock
    private EmployeeSalaryRepository employeeSalaryRepository;

    @Mock
    private MonthlySalaryCalculationService monthlySalaryCalculationService;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private PayrollResultDetailService payrollResultDetailService;

    @Mock
    private CodeGenerator codeGenerator;

    @Mock
    private SecurityContextService securityContextService;

    @Spy
    private PayrollRunMapper payrollRunMapper = Mappers.getMapper(PayrollRunMapper.class);

    @InjectMocks
    private PayrollRunServiceImpl payrollRunService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getPayrollRuns_success() {
        Account account = new Account();
        account.setCode("ACC-1");
        account.setCompanyCode("CMP-1");
        when(securityContextService.getCurrentUser()).thenReturn(new CustomUserDetails(account, null));

        PayrollRun payrollRun = new PayrollRun();
        payrollRun.setCode("PRN-000001");
        payrollRun.setPeriod("2026-04");
        payrollRun.setStatus(PayrollRunStatus.CALCULATED);
        payrollRun.setRunAt(LocalDateTime.of(2026, 4, 1, 10, 30));
        payrollRun.setClosedAt(LocalDateTime.of(2026, 4, 1, 11, 0));
        payrollRun.setCreatedBy("ACC-1");
        payrollRun.setUpdatedBy("ACC-2");

        LocalDateTime runAtFrom = LocalDateTime.of(2026, 4, 1, 0, 0);
        LocalDateTime runAtTo = LocalDateTime.of(2026, 4, 30, 23, 59);
        when(payrollRunRepository.searchByConditions(
                eq("CMP-1"),
                eq(PayrollRunStatus.CALCULATED),
                eq(runAtFrom),
                eq(runAtTo),
                eq(LocalDateTime.of(1900, 1, 1, 0, 0)),
                eq(LocalDateTime.of(1900, 1, 1, 0, 0)),
                eq(LocalDateTime.of(2999, 12, 31, 23, 59, 59)),
                eq(LocalDateTime.of(1900, 1, 1, 0, 0)),
                any()))
                        .thenReturn(new PageImpl<>(List.of(payrollRun), PageRequest.of(0, 20), 1));

        PagedResponse<PayrollRunResponse> response = payrollRunService.getPayrollRuns(
                PayrollRunStatus.CALCULATED,
                runAtFrom,
                runAtTo,
                null,
                null,
                0,
                20,
                null,
                "DESC");

        assertNotNull(response);
        assertEquals(1, response.getData().size());
        assertEquals("PRN-000001", response.getData().get(0).getCode());
        assertEquals("ACC-1", response.getData().get(0).getRunBy());
        assertEquals("ACC-2", response.getData().get(0).getUpdatedBy());
        assertEquals(Messages.SUCCESS, response.getMessage());
    }

    @Test
    void getPayrollRuns_badRequestWhenRunAtRangeInvalid() {
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> payrollRunService.getPayrollRuns(
                        null,
                        LocalDateTime.of(2026, 4, 2, 0, 0),
                        LocalDateTime.of(2026, 4, 1, 0, 0),
                        null,
                        null,
                        0,
                        20,
                        null,
                        "DESC"));

        assertEquals(Messages.ERROR_PAYROLL_RUN_RUN_AT_RANGE_INVALID, exception.getMessage());
        verify(payrollRunRepository, never()).searchByConditions(
                any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void getPayrollRuns_badRequestWhenCloseAtRangeInvalid() {
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> payrollRunService.getPayrollRuns(
                        null,
                        null,
                        null,
                        LocalDateTime.of(2026, 4, 2, 0, 0),
                        LocalDateTime.of(2026, 4, 1, 0, 0),
                        0,
                        20,
                        null,
                        "DESC"));

        assertEquals(Messages.ERROR_PAYROLL_RUN_CLOSE_AT_RANGE_INVALID, exception.getMessage());
        verify(payrollRunRepository, never()).searchByConditions(
                any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void runPayroll_success() {
        Account account = new Account();
        account.setCode("ACC-1");
        account.setCompanyCode("CMP-1");
        when(securityContextService.getCurrentUser()).thenReturn(new CustomUserDetails(account, null));

        YearMonth requestedRunMonth = YearMonth.now().minusMonths(1);
        String requestedPeriod = requestedRunMonth.toString();
        when(payrollRunRepository.findByCompanyCodeAndPeriodAndIsDeletedFalse("CMP-1", requestedPeriod))
                .thenReturn(Optional.empty());
        when(codeGenerator.nextCode("PRN-")).thenReturn("PRN-000001");
        when(payrollRunRepository.save(any(PayrollRun.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doAnswer(invocation -> {
            PayrollRun payrollRun = invocation.getArgument(0);
            payrollRun.setStatus(PayrollRunStatus.CALCULATED);
            return null;
        }).when(payrollResultService).generatePayrollResult(any(PayrollRun.class));

        PayrollRunResponse response = payrollRunService.runPayroll(requestedRunMonth);

        assertNotNull(response);
        assertEquals("PRN-000001", response.getCode());
        assertEquals(requestedPeriod, response.getPeriod());
        assertEquals(PayrollRunStatus.CALCULATED, response.getStatus());
        assertNotNull(response.getRunAt());
        assertEquals("ACC-1", response.getRunBy());
        assertEquals("ACC-1", response.getUpdatedBy());

        ArgumentCaptor<PayrollRun> payrollRunCaptor = ArgumentCaptor.forClass(PayrollRun.class);
        verify(payrollRunRepository).save(payrollRunCaptor.capture());
        PayrollRun savedPayrollRun = payrollRunCaptor.getValue();
        assertEquals("CMP-1", savedPayrollRun.getCompanyCode());
        assertEquals("PRN-000001", savedPayrollRun.getCode());
        assertEquals(requestedPeriod, savedPayrollRun.getPeriod());
        assertEquals("ACC-1", savedPayrollRun.getCreatedBy());
        assertNotNull(savedPayrollRun.getRunAt());
        verify(payrollResultService).generatePayrollResult(savedPayrollRun);
    }

    @Test
    void runPayroll_conflictWhenRequestedPeriodAlreadyExists() {
        Account account = new Account();
        account.setCode("ACC-1");
        account.setCompanyCode("CMP-1");
        when(securityContextService.getCurrentUser()).thenReturn(new CustomUserDetails(account, null));

        YearMonth requestedRunMonth = YearMonth.now();
        String requestedPeriod = requestedRunMonth.toString();
        when(payrollRunRepository.findByCompanyCodeAndPeriodAndIsDeletedFalse("CMP-1", requestedPeriod))
                .thenReturn(Optional.of(new PayrollRun()));

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> payrollRunService.runPayroll(requestedRunMonth));

        assertEquals(Messages.ERROR_PAYROLL_RUN_ALREADY_EXISTS, exception.getMessage());
        verify(payrollRunRepository, never()).save(any(PayrollRun.class));
        verify(payrollResultService, never()).generatePayrollResult(any(PayrollRun.class));
    }

    @Test
    void runPayroll_badRequestWhenRunMonthTooOld() {
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> payrollRunService.runPayroll(YearMonth.now().minusMonths(4)));

        assertEquals(Messages.ERROR_PAYROLL_RUN_MONTH_TOO_OLD, exception.getMessage());
        verify(payrollRunRepository, never()).findByCompanyCodeAndPeriodAndIsDeletedFalse(any(), any());
        verify(payrollRunRepository, never()).save(any(PayrollRun.class));
        verify(payrollResultService, never()).generatePayrollResult(any(PayrollRun.class));
    }

    @Test
    void runPayroll_badRequestWhenCompanyMissing() {
        Account account = new Account();
        account.setCode("ACC-1");
        account.setCompanyCode(" ");
        when(securityContextService.getCurrentUser()).thenReturn(new CustomUserDetails(account, null));

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> payrollRunService.runPayroll(YearMonth.now()));

        assertEquals(Messages.ERROR_CURRENT_USER_COMPANY_MISSING, exception.getMessage());
        verify(payrollRunRepository, never()).save(any(PayrollRun.class));
        verify(payrollResultService, never()).generatePayrollResult(any(PayrollRun.class));
    }

    @Test
    void rerunPayroll_badRequestWhenReasonMissing() {
        mockCurrentUser();
        PayrollRerunRequest request = new PayrollRerunRequest();
        request.setMode(PayrollRerunMode.FULL_RUN);
        request.setReason(" ");

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> payrollRunService.rerunPayroll("PRN-1", request));

        assertEquals(Messages.ERROR_PAYROLL_RERUN_REASON_REQUIRED, exception.getMessage());
        verify(payrollRunRepository, never()).findLockedByCodeAndCompanyCode(any(), any());
    }

    @Test
    void rerunPayroll_conflictWhenClosed() {
        mockCurrentUser();
        PayrollRun payrollRun = payrollRun("PRN-1", PayrollRunStatus.CLOSED);
        when(payrollRunRepository.findLockedByCodeAndCompanyCode("PRN-1", "CMP-1"))
                .thenReturn(Optional.of(payrollRun));

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> payrollRunService.rerunPayroll("PRN-1", rerunRequest(PayrollRerunMode.FULL_RUN, false)));

        assertEquals(Messages.ERROR_PAYROLL_RERUN_CLOSED_NOT_ALLOWED, exception.getMessage());
        verify(payrollResultRepository, never()).findActiveByRunAndCompany(any(), any());
    }

    @Test
    void rerunPayroll_conflictWhenRerunning() {
        mockCurrentUser();
        PayrollRun payrollRun = payrollRun("PRN-1", PayrollRunStatus.RERUNNING);
        when(payrollRunRepository.findLockedByCodeAndCompanyCode("PRN-1", "CMP-1"))
                .thenReturn(Optional.of(payrollRun));

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> payrollRunService.rerunPayroll("PRN-1", rerunRequest(PayrollRerunMode.FULL_RUN, false)));

        assertEquals(Messages.ERROR_PAYROLL_RERUN_CONCURRENT, exception.getMessage());
    }

    @Test
    void rerunPayroll_dryRunDoesNotReplacePayrollResult() {
        mockCurrentUser();
        String secretKey = "1234567890123456";
        PayrollRun payrollRun = payrollRun("PRN-1", PayrollRunStatus.CALCULATED);
        PayrollResult oldResult = payrollResult("PRR-OLD", "EMP001", secretKey, "1000.00");
        Company company = new Company();
        company.setSecretKey(secretKey);

        when(codeGenerator.nextCode(CodePrefixes.PAYROLL_RERUN_BATCH)).thenReturn("PRB-1");
        when(codeGenerator.nextCode(CodePrefixes.PAYROLL_RUN_AUDIT_LOG)).thenReturn("PRA-1", "PRA-2", "PRA-3", "PRA-4");
        when(codeGenerator.nextCode(CodePrefixes.PAYROLL_RESULT)).thenReturn("PRR-NEW");
        when(payrollRunRepository.findLockedByCodeAndCompanyCode("PRN-1", "CMP-1"))
                .thenReturn(Optional.of(payrollRun));
        when(payrollResultRepository.findActiveByRunAndCompany("PRN-1", "CMP-1"))
                .thenReturn(List.of(oldResult));
        when(companyRepository.findByCode("CMP-1")).thenReturn(Optional.of(company));
        when(employeeSalaryRepository.findFirstActiveByEmployeeCodeAndCompanyCodeAndDate(
                eq("EMP001"), eq("CMP-1"), any()))
                        .thenReturn(Optional.of(oldResult.getEmployeeSalary()));
        when(monthlySalaryCalculationService.calculateEmployeeMonthlySalary(eq("EMP001"), any()))
                .thenReturn(new MonthlySalaryCalculationResponse(
                        "EMP001",
                        YearMonth.parse("2026-06"),
                        BigDecimal.valueOf(176),
                        BigDecimal.valueOf(176),
                        BigDecimal.TEN,
                        BigDecimal.valueOf(1200),
                        java.util.Map.of(),
                        List.of()));

        PayrollRerunResponse response = payrollRunService.rerunPayroll(
                "PRN-1", rerunRequest(PayrollRerunMode.FULL_RUN, true));

        assertEquals(PayrollRunStatus.CALCULATED, response.getStatus());
        assertEquals(1, response.getSuccessCount());
        assertEquals(0, response.getFailedCount());
        assertEquals(BigDecimal.valueOf(200).setScale(2), response.getResults().get(0).getDifferenceAmount());
        verify(payrollResultRepository, never()).save(any(PayrollResult.class));
        verify(payrollResultSnapshotRepository, never()).save(any());
        verify(payrollRunAuditLogRepository, atLeastOnce()).save(any());
    }

    private void mockCurrentUser() {
        Account account = new Account();
        account.setCode("ACC-1");
        account.setCompanyCode("CMP-1");
        when(securityContextService.getCurrentUser()).thenReturn(new CustomUserDetails(account, null));
    }

    private PayrollRerunRequest rerunRequest(PayrollRerunMode mode, boolean dryRun) {
        PayrollRerunRequest request = new PayrollRerunRequest();
        request.setReason("Timesheet data was updated");
        request.setMode(mode);
        request.setDryRun(dryRun);
        return request;
    }

    private PayrollRun payrollRun(String code, PayrollRunStatus status) {
        PayrollRun payrollRun = new PayrollRun();
        payrollRun.setCode(code);
        payrollRun.setCompanyCode("CMP-1");
        payrollRun.setPeriod("2026-06");
        payrollRun.setStatus(status);
        return payrollRun;
    }

    private PayrollResult payrollResult(String code, String employeeCode, String secretKey, String actualAmount) {
        UserProfile userProfile = new UserProfile();
        userProfile.setCode(employeeCode);
        EmployeeSalary employeeSalary = new EmployeeSalary();
        employeeSalary.setCode("ESL-1");
        employeeSalary.setUserProfile(userProfile);
        employeeSalary.setTotalAmount(CompanySecretKeyCryptoUtils.encrypt("1000.00", secretKey));
        employeeSalary.setCurrency("USD");

        PayrollResult payrollResult = new PayrollResult();
        payrollResult.setCode(code);
        payrollResult.setCompanyCode("CMP-1");
        payrollResult.setPayrollRun(payrollRun("PRN-1", PayrollRunStatus.CALCULATED));
        payrollResult.setEmployeeSalary(employeeSalary);
        payrollResult.setExpectedAmount(CompanySecretKeyCryptoUtils.encrypt("1000.00", secretKey));
        payrollResult.setActualAmount(CompanySecretKeyCryptoUtils.encrypt(actualAmount, secretKey));
        payrollResult.setCurrency("USD");
        payrollResult.setExpectedQuantity(176);
        payrollResult.setActualQuantity(176);
        payrollResult.setIsDeleted(false);
        return payrollResult;
    }
}
