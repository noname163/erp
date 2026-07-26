package com.dat.erp.services.payroll.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dat.erp.constants.Messages;
import com.dat.erp.constants.PayrollRerunMode;
import com.dat.erp.constants.PayrollResultCalcBasis;
import com.dat.erp.constants.PayrollRunAuditActionType;
import com.dat.erp.constants.PayrollRunAuditStatus;
import com.dat.erp.constants.PayrollRunStatus;
import com.dat.erp.dto.request.PayrollRerunRequest;
import com.dat.erp.dto.request.PayrollRunAuditLogRequest;
import com.dat.erp.dto.response.PagedResponse;
import com.dat.erp.dto.response.PayrollRerunEmployeeResultResponse;
import com.dat.erp.dto.response.PayrollRerunErrorResponse;
import com.dat.erp.dto.response.PayrollRerunResponse;
import com.dat.erp.dto.response.PayrollRunResponse;
import com.dat.erp.dto.response.salary.MonthlySalaryCalculationResponse;
import com.dat.erp.dto.response.salary.MonthlySalaryDetailAuditResponse;
import com.dat.erp.entities.EmployeeSalary;
import com.dat.erp.entities.PayrollResult;
import com.dat.erp.entities.PayrollResultDetail;
import com.dat.erp.entities.PayrollRun;
import com.dat.erp.entities.UserProfile;
import com.dat.erp.exceptions.BadRequestException;
import com.dat.erp.exceptions.ConflictException;
import com.dat.erp.exceptions.ResourceNotFoundException;
import com.dat.erp.mapper.interfaces.PayrollRunMapper;
import com.dat.erp.repositories.customrepositories.EmployeeSalaryRepository;
import com.dat.erp.repositories.customrepositories.PayrollRunRepository;
import com.dat.erp.services.MonthlySalaryCalculationService;
import com.dat.erp.services.SecurityContextService;
import com.dat.erp.services.payroll.PayrollResultDetailService;
import com.dat.erp.services.payroll.PayrollResultService;
import com.dat.erp.services.payroll.PayrollResultSnapshotService;
import com.dat.erp.services.payroll.PayrollRunAuditLogService;
import com.dat.erp.services.payroll.PayrollRunService;
import com.dat.erp.utils.CompanySecretKeyCryptoUtils;
import com.dat.erp.utils.CustomStringUtils;
import com.dat.erp.utils.PageableUtils;
import com.dat.erp.utils.UuidV7;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class PayrollRunServiceImpl implements PayrollRunService {

    private final SecurityContextService securityContextService;

    private static final Logger log = LoggerFactory.getLogger(PayrollRunServiceImpl.class);

    private static final int MAX_PAST_RUN_MONTHS = 3;
    private static final String DEFAULT_SORT_BY = "runAt";
    private static final String DEFAULT_SORT_DIR = "DESC";
    private static final LocalDateTime MIN_FILTER_DATE = LocalDateTime.of(1900, 1, 1, 0, 0);
    private static final LocalDateTime MAX_FILTER_DATE = LocalDateTime.of(2999, 12, 31, 23, 59, 59);

    private final PayrollRunRepository payrollRunRepository;
    private final PayrollRunMapper payrollRunMapper;
    private final PayrollResultService payrollResultService;
    private final PayrollResultSnapshotService payrollResultSnapshotService;
    private final EmployeeSalaryRepository employeeSalaryRepository;
    private final MonthlySalaryCalculationService monthlySalaryCalculationService;
    private final PayrollResultDetailService payrollResultDetailService;
    private final PayrollRunAuditLogService payrollRunAuditLogService;


    @Override
    @Transactional(readOnly = true)
    public PagedResponse<PayrollRunResponse> getPayrollRuns(
            PayrollRunStatus status,
            LocalDateTime runAtFrom,
            LocalDateTime runAtTo,
            LocalDateTime closeAtFrom,
            LocalDateTime closeAtTo,
            Integer page,
            Integer size,
            String sortBy,
            String sortDir) {
        validateDateRange(runAtFrom, runAtTo, Messages.ERROR_PAYROLL_RUN_RUN_AT_RANGE_INVALID);
        validateDateRange(closeAtFrom, closeAtTo, Messages.ERROR_PAYROLL_RUN_CLOSE_AT_RANGE_INVALID);

        String companyCode = securityContextService.getCurrentCompanyCode();
        LocalDateTime effectiveRunAtFrom = runAtFrom == null ? MIN_FILTER_DATE : runAtFrom;
        LocalDateTime effectiveRunAtTo = runAtTo == null ? MAX_FILTER_DATE : runAtTo;
        LocalDateTime effectiveCloseAtFrom = closeAtFrom == null ? MIN_FILTER_DATE : closeAtFrom;
        LocalDateTime effectiveCloseAtTo = closeAtTo == null ? MAX_FILTER_DATE : closeAtTo;
        Pageable pageable = PageableUtils.create(
                page,
                size,
                resolveSortBy(sortBy),
                sortDir == null || sortDir.isBlank() ? DEFAULT_SORT_DIR : sortDir);

        Page<PayrollRun> payrollRuns = payrollRunRepository.searchByConditions(
                companyCode,
                status,
                effectiveRunAtFrom,
                effectiveRunAtTo,
                resolveNullValueForRange(runAtFrom, runAtTo),
                effectiveCloseAtFrom,
                effectiveCloseAtTo,
                resolveNullValueForRange(closeAtFrom, closeAtTo),
                pageable);
        return PageableUtils.mapPage(payrollRuns, payrollRunMapper::toResponse, Messages.SUCCESS);
    }

    @Override
    @Transactional
    public PayrollRunResponse runPayroll(YearMonth runDate) {
        YearMonth requestedRunMonth = validateAndResolveRunMonth(runDate);
        String companyCode = securityContextService.getCurrentCompanyCode();
        Month period = requestedRunMonth.getMonth();

        log.info("PAYROLL_RUN action=RUN_REQUESTED companyCode={} period={}", companyCode, period);

        if (payrollRunRepository.findByCompanyCodeAndPeriodAndIsDeletedFalse(companyCode, period).isPresent()) {
            log.warn("PAYROLL_RUN action=RUN_REJECTED result=DUPLICATE companyCode={} period={}", companyCode, period);
            throw new ConflictException(Messages.ERROR_PAYROLL_RUN_ALREADY_EXISTS);
        }

        PayrollRun payrollRun = PayrollRun.create(requestedRunMonth);
        payrollRun.start(LocalDateTime.now(ZoneOffset.UTC));
        PayrollRun savedPayrollRun = payrollRunRepository.save(payrollRun);
        log.info("PAYROLL_RUN action=RUN_CREATED result=SUCCESS companyCode={} payrollRunCode={} period={} status={}",
                companyCode, savedPayrollRun.getCode(), period, savedPayrollRun.getStatus());
        payrollResultService.generatePayrollResult(savedPayrollRun);
        log.info("PAYROLL_RUN action=RESULT_GENERATION_REQUESTED companyCode={} payrollRunCode={} period={}",
                companyCode, savedPayrollRun.getCode(), period);
        return payrollRunMapper.toResponse(savedPayrollRun);
    }

    @Override
    @Transactional
    public PayrollRerunResponse rerunPayroll(String payrollRunCode, PayrollRerunRequest request) {
        String companyCode = securityContextService.getCurrentCompanyCode();
        String normalizedPayrollRunCode = CustomStringUtils.normalizeCode(payrollRunCode);
        if (normalizedPayrollRunCode == null) {
            throw new BadRequestException(Messages.ERROR_PAYROLL_RUN_CODE_INVALID);
        }
        validateRerunRequest(request);

        String rerunBatchCode = "PRR" + UuidV7.generate();
        PayrollRun payrollRun = payrollRunRepository
                .findLockedByCodeAndCompanyCode(normalizedPayrollRunCode, companyCode)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(Messages.ERROR_PAYROLL_RUN_NOT_FOUND, normalizedPayrollRunCode)));
        validateRerunnableStatus(payrollRun);

        String reason = request.getReason().trim();
        payrollRunAuditLogService.writeAudit(
                payrollRun,
                new PayrollRunAuditLogRequest(rerunBatchCode, PayrollRunAuditActionType.RERUN_REQUESTED, reason, null,
                        null, null, PayrollRunAuditStatus.REQUESTED, null));
        logRerun("RERUN_REQUESTED", "REQUESTED", normalizedPayrollRunCode, rerunBatchCode, null, reason, null, null,
                null, 0L);

        payrollRunAuditLogService.writeAudit(
                payrollRun,
                new PayrollRunAuditLogRequest(rerunBatchCode, PayrollRunAuditActionType.RERUN_STARTED, reason, null,
                        null, null, request.isDryRun() ? PayrollRunAuditStatus.DRY_RUN : PayrollRunAuditStatus.STARTED,
                        null));

        List<PayrollResult> targetOldResults = payrollResultService.resolveTargetResults(payrollRun.getCode(), companyCode, request,
                request.getMode());
        Map<String, PayrollResult> oldResultsByEmployee = mapResultsByEmployeeCode(targetOldResults);
        List<String> targetEmployeeCodes = new ArrayList<>(oldResultsByEmployee.keySet());
        PayrollRerunResponse response = new PayrollRerunResponse();
        response.setPayrollRunCode(normalizedPayrollRunCode);
        response.setRerunBatchCode(rerunBatchCode);
        response.setDryRun(request.isDryRun());
        response.setTotalEmployees(targetEmployeeCodes.size());

        String companySecretKey = securityContextService.getCurrentCompanySecretKey();
        int successCount = 0;
        int failureCount = 0;
        for (String employeeCode : targetEmployeeCodes) {
            long startedAt = System.currentTimeMillis();
            PayrollResult oldResult = oldResultsByEmployee.get(employeeCode);
            try {
                payrollRunAuditLogService.writeAudit(
                        payrollRun,
                        new PayrollRunAuditLogRequest(rerunBatchCode,
                                PayrollRunAuditActionType.EMPLOYEE_RERUN_STARTED, reason, employeeCode, oldResult,
                                null, PayrollRunAuditStatus.STARTED, null));
                if (request.isDryRun()) {
                    payrollResultSnapshotService.createSnapshot(payrollRun, oldResult, rerunBatchCode, employeeCode);
                }

                MonthlySalaryCalculationResponse calculation = monthlySalaryCalculationService
                        .calculateEmployeeMonthlySalary(employeeCode, payrollRun.getPeriod());
                EmployeeSalary activeSalary = employeeSalaryRepository
                        .findFirstActiveByEmployeeCodeAndCompanyCodeAndDate(
                                employeeCode, companyCode, payrollRun.getPeriod().atEndOfMonth())
                        .orElse(oldResult.getEmployeeSalary());
                PayrollResult newResult = payrollResultService.buildRerunPayrollResult(payrollRun, oldResult, activeSalary, calculation,
                        companySecretKey);

                if (!request.isDryRun()) {
                    PayrollResult savedNewResult = payrollResultService.saveRerunPayRollResult(newResult);
                    payrollResultDetailService.replacePayrollResultDetailsBestEffort(
                            "PAYROLL_RERUN", savedNewResult, buildPayrollResultDetails(savedNewResult, calculation));
                    payrollResultService.softDeleteOldResult(oldResult.getCode());
                    newResult = savedNewResult;
                }

                PayrollRerunEmployeeResultResponse employeeResponse = buildEmployeeResponse(
                        employeeCode, oldResult, newResult, companySecretKey);
                response.getResults().add(employeeResponse);
                successCount++;
                payrollRunAuditLogService.writeAudit(
                        payrollRun,
                        new PayrollRunAuditLogRequest(rerunBatchCode,
                                PayrollRunAuditActionType.EMPLOYEE_RERUN_SUCCESS, reason, employeeCode, oldResult,
                                request.isDryRun() ? null : newResult, PayrollRunAuditStatus.SUCCESS, null));
                logRerun("EMPLOYEE_RERUN_SUCCESS", "SUCCESS", normalizedPayrollRunCode, rerunBatchCode, employeeCode,
                        reason, employeeResponse.getOldActualAmount(), employeeResponse.getNewActualAmount(), null,
                        System.currentTimeMillis() - startedAt);
            } catch (Exception ex) {
                failureCount++;
                response.getErrors().add(new PayrollRerunErrorResponse(employeeCode, ex.getMessage()));
                payrollRunAuditLogService.writeAudit(
                        payrollRun,
                        new PayrollRunAuditLogRequest(rerunBatchCode,
                                PayrollRunAuditActionType.EMPLOYEE_RERUN_FAILED, reason, employeeCode, oldResult, null,
                                PayrollRunAuditStatus.FAILED, ex.getMessage()));
                logRerun("EMPLOYEE_RERUN_FAILED", "FAILED", normalizedPayrollRunCode, rerunBatchCode, employeeCode,
                        reason, null, null, ex.getMessage(), System.currentTimeMillis() - startedAt);
            }
        }

        response.setSuccessCount(successCount);
        response.setFailedCount(failureCount);
        PayrollRunStatus finalStatus = failureCount > 0
                ? PayrollRunStatus.FAILED
                : PayrollRunStatus.CALCULATED;
        response.setStatus(finalStatus);
        if (!request.isDryRun()) {
            payrollRun.completeRerun(failureCount, request.isDryRun());
            payrollRunRepository.save(payrollRun);
        }
        payrollRunAuditLogService.writeAudit(
                payrollRun,
                new PayrollRunAuditLogRequest(
                        rerunBatchCode,
                        failureCount == 0 ? PayrollRunAuditActionType.RERUN_COMPLETED
                                : PayrollRunAuditActionType.RERUN_FAILED,
                        reason,
                        null,
                        null,
                        null,
                        toAuditStatus(finalStatus),
                        null));
        logRerun(failureCount == 0 ? "RERUN_COMPLETED" : "RERUN_FAILED", finalStatus.name(), normalizedPayrollRunCode,
                rerunBatchCode, null, reason, null, null, null, 0L);
        return response;
    }

    private void validateRerunRequest(PayrollRerunRequest request) {

        PayrollRerunMode mode = request.getMode() == null ? PayrollRerunMode.FULL_RUN : request.getMode();
        if (mode == PayrollRerunMode.SELECTED_EMPLOYEES
                && (request.getEmployeeCodes() == null || request.getEmployeeCodes().isEmpty())) {
            throw new BadRequestException(Messages.ERROR_PAYROLL_RERUN_EMPLOYEES_REQUIRED);
        }
        if (mode == PayrollRerunMode.FAILED_ONLY) {
            throw new BadRequestException(Messages.ERROR_PAYROLL_RERUN_FAILED_ONLY_UNSUPPORTED);
        }
    }

    private void validateRerunnableStatus(PayrollRun payrollRun) {
        if (payrollRun.getStatus() == PayrollRunStatus.CLOSED) {
            throw new ConflictException(Messages.ERROR_PAYROLL_RERUN_CLOSED_NOT_ALLOWED);
        }
        if (payrollRun.getStatus() == PayrollRunStatus.PROCESSING
                || payrollRun.getStatus() == PayrollRunStatus.RERUNNING) {
            throw new ConflictException(Messages.ERROR_PAYROLL_RERUN_CONCURRENT);
        }
    }

    private Map<String, PayrollResult> mapResultsByEmployeeCode(List<PayrollResult> payrollResults) {
        Map<String, PayrollResult> resultsByEmployee = new LinkedHashMap<>();
        for (PayrollResult payrollResult : payrollResults) {
            String employeeCode = resolveEmployeeCode(payrollResult);
            if (employeeCode != null) {
                resultsByEmployee.putIfAbsent(employeeCode, payrollResult);
            }
        }
        return resultsByEmployee;
    }

    private List<PayrollResultDetail> buildPayrollResultDetails(
            PayrollResult payrollResult,
            MonthlySalaryCalculationResponse calculation) {
        List<PayrollResultDetail> details = new ArrayList<>();
        details.add(buildSummaryDetail(payrollResult, calculation));
        if (calculation.getPaidLeaveHours() != null && calculation.getPaidLeaveHours().signum() > 0) {
            details.add(buildAmountDetail(payrollResult, PayrollResultCalcBasis.PAID_LEAVE,
                    calculation.getPaidLeaveHours(), calculation.getStandardMoneyPerHour(), java.math.BigDecimal.ZERO,
                    "Paid leave counted as paid working time"));
        }
        if (calculation.getUnpaidLeaveHours() != null && calculation.getUnpaidLeaveHours().signum() > 0) {
            details.add(buildAmountDetail(payrollResult, PayrollResultCalcBasis.UNPAID_LEAVE,
                    calculation.getUnpaidLeaveHours(), calculation.getStandardMoneyPerHour(),
                    calculation.getUnpaidLeaveHours().multiply(calculation.getStandardMoneyPerHour()).negate(),
                    "Unpaid leave excluded from paid working time"));
        }
        if (calculation.getLateEarlyDeductionHours() != null && calculation.getLateEarlyDeductionHours().signum() > 0) {
            details.add(buildAmountDetail(payrollResult, PayrollResultCalcBasis.LATE_EARLY_DEDUCTION,
                    calculation.getLateEarlyDeductionHours(), calculation.getStandardMoneyPerHour(),
                    calculation.getLateEarlyDeductionHours().multiply(calculation.getStandardMoneyPerHour()).negate(),
                    "Late arrival and early leave deducted from paid working time"));
        }
        if (calculation.getAuditTrail() != null) {
            for (MonthlySalaryDetailAuditResponse audit : calculation.getAuditTrail()) {
                details.add(buildAuditDetail(payrollResult, audit));
            }
        }
        return details;
    }

    private PayrollResultDetail buildSummaryDetail(PayrollResult payrollResult,
            MonthlySalaryCalculationResponse calculation) {
        PayrollResultDetail detail = new PayrollResultDetail();
        detail.setPayrollResult(payrollResult);
        detail.setCalcBasis(PayrollResultCalcBasis.HOURS);
        detail.setBasisHours(calculation.getExpectedWorkingHourPerMonth());
        detail.setPaidDays(calculation.getActualWorkingHourPerMonth());
        detail.setRatePerDay(calculation.getStandardMoneyPerHour());
        detail.setAmount(calculation.getFinalSalary());
        detail.setFormulaNote("basis=" + calculation.getSalaryBasisType() + ", expected="
                + calculation.getExpectedBasisValue() + ", actual=" + calculation.getActualBasisValue()
                + ", unit=" + calculation.getBasisUnit());
        return detail;
    }

    private PayrollResultDetail buildAmountDetail(PayrollResult payrollResult, PayrollResultCalcBasis calcBasis,
            java.math.BigDecimal hours, java.math.BigDecimal rate, java.math.BigDecimal amount, String formulaNote) {
        PayrollResultDetail detail = new PayrollResultDetail();
        detail.setPayrollResult(payrollResult);
        detail.setCalcBasis(PayrollResultCalcBasis.HOURS);
        detail.setBasisHours(hours);
        detail.setRatePerDay(rate);
        detail.setAmount(amount);
        detail.setFormulaNote("type=" + calcBasis + ", " + formulaNote);
        return detail;
    }

    private PayrollResultDetail buildAuditDetail(PayrollResult payrollResult, MonthlySalaryDetailAuditResponse audit) {
        PayrollResultDetail detail = new PayrollResultDetail();
        detail.setPayrollResult(payrollResult);
        detail.setCalcBasis(PayrollResultCalcBasis.HOURS);
        detail.setBasisHours(audit.getBaseAmount());
        detail.setRatePerDay(audit.getConfiguredAmount());
        detail.setMultiplierApplied(audit.getDependencyAmount());
        detail.setAmount(audit.getResult());
        detail.setFormulaNote("salaryCode=" + audit.getSalaryCode() + ", method=" + audit.getCalculateMethod()
                + ", dependency=" + audit.getDependenceCode());
        return detail;
    }




    private PayrollRerunEmployeeResultResponse buildEmployeeResponse(
            String employeeCode,
            PayrollResult oldResult,
            PayrollResult newResult,
            String companySecretKey) {
        BigDecimal oldActualAmount = CompanySecretKeyCryptoUtils.decryptAmount(oldResult.getActualAmount(),
                companySecretKey);
        BigDecimal newActualAmount = CompanySecretKeyCryptoUtils.decryptAmount(newResult.getActualAmount(),
                companySecretKey);
        BigDecimal oldExpectedAmount = CompanySecretKeyCryptoUtils.decryptAmount(oldResult.getExpectedAmount(),
                companySecretKey);
        BigDecimal newExpectedAmount = CompanySecretKeyCryptoUtils.decryptAmount(newResult.getExpectedAmount(),
                companySecretKey);
        return new PayrollRerunEmployeeResultResponse(
                employeeCode,
                "SUCCESS",
                oldActualAmount,
                newActualAmount,
                newActualAmount.subtract(oldActualAmount),
                oldExpectedAmount,
                newExpectedAmount,
                oldResult.getCode(),
                newResult.getCode());
    }

    private PayrollRunAuditStatus toAuditStatus(PayrollRunStatus status) {
        return switch (status) {
            case CALCULATED -> PayrollRunAuditStatus.CALCULATED;
            case FAILED -> PayrollRunAuditStatus.FAILED;
            default -> PayrollRunAuditStatus.SUCCESS;
        };
    }

    private void logRerun(
            String action,
            String status,
            String payrollRunCode,
            String rerunBatchCode,
            String employeeCode,
            String reason,
            java.math.BigDecimal oldActualAmount,
            java.math.BigDecimal newActualAmount,
            String errorMessage,
            long executionTimeMs) {
        log.info(
                "payroll_rerun action={} requestId={} traceId={} payrollRunCode={} rerunBatchCode={} employeeCode={} oldActualAmount={} newActualAmount={} executionTimeMs={} status={} reason={} errorMessage={}",
                action,
                MDC.get("requestId"),
                MDC.get("traceId"),
                payrollRunCode,
                rerunBatchCode,
                employeeCode,
                oldActualAmount,
                newActualAmount,
                executionTimeMs,
                status,
                reason,
                errorMessage);
    }

    private String resolveEmployeeCode(PayrollResult payrollResult) {
        if (payrollResult == null || payrollResult.getEmployeeSalary() == null) {
            return null;
        }
        UserProfile userProfile = payrollResult.getEmployeeSalary().getUserProfile();
        return userProfile == null ? null : CustomStringUtils.normalizeCode(userProfile.getCode());
    }



    private YearMonth validateAndResolveRunMonth(YearMonth runDate) {
        if (runDate == null) {
            throw new BadRequestException(Messages.ERROR_PAYROLL_MONTH_INVALID);
        }

        YearMonth minimumAllowedRunMonth = YearMonth.now().minusMonths(MAX_PAST_RUN_MONTHS);
        if (runDate.isBefore(minimumAllowedRunMonth)) {
            throw new BadRequestException(Messages.ERROR_PAYROLL_RUN_MONTH_TOO_OLD);
        }

        return runDate;
    }

    private void validateDateRange(LocalDateTime from, LocalDateTime to, String errorMessage) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new BadRequestException(errorMessage);
        }
    }

    private String resolveSortBy(String sortBy) {
        if (sortBy == null || sortBy.isBlank()) {
            return DEFAULT_SORT_BY;
        }

        return switch (sortBy.trim()) {
            case "code" -> "code";
            case "period" -> "period";
            case "status" -> "status";
            case "runAt" -> "runAt";
            case "closeAt", "closedAt" -> "closedAt";
            case "runBy", "createdBy" -> "createdBy";
            case "updatedBy" -> "updatedBy";
            default -> DEFAULT_SORT_BY;
        };
    }

    private LocalDateTime resolveNullValueForRange(LocalDateTime from, LocalDateTime to) {
        return from == null && to != null ? MAX_FILTER_DATE : MIN_FILTER_DATE;
    }

    @Override
    public void finalizePayrollRun(String payrollCode, boolean isSuccess) {
        payrollRunRepository.updateStatusBySuccessFlag(payrollCode, securityContextService.getCurrentCompanyCode(),
                isSuccess);
    }
}
