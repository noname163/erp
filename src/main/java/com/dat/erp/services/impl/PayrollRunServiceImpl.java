package com.dat.erp.services.impl;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dat.erp.constants.CodePrefixes;
import com.dat.erp.constants.Messages;
import com.dat.erp.constants.PayrollRunStatus;
import com.dat.erp.dto.request.payroll.PayrollRunPreviewRequest;
import com.dat.erp.dto.response.payroll.EmployeePayslipResponse;
import com.dat.erp.dto.response.payroll.PayrollEmployeeSummaryResponse;
import com.dat.erp.dto.response.payroll.PayrollLineResponse;
import com.dat.erp.dto.response.payroll.PayrollRunDetailResponse;
import com.dat.erp.dto.response.payroll.PayrollRunResponse;
import com.dat.erp.entities.PayrollEmployeeSummary;
import com.dat.erp.entities.PayrollResult;
import com.dat.erp.entities.PayrollResultDetail;
import com.dat.erp.entities.PayrollRun;
import com.dat.erp.entities.UserProfile;
import com.dat.erp.exceptions.BadRequestException;
import com.dat.erp.exceptions.ForbiddenException;
import com.dat.erp.exceptions.ResourceNotFoundException;
import com.dat.erp.repositories.customrepositories.PayrollEmployeeSummaryRepository;
import com.dat.erp.repositories.customrepositories.PayrollResultDetailRepository;
import com.dat.erp.repositories.customrepositories.PayrollResultRepository;
import com.dat.erp.repositories.customrepositories.PayrollRunRepository;
import com.dat.erp.repositories.customrepositories.UserProfileRepository;
import com.dat.erp.services.PayrollRunService;
import com.dat.erp.services.SecurityContextService;
import com.dat.erp.services.base.AbstractAuditableService;

@Service
public class PayrollRunServiceImpl extends AbstractAuditableService implements PayrollRunService {

    private final PayrollRunRepository payrollRunRepository;
    private final PayrollEmployeeSummaryRepository payrollEmployeeSummaryRepository;
    private final PayrollResultRepository payrollResultRepository;
    private final PayrollResultDetailRepository payrollResultDetailRepository;
    private final UserProfileRepository userProfileRepository;
    private final PayrollRunAsyncProcessor payrollRunAsyncProcessor;

    public PayrollRunServiceImpl(
            PayrollRunRepository payrollRunRepository,
            PayrollEmployeeSummaryRepository payrollEmployeeSummaryRepository,
            PayrollResultRepository payrollResultRepository,
            PayrollResultDetailRepository payrollResultDetailRepository,
            UserProfileRepository userProfileRepository,
            SecurityContextService securityContextService,
            PayrollRunAsyncProcessor payrollRunAsyncProcessor) {
        this.payrollRunRepository = payrollRunRepository;
        this.payrollEmployeeSummaryRepository = payrollEmployeeSummaryRepository;
        this.payrollResultRepository = payrollResultRepository;
        this.payrollResultDetailRepository = payrollResultDetailRepository;
        this.userProfileRepository = userProfileRepository;
        this.securityContextService = securityContextService;
        this.payrollRunAsyncProcessor = payrollRunAsyncProcessor;
    }

    @Override
    @Transactional
    public PayrollRunResponse createPreview(PayrollRunPreviewRequest request) {
        if (request.periodStart() == null || request.periodEnd() == null || request.periodStart().isAfter(request.periodEnd())) {
            throw new BadRequestException(Messages.ERROR_PAYROLL_PERIOD_INVALID);
        }
        PayrollRun run = PayrollRun.builder()
                .period(YearMonth.from(request.periodStart()).toString())
                .periodStart(request.periodStart())
                .periodEnd(request.periodEnd())
                .status(PayrollRunStatus.QUEUED)
                .isPreview(true)
                .snapshotVersion(YearMonth.from(request.periodStart()) + "-snapshot")
                .warningCount(0)
                .errorCount(0)
                .runAt(LocalDateTime.now())
                .build();
        generateCodeIfMissing(run, CodePrefixes.PAYROLL_RUN);
        applyInsertAudit(run);
        PayrollRun saved = payrollRunRepository.save(run);
        payrollRunAsyncProcessor.processPreviewAsync(saved.getCode(), request.userProfileCodes());
        return toRunResponse(saved);
    }

    @Override
    @Transactional
    public PayrollRunResponse finalizeRun(String runCode) {
        PayrollRun run = findRun(runCode);
        if (run.getStatus() != PayrollRunStatus.PREVIEW_READY) {
            throw new BadRequestException(Messages.ERROR_PAYROLL_RUN_STATUS_INVALID);
        }
        if (payrollEmployeeSummaryRepository.existsByPayrollRun_CodeAndHasBlockingIssueTrueAndIsDeletedFalse(runCode)) {
            throw new BadRequestException(Messages.ERROR_PAYROLL_RUN_FINALIZE_BLOCKED);
        }
        run.setStatus(PayrollRunStatus.FINALIZED);
        run.setIsPreview(false);
        run.setClosedAt(LocalDateTime.now());
        payrollRunRepository.save(run);
        payrollEmployeeSummaryRepository.findByPayrollRun_CodeAndIsDeletedFalseOrderByUserProfile_CodeAsc(runCode).forEach(summary -> {
            summary.setIsFrozen(true);
            payrollEmployeeSummaryRepository.save(summary);
        });
        payrollResultRepository.findByPayrollRun_CodeAndIsDeletedFalseOrderByUserProfile_CodeAscSequenceOrderAscIdAsc(runCode)
                .forEach(result -> {
                    result.setIsFrozen(true);
                    payrollResultRepository.save(result);
                });
        return toRunResponse(run);
    }

    @Override
    @Transactional
    public PayrollRunResponse replaySnapshot(String runCode) {
        PayrollRun sourceRun = findRun(runCode);
        if (sourceRun.getStatus() != PayrollRunStatus.FINALIZED) {
            throw new BadRequestException(Messages.ERROR_PAYROLL_RUN_REPLAY_SOURCE_INVALID);
        }

        PayrollRun replayRun = PayrollRun.builder()
                .period(sourceRun.getPeriod())
                .periodStart(sourceRun.getPeriodStart())
                .periodEnd(sourceRun.getPeriodEnd())
                .status(PayrollRunStatus.PREVIEW_READY)
                .isPreview(true)
                .snapshotVersion(sourceRun.getSnapshotVersion())
                .replayedFromRunCode(sourceRun.getCode())
                .warningCount(sourceRun.getWarningCount())
                .errorCount(sourceRun.getErrorCount())
                .runAt(LocalDateTime.now())
                .build();
        generateCodeIfMissing(replayRun, CodePrefixes.PAYROLL_RUN);
        applyInsertAudit(replayRun);
        PayrollRun savedReplayRun = payrollRunRepository.save(replayRun);

        for (PayrollEmployeeSummary sourceSummary : payrollEmployeeSummaryRepository
                .findByPayrollRun_CodeAndIsDeletedFalseOrderByUserProfile_CodeAsc(runCode)) {
            cloneSummary(sourceSummary, savedReplayRun);
        }
        return toRunResponse(savedReplayRun);
    }

    @Override
    @Transactional(readOnly = true)
    public PayrollRunDetailResponse getRun(String runCode) {
        PayrollRun run = findRun(runCode);
        List<PayrollEmployeeSummaryResponse> employees = payrollEmployeeSummaryRepository
                .findByPayrollRun_CodeAndIsDeletedFalseOrderByUserProfile_CodeAsc(runCode).stream()
                .map(this::toSummaryResponse)
                .toList();
        return new PayrollRunDetailResponse(toRunResponse(run), employees);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeePayslipResponse getPayslip(String runCode, String userProfileCode) {
        PayrollRun run = findRun(runCode);
        PayrollEmployeeSummary summary = payrollEmployeeSummaryRepository
                .findByPayrollRun_CodeAndUserProfile_CodeAndIsDeletedFalse(runCode, userProfileCode)
                .orElseThrow(() -> new ResourceNotFoundException(Messages.ERROR_PAYROLL_RUN_NOT_FOUND));
        List<PayrollLineResponse> lines = payrollResultRepository
                .findByPayrollRun_CodeAndUserProfile_CodeAndIsDeletedFalseOrderBySequenceOrderAscIdAsc(runCode, userProfileCode)
                .stream()
                .map(this::toLineResponse)
                .toList();
        return new EmployeePayslipResponse(toRunResponse(run), toSummaryResponse(summary), lines);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeePayslipResponse getPayslipForCurrentUser(String runCode) {
        PayrollRun run = findRun(runCode);
        if (run.getStatus() != PayrollRunStatus.FINALIZED) {
            throw new BadRequestException(Messages.ERROR_PAYROLL_RUN_STATUS_INVALID);
        }
        UserProfile currentUserProfile = resolveCurrentUserProfile();
        return getPayslip(runCode, currentUserProfile.getCode());
    }

    private void cloneSummary(PayrollEmployeeSummary sourceSummary, PayrollRun replayRun) {
        PayrollEmployeeSummary clonedSummary = PayrollEmployeeSummary.builder()
                .payrollRun(replayRun)
                .userProfile(sourceSummary.getUserProfile())
                .grossAmount(sourceSummary.getGrossAmount())
                .deductionAmount(sourceSummary.getDeductionAmount())
                .netAmount(sourceSummary.getNetAmount())
                .currency(sourceSummary.getCurrency())
                .status(sourceSummary.getStatus())
                .hasBlockingIssue(sourceSummary.getHasBlockingIssue())
                .issueMessage(sourceSummary.getIssueMessage())
                .policySnapshotVersion(sourceSummary.getPolicySnapshotVersion())
                .isFrozen(false)
                .build();
        generateCodeIfMissing(clonedSummary, CodePrefixes.PAYROLL_EMPLOYEE_SUMMARY);
        applyInsertAudit(clonedSummary);
        PayrollEmployeeSummary savedSummary = payrollEmployeeSummaryRepository.save(clonedSummary);

        for (PayrollResult sourceResult : payrollResultRepository
                .findByEmployeeSummary_CodeAndIsDeletedFalseOrderBySequenceOrderAscIdAsc(sourceSummary.getCode())) {
            PayrollResult clonedResult = PayrollResult.builder()
                    .payrollRun(replayRun)
                    .employeeSummary(savedSummary)
                    .userProfile(sourceResult.getUserProfile())
                    .salary(sourceResult.getSalary())
                    .amount(sourceResult.getAmount())
                    .quantity(sourceResult.getQuantity())
                    .quantityValue(sourceResult.getQuantityValue())
                    .unit(sourceResult.getUnit())
                    .lineType(sourceResult.getLineType())
                    .sourceType(sourceResult.getSourceType())
                    .segmentFrom(sourceResult.getSegmentFrom())
                    .segmentTo(sourceResult.getSegmentTo())
                    .currency(sourceResult.getCurrency())
                    .rate(sourceResult.getRate())
                    .multiplier(sourceResult.getMultiplier())
                    .sequenceOrder(sourceResult.getSequenceOrder())
                    .sourceRefCode(sourceResult.getSourceRefCode())
                    .policySnapshotVersion(sourceResult.getPolicySnapshotVersion())
                    .isManual(sourceResult.getIsManual())
                    .isFrozen(false)
                    .isRetro(sourceResult.getIsRetro())
                    .retroReason(sourceResult.getRetroReason())
                    .build();
            generateCodeIfMissing(clonedResult, CodePrefixes.PAYROLL_RESULT);
            applyInsertAudit(clonedResult);
            PayrollResult savedResult = payrollResultRepository.save(clonedResult);
            cloneDetails(sourceResult.getCode(), savedResult);
        }
    }

    private void cloneDetails(String sourceResultCode, PayrollResult savedResult) {
        for (PayrollResultDetail sourceDetail : payrollResultDetailRepository.findByPayrollResult_CodeAndIsDeletedFalse(sourceResultCode)) {
            PayrollResultDetail clonedDetail = PayrollResultDetail.builder()
                    .payrollResult(savedResult)
                    .calcBasis(sourceDetail.getCalcBasis())
                    .basisDays(sourceDetail.getBasisDays())
                    .basisHours(sourceDetail.getBasisHours())
                    .basisMinutes(sourceDetail.getBasisMinutes())
                    .paidDays(sourceDetail.getPaidDays())
                    .unpaidDays(sourceDetail.getUnpaidDays())
                    .payableHours(sourceDetail.getPayableHours())
                    .payableMinutes(sourceDetail.getPayableMinutes())
                    .expectedHours(sourceDetail.getExpectedHours())
                    .expectedMinutes(sourceDetail.getExpectedMinutes())
                    .expectedQuantity(sourceDetail.getExpectedQuantity())
                    .payableQuantity(sourceDetail.getPayableQuantity())
                    .ratePerDay(sourceDetail.getRatePerDay())
                    .ratePerHour(sourceDetail.getRatePerHour())
                    .ratePerMinute(sourceDetail.getRatePerMinute())
                    .multiplierApplied(sourceDetail.getMultiplierApplied())
                    .formulaNote(sourceDetail.getFormulaNote())
                    .roundingNote(sourceDetail.getRoundingNote())
                    .policyRuleCode(sourceDetail.getPolicyRuleCode())
                    .sourceDate(sourceDetail.getSourceDate())
                    .build();
            generateCodeIfMissing(clonedDetail, CodePrefixes.PAYROLL_RESULT_DETAIL);
            applyInsertAudit(clonedDetail);
            payrollResultDetailRepository.save(clonedDetail);
        }
    }

    private UserProfile resolveCurrentUserProfile() {
        var currentUser = securityContextService.getCurrentUser();
        if (currentUser.getUserProfile() != null) {
            return currentUser.getUserProfile();
        }
        return userProfileRepository.findByAccount_Code(currentUser.getCode())
                .orElseThrow(() -> new ForbiddenException(Messages.ERROR_PAYROLL_SELF_VIEW_FORBIDDEN));
    }

    private PayrollRun findRun(String runCode) {
        return payrollRunRepository.findByCodeAndIsDeletedFalse(runCode)
                .orElseThrow(() -> new ResourceNotFoundException(Messages.ERROR_PAYROLL_RUN_NOT_FOUND));
    }

    private PayrollRunResponse toRunResponse(PayrollRun run) {
        return new PayrollRunResponse(run.getCode(), run.getPeriod(), run.getPeriodStart(), run.getPeriodEnd(),
                run.getIsPreview(), run.getStatus(), run.getSnapshotVersion(), run.getReplayedFromRunCode(),
                run.getWarningCount(), run.getErrorCount());
    }

    private PayrollEmployeeSummaryResponse toSummaryResponse(PayrollEmployeeSummary summary) {
        return new PayrollEmployeeSummaryResponse(summary.getUserProfile().getCode(), buildFullName(summary.getUserProfile()),
                summary.getGrossAmount(), summary.getDeductionAmount(), summary.getNetAmount(), summary.getCurrency(),
                summary.getStatus(), summary.getHasBlockingIssue(), summary.getIssueMessage());
    }

    private PayrollLineResponse toLineResponse(PayrollResult result) {
        String formulaNote = payrollResultDetailRepository.findByPayrollResult_CodeAndIsDeletedFalse(result.getCode()).stream()
                .map(PayrollResultDetail::getFormulaNote)
                .filter(note -> note != null && !note.isBlank())
                .findFirst()
                .orElse(null);
        return new PayrollLineResponse(result.getSalary() == null ? null : result.getSalary().getCode(),
                result.getSalary() == null ? null : result.getSalary().getName(), result.getLineType(), result.getSourceType(),
                result.getAmount(), result.getQuantityValue(), result.getCurrency(),
                result.getRate() == null ? null : new java.math.BigDecimal(result.getRate()), result.getMultiplier(),
                result.getSegmentFrom(), result.getSegmentTo(), result.getIsRetro(), result.getRetroReason(), formulaNote);
    }

    private String buildFullName(UserProfile userProfile) {
        String firstName = userProfile.getFirstName() == null ? "" : userProfile.getFirstName().trim();
        String lastName = userProfile.getLastName() == null ? "" : userProfile.getLastName().trim();
        return (firstName + " " + lastName).trim();
    }
}
