package com.dat.erp.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.dat.erp.constants.Messages;
import com.dat.erp.constants.PayrollLineType;
import com.dat.erp.constants.PayrollResultCalcBasis;
import com.dat.erp.constants.PayrollRunStatus;
import com.dat.erp.constants.PayrollSourceType;
import com.dat.erp.constants.PayrollSummaryStatus;
import com.dat.erp.dto.request.payroll.PayrollRunPreviewRequest;
import com.dat.erp.dto.response.payroll.PayrollRunResponse;
import com.dat.erp.entities.Account;
import com.dat.erp.entities.PayrollEmployeeSummary;
import com.dat.erp.entities.PayrollResult;
import com.dat.erp.entities.PayrollResultDetail;
import com.dat.erp.entities.PayrollRun;
import com.dat.erp.entities.UserProfile;
import com.dat.erp.exceptions.BadRequestException;
import com.dat.erp.repositories.customrepositories.PayrollEmployeeSummaryRepository;
import com.dat.erp.repositories.customrepositories.PayrollResultDetailRepository;
import com.dat.erp.repositories.customrepositories.PayrollResultRepository;
import com.dat.erp.repositories.customrepositories.PayrollRunRepository;
import com.dat.erp.repositories.customrepositories.UserProfileRepository;
import com.dat.erp.services.CodeGenerator;
import com.dat.erp.services.SecurityContextService;
import com.dat.erp.systemconfigs.CustomUserDetails;

class PayrollRunServiceImplTest {

    @Mock
    private PayrollRunRepository payrollRunRepository;

    @Mock
    private PayrollEmployeeSummaryRepository payrollEmployeeSummaryRepository;

    @Mock
    private PayrollResultRepository payrollResultRepository;

    @Mock
    private PayrollResultDetailRepository payrollResultDetailRepository;

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private SecurityContextService securityContextService;

    @Mock
    private PayrollRunAsyncProcessor payrollRunAsyncProcessor;

    @Mock
    private CodeGenerator codeGenerator;

    @InjectMocks
    private PayrollRunServiceImpl payrollRunService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(payrollRunService, "codeGenerator", codeGenerator);
    }

    @Test
    void createPreview_queuesAsyncPayrollRun() {
        Account account = new Account();
        account.setCode("ACC-001");
        account.setCompanyCode("CMP-001");
        when(securityContextService.getCurrentUser()).thenReturn(new CustomUserDetails(account, null));
        when(codeGenerator.nextCode("PRN-")).thenReturn("PRN-000001");
        when(payrollRunRepository.save(any(PayrollRun.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(payrollRunAsyncProcessor).processPreviewAsync(eq("PRN-000001"), any());

        PayrollRunPreviewRequest request = new PayrollRunPreviewRequest(
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 31),
                List.of("USR-001", "USR-002"));

        PayrollRunResponse response = payrollRunService.createPreview(request);

        assertNotNull(response);
        assertEquals("PRN-000001", response.code());
        assertEquals(PayrollRunStatus.QUEUED, response.status());
        ArgumentCaptor<PayrollRun> captor = ArgumentCaptor.forClass(PayrollRun.class);
        verify(payrollRunRepository).save(captor.capture());
        assertEquals("CMP-001", captor.getValue().getCompanyCode());
        verify(payrollRunAsyncProcessor).processPreviewAsync("PRN-000001", request.userProfileCodes());
    }

    @Test
    void finalizeRun_blocksWhenEmployeeSummaryHasBlockingIssue() {
        PayrollRun run = new PayrollRun();
        run.setCode("PRN-000001");
        run.setStatus(PayrollRunStatus.PREVIEW_READY);
        run.setIsPreview(true);
        when(payrollRunRepository.findByCodeAndIsDeletedFalse("PRN-000001")).thenReturn(Optional.of(run));
        when(payrollEmployeeSummaryRepository.existsByPayrollRun_CodeAndHasBlockingIssueTrueAndIsDeletedFalse("PRN-000001"))
                .thenReturn(true);

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> payrollRunService.finalizeRun("PRN-000001"));

        assertEquals(Messages.ERROR_PAYROLL_RUN_FINALIZE_BLOCKED, exception.getMessage());
        verify(payrollRunRepository, never()).save(any(PayrollRun.class));
    }

    @Test
    void finalizeRun_marksSummariesAndResultsFrozen() {
        PayrollRun run = new PayrollRun();
        run.setCode("PRN-000001");
        run.setStatus(PayrollRunStatus.PREVIEW_READY);
        run.setIsPreview(true);
        PayrollEmployeeSummary summary = new PayrollEmployeeSummary();
        summary.setCode("PES-001");
        summary.setIsFrozen(false);
        PayrollResult result = new PayrollResult();
        result.setCode("PRS-001");
        result.setIsFrozen(false);
        when(payrollRunRepository.findByCodeAndIsDeletedFalse("PRN-000001")).thenReturn(Optional.of(run));
        when(payrollEmployeeSummaryRepository.existsByPayrollRun_CodeAndHasBlockingIssueTrueAndIsDeletedFalse("PRN-000001"))
                .thenReturn(false);
        when(payrollRunRepository.save(any(PayrollRun.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(payrollEmployeeSummaryRepository.findByPayrollRun_CodeAndIsDeletedFalseOrderByUserProfile_CodeAsc("PRN-000001"))
                .thenReturn(List.of(summary));
        when(payrollResultRepository.findByPayrollRun_CodeAndIsDeletedFalseOrderByUserProfile_CodeAscSequenceOrderAscIdAsc(
                "PRN-000001")).thenReturn(List.of(result));
        when(payrollEmployeeSummaryRepository.save(any(PayrollEmployeeSummary.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(payrollResultRepository.save(any(PayrollResult.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PayrollRunResponse response = payrollRunService.finalizeRun("PRN-000001");

        assertEquals(PayrollRunStatus.FINALIZED, response.status());
        assertFalse(response.isPreview());
        verify(payrollEmployeeSummaryRepository).save(summary);
        verify(payrollResultRepository).save(result);
    }

    @Test
    void replaySnapshot_clonesFrozenRunDataIntoPreviewRun() {
        PayrollRun sourceRun = new PayrollRun();
        sourceRun.setCode("PRN-SOURCE");
        sourceRun.setPeriod("2026-01");
        sourceRun.setPeriodStart(LocalDate.of(2026, 1, 1));
        sourceRun.setPeriodEnd(LocalDate.of(2026, 1, 31));
        sourceRun.setStatus(PayrollRunStatus.FINALIZED);
        sourceRun.setSnapshotVersion("2026-01-snapshot");
        sourceRun.setWarningCount(0);
        sourceRun.setErrorCount(0);
        UserProfile userProfile = new UserProfile();
        userProfile.setCode("USR-001");
        userProfile.setFirstName("Jane");
        userProfile.setLastName("Doe");

        PayrollEmployeeSummary sourceSummary = new PayrollEmployeeSummary();
        sourceSummary.setCode("PES-SOURCE");
        sourceSummary.setUserProfile(userProfile);
        sourceSummary.setGrossAmount("3100.00");
        sourceSummary.setDeductionAmount("0.00");
        sourceSummary.setNetAmount("3100.00");
        sourceSummary.setCurrency("VND");
        sourceSummary.setStatus(PayrollSummaryStatus.SUCCESS);
        sourceSummary.setHasBlockingIssue(false);
        sourceSummary.setIssueMessage(null);
        sourceSummary.setPolicySnapshotVersion("2026-01-snapshot");
        sourceSummary.setIsFrozen(true);
        PayrollResult sourceResult = new PayrollResult();
        sourceResult.setCode("PRS-SOURCE");
        sourceResult.setUserProfile(userProfile);
        sourceResult.setEmployeeSummary(sourceSummary);
        sourceResult.setAmount("3100.00");
        sourceResult.setQuantityValue(new BigDecimal("31.00"));
        sourceResult.setLineType(PayrollLineType.EARNING);
        sourceResult.setSourceType(PayrollSourceType.SALARY);
        sourceResult.setCurrency("VND");
        sourceResult.setRate("100.00");
        sourceResult.setMultiplier(BigDecimal.ONE);
        sourceResult.setSequenceOrder(1);
        sourceResult.setPolicySnapshotVersion("2026-01-snapshot");
        sourceResult.setIsManual(false);
        sourceResult.setIsFrozen(true);
        sourceResult.setIsRetro(false);
        PayrollResultDetail sourceDetail = new PayrollResultDetail();
        sourceDetail.setCode("PRD-SOURCE");
        sourceDetail.setCalcBasis(PayrollResultCalcBasis.CALENDAR_DAYS);
        sourceDetail.setPayableQuantity(new BigDecimal("31.00"));
        sourceDetail.setRatePerDay(new BigDecimal("100.00"));
        sourceDetail.setMultiplierApplied(BigDecimal.ONE);
        sourceDetail.setFormulaNote("Frozen snapshot");

        when(payrollRunRepository.findByCodeAndIsDeletedFalse("PRN-SOURCE")).thenReturn(Optional.of(sourceRun));
        when(codeGenerator.nextCode("PRN-")).thenReturn("PRN-REPLAY");
        when(codeGenerator.nextCode("PES-")).thenReturn("PES-REPLAY");
        when(codeGenerator.nextCode("PRS-")).thenReturn("PRS-REPLAY");
        when(codeGenerator.nextCode("PRD-")).thenReturn("PRD-REPLAY");
        when(payrollRunRepository.save(any(PayrollRun.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(payrollEmployeeSummaryRepository.findByPayrollRun_CodeAndIsDeletedFalseOrderByUserProfile_CodeAsc("PRN-SOURCE"))
                .thenReturn(List.of(sourceSummary));
        when(payrollEmployeeSummaryRepository.save(any(PayrollEmployeeSummary.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(payrollResultRepository.findByEmployeeSummary_CodeAndIsDeletedFalseOrderBySequenceOrderAscIdAsc("PES-SOURCE"))
                .thenReturn(List.of(sourceResult));
        when(payrollResultRepository.save(any(PayrollResult.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(payrollResultDetailRepository.findByPayrollResult_CodeAndIsDeletedFalse("PRS-SOURCE")).thenReturn(List.of(sourceDetail));
        when(payrollResultDetailRepository.save(any(PayrollResultDetail.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PayrollRunResponse response = payrollRunService.replaySnapshot("PRN-SOURCE");

        assertEquals("PRN-REPLAY", response.code());
        assertEquals(PayrollRunStatus.PREVIEW_READY, response.status());
        assertEquals("PRN-SOURCE", response.replayedFromRunCode());

        ArgumentCaptor<PayrollEmployeeSummary> summaryCaptor = ArgumentCaptor.forClass(PayrollEmployeeSummary.class);
        verify(payrollEmployeeSummaryRepository).save(summaryCaptor.capture());
        assertEquals("PES-REPLAY", summaryCaptor.getValue().getCode());
        assertFalse(summaryCaptor.getValue().getIsFrozen());

        ArgumentCaptor<PayrollResult> resultCaptor = ArgumentCaptor.forClass(PayrollResult.class);
        verify(payrollResultRepository).save(resultCaptor.capture());
        assertEquals("PRS-REPLAY", resultCaptor.getValue().getCode());
        assertFalse(resultCaptor.getValue().getIsFrozen());

        ArgumentCaptor<PayrollResultDetail> detailCaptor = ArgumentCaptor.forClass(PayrollResultDetail.class);
        verify(payrollResultDetailRepository).save(detailCaptor.capture());
        assertEquals("PRD-REPLAY", detailCaptor.getValue().getCode());
        assertEquals("Frozen snapshot", detailCaptor.getValue().getFormulaNote());
    }
}
