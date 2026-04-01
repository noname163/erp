package com.dat.erp.services.payroll.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import com.dat.erp.constants.Messages;
import com.dat.erp.constants.PayrollRunStatus;
import com.dat.erp.dto.response.PagedResponse;
import com.dat.erp.dto.response.PayrollRunResponse;
import com.dat.erp.entities.Account;
import com.dat.erp.entities.PayrollRun;
import com.dat.erp.exceptions.BadRequestException;
import com.dat.erp.exceptions.ConflictException;
import com.dat.erp.repositories.customrepositories.PayrollRunRepository;
import com.dat.erp.services.CodeGenerator;
import com.dat.erp.services.SecurityContextService;
import com.dat.erp.services.payroll.PayrollResultService;
import com.dat.erp.systemconfigs.CustomUserDetails;

class PayrollRunServiceImplTest {

    @Mock
    private PayrollRunRepository payrollRunRepository;

    @Mock
    private PayrollResultService payrollResultService;

    @Mock
    private CodeGenerator codeGenerator;

    @Mock
    private SecurityContextService securityContextService;

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

        String currentPeriod = YearMonth.now().toString();
        when(payrollRunRepository.findByCompanyCodeAndPeriodAndIsDeletedFalse("CMP-1", currentPeriod))
                .thenReturn(Optional.empty());
        when(codeGenerator.nextCode("PRN-")).thenReturn("PRN-000001");
        when(payrollRunRepository.save(any(PayrollRun.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doAnswer(invocation -> {
            PayrollRun payrollRun = invocation.getArgument(0);
            payrollRun.setStatus(PayrollRunStatus.CALCULATED);
            return null;
        }).when(payrollResultService).generatePayrollResult(any(PayrollRun.class));

        PayrollRunResponse response = payrollRunService.runPayroll();

        assertNotNull(response);
        assertEquals("PRN-000001", response.getCode());
        assertEquals(currentPeriod, response.getPeriod());
        assertEquals(PayrollRunStatus.CALCULATED, response.getStatus());
        assertNotNull(response.getRunAt());
        assertEquals("ACC-1", response.getRunBy());
        assertEquals("ACC-1", response.getUpdatedBy());

        ArgumentCaptor<PayrollRun> payrollRunCaptor = ArgumentCaptor.forClass(PayrollRun.class);
        verify(payrollRunRepository).save(payrollRunCaptor.capture());
        PayrollRun savedPayrollRun = payrollRunCaptor.getValue();
        assertEquals("CMP-1", savedPayrollRun.getCompanyCode());
        assertEquals("PRN-000001", savedPayrollRun.getCode());
        assertEquals(currentPeriod, savedPayrollRun.getPeriod());
        assertEquals("ACC-1", savedPayrollRun.getCreatedBy());
        assertNotNull(savedPayrollRun.getRunAt());
        verify(payrollResultService).generatePayrollResult(savedPayrollRun);
    }

    @Test
    void runPayroll_conflictWhenCurrentPeriodAlreadyExists() {
        Account account = new Account();
        account.setCode("ACC-1");
        account.setCompanyCode("CMP-1");
        when(securityContextService.getCurrentUser()).thenReturn(new CustomUserDetails(account, null));

        String currentPeriod = YearMonth.now().toString();
        when(payrollRunRepository.findByCompanyCodeAndPeriodAndIsDeletedFalse("CMP-1", currentPeriod))
                .thenReturn(Optional.of(new PayrollRun()));

        ConflictException exception = assertThrows(ConflictException.class, () -> payrollRunService.runPayroll());

        assertEquals(Messages.ERROR_PAYROLL_RUN_ALREADY_EXISTS, exception.getMessage());
        verify(payrollRunRepository, never()).save(any(PayrollRun.class));
        verify(payrollResultService, never()).generatePayrollResult(any(PayrollRun.class));
    }

    @Test
    void runPayroll_badRequestWhenCompanyMissing() {
        Account account = new Account();
        account.setCode("ACC-1");
        account.setCompanyCode(" ");
        when(securityContextService.getCurrentUser()).thenReturn(new CustomUserDetails(account, null));

        BadRequestException exception = assertThrows(BadRequestException.class, () -> payrollRunService.runPayroll());

        assertEquals(Messages.ERROR_CURRENT_USER_COMPANY_MISSING, exception.getMessage());
        verify(payrollRunRepository, never()).save(any(PayrollRun.class));
        verify(payrollResultService, never()).generatePayrollResult(any(PayrollRun.class));
    }
}
