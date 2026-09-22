package com.dat.erp.services.payroll.impl;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dat.erp.constants.Messages;
import com.dat.erp.constants.PayrollRerunMode;
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
import com.dat.erp.entities.EmployeeSalary;
import com.dat.erp.entities.PayrollResult;
import com.dat.erp.entities.PayrollRun;
import com.dat.erp.exceptions.BadRequestException;
import com.dat.erp.exceptions.ConflictException;
import com.dat.erp.exceptions.ResourceNotFoundException;
import com.dat.erp.mapper.interfaces.PayrollResultDetailCalculationMapper;
import com.dat.erp.mapper.interfaces.PayrollRunMapper;
import com.dat.erp.repositories.customrepositories.PayrollRunRepository;
import com.dat.erp.services.EmployeeSalaryService;
import com.dat.erp.services.MonthlySalaryCalculationService;
import com.dat.erp.services.SecurityContextService;
import com.dat.erp.services.payroll.PayrollResultDetailService;
import com.dat.erp.services.payroll.PayrollResultService;
import com.dat.erp.services.payroll.PayrollResultSnapshotService;
import com.dat.erp.services.payroll.PayrollRunAuditLogService;
import com.dat.erp.services.payroll.PayrollRunService;
import com.dat.erp.services.payroll.RequiresNewTransactionExecutor;
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
    private final EmployeeSalaryService employeeSalaryService;
    private final MonthlySalaryCalculationService monthlySalaryCalculationService;
    private final PayrollResultDetailService payrollResultDetailService;
    private final PayrollRunAuditLogService payrollRunAuditLogService;
    private final PayrollResultDetailCalculationMapper payrollResultDetailCalculationMapper;
    private final RequiresNewTransactionExecutor requiresNewTransactionExecutor;

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
        if (companyCode == null || companyCode.isBlank() || "SYSTEM".equals(companyCode)) {
            throw new BadRequestException(Messages.ERROR_CURRENT_USER_COMPANY_MISSING);
        }
        log.info("PAYROLL_RUN action=RUN_REQUESTED companyCode={} period={}", companyCode, requestedRunMonth);

        if (payrollRunRepository.findByCompanyCodeAndPeriodAndIsDeletedFalse(companyCode, requestedRunMonth).isPresent()) {
            log.warn("PAYROLL_RUN action=RUN_REJECTED result=DUPLICATE companyCode={} period={}", companyCode,
                    requestedRunMonth);
            throw new ConflictException(Messages.ERROR_PAYROLL_RUN_ALREADY_EXISTS);
        }

        PayrollRun payrollRun = PayrollRun.create(requestedRunMonth);
        payrollRun.start(LocalDateTime.now(ZoneOffset.UTC));
        PayrollRun savedPayrollRun = payrollRunRepository.save(payrollRun);
        log.info("PAYROLL_RUN action=RUN_CREATED result=SUCCESS companyCode={} payrollRunCode={} period={} status={}",
                companyCode, savedPayrollRun.getCode(), requestedRunMonth, savedPayrollRun.getStatus());
        payrollResultService.generatePayrollResult(savedPayrollRun);
        log.info("PAYROLL_RUN action=RESULT_GENERATION_REQUESTED companyCode={} payrollRunCode={} period={}",
                companyCode, savedPayrollRun.getCode(), requestedRunMonth);
        return payrollRunMapper.toResponse(savedPayrollRun);
    }

    @Override
    @Transactional
    public PayrollRerunResponse rerunPayroll(String payrollRunCode, PayrollRerunRequest request) {
        RerunContext context = prepareRerunContext(payrollRunCode, request);
        Map<String, PayrollResult> oldResultsByEmployee = resolveOldResultsByEmployee(context);
        PayrollRerunResponse response = initializeRerunResponse(context, oldResultsByEmployee.size());

        int successCount = 0;
        int failureCount = 0;
        for (Map.Entry<String, PayrollResult> entry : oldResultsByEmployee.entrySet()) {
            RerunEmployeeResult result = rerunEmployee(context, entry.getKey(), entry.getValue());
            if (result.success()) {
                response.getResults().add(result.employeeResponse());
                successCount++;
            } else {
                response.getErrors().add(result.errorResponse());
                failureCount++;
            }
        }

        finalizeRerun(context, response, successCount, failureCount);
        return response;
    }

    private RerunContext prepareRerunContext(String payrollRunCode, PayrollRerunRequest request) {
        String companyCode = securityContextService.getCurrentCompanyCode();
        String normalizedPayrollRunCode = CustomStringUtils.normalizeCode(payrollRunCode);
        if (normalizedPayrollRunCode == null) {
            throw new BadRequestException(Messages.ERROR_PAYROLL_RUN_CODE_INVALID);
        }
        validateRerunRequest(request);

        PayrollRun payrollRun = payrollRunRepository
                .findLockedByCodeAndCompanyCode(normalizedPayrollRunCode, companyCode)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(Messages.ERROR_PAYROLL_RUN_NOT_FOUND, normalizedPayrollRunCode)));
        validateRerunnableStatus(payrollRun);

        String reason = request.getReason().trim();
        String rerunBatchCode = "PRR" + UuidV7.generate();
        RerunContext context = new RerunContext(
                companyCode,
                normalizedPayrollRunCode,
                rerunBatchCode,
                reason,
                request,
                payrollRun,
                securityContextService.getCurrentCompanySecretKey());
        writeRerunAudit(context, PayrollRunAuditActionType.RERUN_REQUESTED, null, null, null,
                PayrollRunAuditStatus.REQUESTED, null);
        logRerun("RERUN_REQUESTED", "REQUESTED", context.payrollRunCode(), context.rerunBatchCode(), null,
                context.reason(), null, null, null, 0L);
        writeRerunAudit(context, PayrollRunAuditActionType.RERUN_STARTED, null, null, null,
                request.isDryRun() ? PayrollRunAuditStatus.DRY_RUN : PayrollRunAuditStatus.STARTED, null);
        return context;
    }

    private Map<String, PayrollResult> resolveOldResultsByEmployee(RerunContext context) {
        List<PayrollResult> targetOldResults = payrollResultService.resolveTargetResults(
                context.payrollRun().getCode(),
                context.companyCode(),
                context.request(),
                context.request().getMode());
        List<String> oldResultsCodes = targetOldResults.stream()
                .filter(Objects::nonNull)
                .map(payrollResult -> payrollResult.getCode())
                .filter(Objects::nonNull)
                .toList();
        return payrollResultService.mapResultsByEmployeeCodeByPayRollResultCodes(oldResultsCodes);
    }

    private PayrollRerunResponse initializeRerunResponse(RerunContext context, int totalEmployees) {
        PayrollRerunResponse response = new PayrollRerunResponse();
        response.setPayrollRunCode(context.payrollRunCode());
        response.setRerunBatchCode(context.rerunBatchCode());
        response.setDryRun(context.request().isDryRun());
        response.setTotalEmployees(totalEmployees);
        return response;
    }

    private RerunEmployeeResult rerunEmployee(RerunContext context, String employeeCode, PayrollResult oldResult) {
        long startedAt = System.currentTimeMillis();
        try {
            return requiresNewTransactionExecutor.execute(
                    () -> rerunEmployeeInIsolatedTransaction(context, employeeCode, oldResult, startedAt));
        } catch (Exception ex) {
            writeRerunAudit(context, PayrollRunAuditActionType.EMPLOYEE_RERUN_FAILED, employeeCode, oldResult, null,
                    PayrollRunAuditStatus.FAILED, ex.getMessage());
            logRerun("EMPLOYEE_RERUN_FAILED", "FAILED", context.payrollRunCode(), context.rerunBatchCode(),
                    employeeCode, context.reason(), null, null, ex.getMessage(),
                    System.currentTimeMillis() - startedAt);
            return RerunEmployeeResult.failure(new PayrollRerunErrorResponse(employeeCode, ex.getMessage()));
        }
    }

    private RerunEmployeeResult rerunEmployeeInIsolatedTransaction(
            RerunContext context,
            String employeeCode,
            PayrollResult oldResult,
            long startedAt) {
        writeRerunAudit(context, PayrollRunAuditActionType.EMPLOYEE_RERUN_STARTED, employeeCode, oldResult, null,
                PayrollRunAuditStatus.STARTED, null);
        if (context.request().isDryRun()) {
            payrollResultSnapshotService.createSnapshot(
                    context.payrollRun(),
                    oldResult,
                    context.rerunBatchCode(),
                    employeeCode);
        }

        MonthlySalaryCalculationResponse calculation = monthlySalaryCalculationService
                .calculateEmployeeMonthlySalary(employeeCode, context.payrollRun().getPeriod());
        PayrollResult newResult = buildNewRerunResult(context, employeeCode, oldResult, calculation);
        if (!context.request().isDryRun()) {
            newResult = persistRerunResult(newResult, oldResult, calculation);
        }

        PayrollRerunEmployeeResultResponse employeeResponse = PayrollRerunEmployeeResultResponse
                .buildEmployeeResponse(employeeCode, oldResult, newResult, context.companySecretKey());
        writeRerunAudit(context, PayrollRunAuditActionType.EMPLOYEE_RERUN_SUCCESS, employeeCode, oldResult,
                context.request().isDryRun() ? null : newResult, PayrollRunAuditStatus.SUCCESS, null);
        logRerun("EMPLOYEE_RERUN_SUCCESS", "SUCCESS", context.payrollRunCode(), context.rerunBatchCode(),
                employeeCode, context.reason(), employeeResponse.getOldActualAmount(),
                employeeResponse.getNewActualAmount(), null, System.currentTimeMillis() - startedAt);
        return RerunEmployeeResult.success(employeeResponse);
    }

    private PayrollResult buildNewRerunResult(
            RerunContext context,
            String employeeCode,
            PayrollResult oldResult,
            MonthlySalaryCalculationResponse calculation) {
        EmployeeSalary activeSalary = employeeSalaryService.getActiveByEmployeeCodeAndDate(
                employeeCode,
                context.payrollRun().getPeriod().atEndOfMonth());
        return payrollResultService.buildRerunPayrollResult(
                context.payrollRun(),
                oldResult,
                activeSalary,
                calculation,
                context.companySecretKey());
    }

    private PayrollResult persistRerunResult(
            PayrollResult newResult,
            PayrollResult oldResult,
            MonthlySalaryCalculationResponse calculation) {
        PayrollResult savedNewResult = payrollResultService.saveRerunPayRollResult(newResult);
        payrollResultDetailService.replacePayrollResultDetailsBestEffort(
                "PAYROLL_RERUN",
                savedNewResult,
                payrollResultDetailCalculationMapper.toDetails(savedNewResult, calculation));
        payrollResultService.softDeleteOldResult(oldResult.getCode());
        return savedNewResult;
    }

    private void finalizeRerun(
            RerunContext context,
            PayrollRerunResponse response,
            int successCount,
            int failureCount) {
        response.setSuccessCount(successCount);
        response.setFailedCount(failureCount);
        PayrollRunStatus finalStatus = failureCount > 0
                ? PayrollRunStatus.FAILED
                : PayrollRunStatus.CALCULATED;
        response.setStatus(finalStatus);
        if (!context.request().isDryRun()) {
            PayrollRun payrollRun = Objects.requireNonNull(context.payrollRun());
            payrollRun.completeRerun(failureCount, false);
            payrollRunRepository.save(payrollRun);
        }
        writeRerunAudit(context,
                failureCount == 0 ? PayrollRunAuditActionType.RERUN_COMPLETED : PayrollRunAuditActionType.RERUN_FAILED,
                null, null, null, toAuditStatus(finalStatus), null);
        logRerun(failureCount == 0 ? "RERUN_COMPLETED" : "RERUN_FAILED", finalStatus.name(),
                context.payrollRunCode(), context.rerunBatchCode(), null, context.reason(), null, null, null, 0L);
    }

    private void writeRerunAudit(
            RerunContext context,
            PayrollRunAuditActionType actionType,
            String employeeCode,
            PayrollResult oldResult,
            PayrollResult newResult,
            PayrollRunAuditStatus status,
            String errorMessage) {
        payrollRunAuditLogService.writeAudit(
                context.payrollRun(),
                new PayrollRunAuditLogRequest(
                        context.rerunBatchCode(),
                        actionType,
                        context.reason(),
                        employeeCode,
                        oldResult,
                        newResult,
                        status,
                        errorMessage));
    }

    private record RerunContext(
            String companyCode,
            String payrollRunCode,
            String rerunBatchCode,
            String reason,
            PayrollRerunRequest request,
            PayrollRun payrollRun,
            String companySecretKey) {
    }

    private record RerunEmployeeResult(
            boolean success,
            PayrollRerunEmployeeResultResponse employeeResponse,
            PayrollRerunErrorResponse errorResponse) {

        private static RerunEmployeeResult success(PayrollRerunEmployeeResultResponse employeeResponse) {
            return new RerunEmployeeResult(true, employeeResponse, null);
        }

        private static RerunEmployeeResult failure(PayrollRerunErrorResponse errorResponse) {
            return new RerunEmployeeResult(false, null, errorResponse);
        }
    }

    private void validateRerunRequest(PayrollRerunRequest request) {
        if (request == null || request.getReason() == null || request.getReason().isBlank()) {
            throw new BadRequestException(Messages.ERROR_PAYROLL_RERUN_REASON_REQUIRED);
        }
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

}
