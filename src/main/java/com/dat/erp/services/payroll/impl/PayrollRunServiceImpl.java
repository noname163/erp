package com.dat.erp.services.payroll.impl;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
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

import com.dat.erp.constants.CodePrefixes;
import com.dat.erp.constants.Messages;
import com.dat.erp.constants.PayrollResultCalcBasis;
import com.dat.erp.constants.PayrollRerunMode;
import com.dat.erp.constants.PayrollRunAuditActionType;
import com.dat.erp.constants.PayrollRunStatus;
import com.dat.erp.dto.request.PayrollRerunRequest;
import com.dat.erp.dto.response.PagedResponse;
import com.dat.erp.dto.response.PayrollRerunEmployeeResultResponse;
import com.dat.erp.dto.response.PayrollRerunErrorResponse;
import com.dat.erp.dto.response.PayrollRerunResponse;
import com.dat.erp.dto.response.PayrollRunResponse;
import com.dat.erp.dto.response.salary.MonthlySalaryCalculationResponse;
import com.dat.erp.dto.response.salary.MonthlySalaryDetailAuditResponse;
import com.dat.erp.entities.Company;
import com.dat.erp.entities.EmployeeSalary;
import com.dat.erp.entities.PayrollResult;
import com.dat.erp.entities.PayrollResultDetail;
import com.dat.erp.entities.PayrollResultSnapshot;
import com.dat.erp.entities.PayrollRun;
import com.dat.erp.entities.PayrollRunAuditLog;
import com.dat.erp.entities.UserProfile;
import com.dat.erp.exceptions.BadRequestException;
import com.dat.erp.exceptions.ConflictException;
import com.dat.erp.exceptions.ResourceNotFoundException;
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
import com.dat.erp.services.base.AbstractAuditableService;
import com.dat.erp.services.payroll.PayrollResultDetailService;
import com.dat.erp.services.payroll.PayrollResultService;
import com.dat.erp.services.payroll.PayrollRunService;
import com.dat.erp.utils.CompanySecretKeyCryptoUtils;
import com.dat.erp.utils.CustomStringUtils;
import com.dat.erp.utils.PageableUtils;

@Service
public class PayrollRunServiceImpl extends AbstractAuditableService implements PayrollRunService {

    private static final Logger log = LoggerFactory.getLogger(PayrollRunServiceImpl.class);

    private static final int MAX_PAST_RUN_MONTHS = 3;
    private static final String DEFAULT_SORT_BY = "runAt";
    private static final String DEFAULT_SORT_DIR = "DESC";
    private static final LocalDateTime MIN_FILTER_DATE = LocalDateTime.of(1900, 1, 1, 0, 0);
    private static final LocalDateTime MAX_FILTER_DATE = LocalDateTime.of(2999, 12, 31, 23, 59, 59);

    private final PayrollRunRepository payrollRunRepository;
    private final PayrollResultService payrollResultService;
    private final PayrollRunMapper payrollRunMapper;
    private final PayrollResultRepository payrollResultRepository;
    private final PayrollResultDetailRepository payrollResultDetailRepository;
    private final PayrollResultSnapshotRepository payrollResultSnapshotRepository;
    private final PayrollRunAuditLogRepository payrollRunAuditLogRepository;
    private final EmployeeSalaryRepository employeeSalaryRepository;
    private final MonthlySalaryCalculationService monthlySalaryCalculationService;
    private final CompanyRepository companyRepository;
    private final PayrollResultDetailService payrollResultDetailService;

    public PayrollRunServiceImpl(
            PayrollRunRepository payrollRunRepository,
            PayrollResultService payrollResultService,
            PayrollRunMapper payrollRunMapper,
            PayrollResultRepository payrollResultRepository,
            PayrollResultDetailRepository payrollResultDetailRepository,
            PayrollResultSnapshotRepository payrollResultSnapshotRepository,
            PayrollRunAuditLogRepository payrollRunAuditLogRepository,
            EmployeeSalaryRepository employeeSalaryRepository,
            MonthlySalaryCalculationService monthlySalaryCalculationService,
            CompanyRepository companyRepository,
            PayrollResultDetailService payrollResultDetailService,
            CodeGenerator codeGenerator,
            SecurityContextService securityContextService) {
        this.payrollRunRepository = payrollRunRepository;
        this.payrollResultService = payrollResultService;
        this.payrollRunMapper = payrollRunMapper;
        this.payrollResultRepository = payrollResultRepository;
        this.payrollResultDetailRepository = payrollResultDetailRepository;
        this.payrollResultSnapshotRepository = payrollResultSnapshotRepository;
        this.payrollRunAuditLogRepository = payrollRunAuditLogRepository;
        this.employeeSalaryRepository = employeeSalaryRepository;
        this.monthlySalaryCalculationService = monthlySalaryCalculationService;
        this.companyRepository = companyRepository;
        this.payrollResultDetailService = payrollResultDetailService;
        this.codeGenerator = codeGenerator;
        this.securityContextService = securityContextService;
    }

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

        String companyCode = requireCurrentUserCompanyCode();
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
        String companyCode = requireCurrentUserCompanyCode();
        String period = requestedRunMonth.toString();

        log.info("PAYROLL_RUN action=RUN_REQUESTED companyCode={} period={}", companyCode, period);

        if (payrollRunRepository.findByCompanyCodeAndPeriodAndIsDeletedFalse(companyCode, period).isPresent()) {
            log.warn("PAYROLL_RUN action=RUN_REJECTED result=DUPLICATE companyCode={} period={}", companyCode, period);
            throw new ConflictException(Messages.ERROR_PAYROLL_RUN_ALREADY_EXISTS);
        }

        PayrollRun payrollRun = new PayrollRun();
        payrollRun.setCompanyCode(companyCode);
        payrollRun.setPeriod(period);
        payrollRun.setStatus(PayrollRunStatus.OPEN);
        payrollRun.setRunAt(LocalDateTime.now(ZoneOffset.UTC));

        generateCodeIfMissing(payrollRun, CodePrefixes.PAYROLL_RUN);
        applyInsertAudit(payrollRun);
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
        String companyCode = requireCurrentUserCompanyCode();
        String normalizedPayrollRunCode = CustomStringUtils.normalizeCode(payrollRunCode);
        if (normalizedPayrollRunCode == null) {
            throw new BadRequestException(Messages.ERROR_PAYROLL_RUN_CODE_INVALID);
        }
        validateRerunRequest(request);

        boolean dryRun = Boolean.TRUE.equals(request.getDryRun());
        String rerunBatchCode = generateCode(CodePrefixes.PAYROLL_RERUN_BATCH);
        PayrollRun payrollRun = payrollRunRepository.findLockedByCodeAndCompanyCode(normalizedPayrollRunCode, companyCode)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(Messages.ERROR_PAYROLL_RUN_NOT_FOUND, normalizedPayrollRunCode)));
        validateRerunnableStatus(payrollRun);

        YearMonth runMonth = resolveRunMonth(payrollRun);
        String reason = request.getReason().trim();
        PayrollRerunMode mode = request.getMode() == null ? PayrollRerunMode.FULL_RUN : request.getMode();
        writeAudit(payrollRun, rerunBatchCode, PayrollRunAuditActionType.RERUN_REQUESTED, reason, null, null, null,
                "REQUESTED", null);
        logRerun("RERUN_REQUESTED", "REQUESTED", normalizedPayrollRunCode, rerunBatchCode, null, reason, null, null,
                null, 0L);

        writeAudit(payrollRun, rerunBatchCode, PayrollRunAuditActionType.RERUN_STARTED, reason, null, null, null,
                dryRun ? "DRY_RUN" : "STARTED", null);

        List<PayrollResult> targetOldResults = resolveTargetResults(payrollRun, companyCode, request, mode);
        Map<String, PayrollResult> oldResultsByEmployee = mapResultsByEmployeeCode(targetOldResults);
        List<String> targetEmployeeCodes = new ArrayList<>(oldResultsByEmployee.keySet());
        PayrollRerunResponse response = new PayrollRerunResponse();
        response.setPayrollRunCode(normalizedPayrollRunCode);
        response.setRerunBatchCode(rerunBatchCode);
        response.setDryRun(dryRun);
        response.setTotalEmployees(targetEmployeeCodes.size());

        String companySecretKey = resolveCompanySecretKey(companyCode);
        int successCount = 0;
        int failureCount = 0;
        for (String employeeCode : targetEmployeeCodes) {
            long startedAt = System.currentTimeMillis();
            PayrollResult oldResult = oldResultsByEmployee.get(employeeCode);
            try {
                writeAudit(payrollRun, rerunBatchCode, PayrollRunAuditActionType.EMPLOYEE_RERUN_STARTED, reason,
                        employeeCode, oldResult, null, "STARTED", null);
                if (!dryRun) {
                    createSnapshot(payrollRun, oldResult, rerunBatchCode, employeeCode);
                }

                MonthlySalaryCalculationResponse calculation = monthlySalaryCalculationService
                        .calculateEmployeeMonthlySalary(employeeCode, runMonth);
                EmployeeSalary activeSalary = employeeSalaryRepository
                        .findFirstActiveByEmployeeCodeAndCompanyCodeAndDate(
                                employeeCode, companyCode, runMonth.atEndOfMonth())
                        .orElse(oldResult.getEmployeeSalary());
                PayrollResult newResult = buildRerunPayrollResult(payrollRun, oldResult, activeSalary, calculation,
                        companySecretKey);

                if (!dryRun) {
                    PayrollResult savedNewResult = payrollResultRepository.saveAndFlush(newResult);
                    payrollResultDetailService.replacePayrollResultDetailsBestEffort(
                            "PAYROLL_RERUN", savedNewResult, buildPayrollResultDetails(savedNewResult, calculation));
                    softDeleteOldResult(oldResult);
                    newResult = savedNewResult;
                }

                PayrollRerunEmployeeResultResponse employeeResponse = buildEmployeeResponse(
                        employeeCode, oldResult, newResult, companySecretKey);
                response.getResults().add(employeeResponse);
                successCount++;
                writeAudit(payrollRun, rerunBatchCode, PayrollRunAuditActionType.EMPLOYEE_RERUN_SUCCESS, reason,
                        employeeCode, oldResult, dryRun ? null : newResult, "SUCCESS", null);
                logRerun("EMPLOYEE_RERUN_SUCCESS", "SUCCESS", normalizedPayrollRunCode, rerunBatchCode, employeeCode,
                        reason, employeeResponse.getOldActualAmount(), employeeResponse.getNewActualAmount(), null,
                        System.currentTimeMillis() - startedAt);
            } catch (Exception ex) {
                failureCount++;
                response.getErrors().add(new PayrollRerunErrorResponse(employeeCode, ex.getMessage()));
                writeAudit(payrollRun, rerunBatchCode, PayrollRunAuditActionType.EMPLOYEE_RERUN_FAILED, reason,
                        employeeCode, oldResult, null, "FAILED", ex.getMessage());
                logRerun("EMPLOYEE_RERUN_FAILED", "FAILED", normalizedPayrollRunCode, rerunBatchCode, employeeCode,
                        reason, null, null, ex.getMessage(), System.currentTimeMillis() - startedAt);
            }
        }

        response.setSuccessCount(successCount);
        response.setFailedCount(failureCount);
        PayrollRunStatus finalStatus = resolveFinalRerunStatus(successCount, failureCount, dryRun, payrollRun.getStatus());
        response.setStatus(finalStatus);
        if (!dryRun) {
            payrollRun.setStatus(finalStatus);
            applyUpdateAudit(payrollRun);
            payrollRunRepository.save(payrollRun);
        }
        writeAudit(payrollRun, rerunBatchCode,
                failureCount == 0 ? PayrollRunAuditActionType.RERUN_COMPLETED : PayrollRunAuditActionType.RERUN_FAILED,
                reason, null, null, null, finalStatus.name(), null);
        logRerun(failureCount == 0 ? "RERUN_COMPLETED" : "RERUN_FAILED", finalStatus.name(), normalizedPayrollRunCode,
                rerunBatchCode, null, reason, null, null, null, 0L);
        return response;
    }

    private void validateRerunRequest(PayrollRerunRequest request) {
        if (request == null) {
            throw new BadRequestException(Messages.ERROR_PAYROLL_RERUN_MODE_INVALID);
        }
        if (request.getReason() == null || request.getReason().isBlank()) {
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
        if (payrollRun.getStatus() == PayrollRunStatus.PROCESSING || payrollRun.getStatus() == PayrollRunStatus.RERUNNING) {
            throw new ConflictException(Messages.ERROR_PAYROLL_RERUN_CONCURRENT);
        }
    }

    private List<PayrollResult> resolveTargetResults(
            PayrollRun payrollRun,
            String companyCode,
            PayrollRerunRequest request,
            PayrollRerunMode mode) {
        if (mode == PayrollRerunMode.SELECTED_EMPLOYEES) {
            List<String> employeeCodes = request.getEmployeeCodes().stream()
                    .map(CustomStringUtils::normalizeCode)
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();
            return payrollResultRepository.findActiveByRunAndCompanyAndEmployeeCodes(
                    payrollRun.getCode(), companyCode, employeeCodes);
        }
        return payrollResultRepository.findActiveByRunAndCompany(payrollRun.getCode(), companyCode);
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

    private PayrollResult buildRerunPayrollResult(
            PayrollRun payrollRun,
            PayrollResult oldResult,
            EmployeeSalary activeSalary,
            MonthlySalaryCalculationResponse calculation,
            String companySecretKey) {
        PayrollResult payrollResult = new PayrollResult();
        payrollResult.setPayrollRun(payrollRun);
        payrollResult.setEmployeeSalary(activeSalary);
        payrollResult.setExpectedAmount(activeSalary == null ? oldResult.getExpectedAmount() : activeSalary.getTotalAmount());
        payrollResult.setActualAmount(CompanySecretKeyCryptoUtils.encrypt(
                calculation.getFinalSalary().toPlainString(), companySecretKey));
        payrollResult.setCurrency(activeSalary == null ? oldResult.getCurrency() : activeSalary.getCurrency());
        payrollResult.setExpectedQuantity(oldResult.getExpectedQuantity());
        payrollResult.setActualQuantity(calculation.getActualWorkingHourPerMonth() == null
                ? oldResult.getActualQuantity()
                : calculation.getActualWorkingHourPerMonth().intValue());
        payrollResult.setUnit(oldResult.getUnit());
        payrollResult.setSourceType(oldResult.getSourceType());
        payrollResult.setIsRetro(Boolean.TRUE);
        payrollResult.setRetroReason("Payroll re-run");
        generateCodeIfMissing(payrollResult, CodePrefixes.PAYROLL_RESULT);
        applyInsertAudit(payrollResult);
        return payrollResult;
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

    private PayrollResultDetail buildSummaryDetail(PayrollResult payrollResult, MonthlySalaryCalculationResponse calculation) {
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
        preparePayrollResultDetail(detail);
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
        preparePayrollResultDetail(detail);
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
        preparePayrollResultDetail(detail);
        return detail;
    }

    private void preparePayrollResultDetail(PayrollResultDetail detail) {
        detail.setCompanyCode(detail.getPayrollResult() == null ? null : detail.getPayrollResult().getCompanyCode());
        generateCodeIfMissing(detail, CodePrefixes.PAYROLL_RESULT_DETAIL);
        applyInsertAudit(detail);
    }

    private void softDeleteOldResult(PayrollResult oldResult) {
        List<PayrollResultDetail> oldDetails = payrollResultDetailRepository
                .findByPayrollResult_CodeAndIsDeletedFalse(oldResult.getCode());
        oldDetails.forEach(detail -> {
            detail.setIsDeleted(true);
            applyUpdateAudit(detail);
        });
        if (!oldDetails.isEmpty()) {
            payrollResultDetailRepository.saveAll(oldDetails);
        }
        oldResult.setIsDeleted(true);
        applyUpdateAudit(oldResult);
        payrollResultRepository.save(oldResult);
    }

    private void createSnapshot(
            PayrollRun payrollRun,
            PayrollResult oldResult,
            String rerunBatchCode,
            String employeeCode) {
        PayrollResultSnapshot snapshot = new PayrollResultSnapshot();
        snapshot.setPayrollRunCode(payrollRun.getCode());
        snapshot.setPayrollResultCode(oldResult.getCode());
        snapshot.setRerunBatchCode(rerunBatchCode);
        snapshot.setEmployeeSalaryCode(oldResult.getEmployeeSalary() == null ? null : oldResult.getEmployeeSalary().getCode());
        snapshot.setEmployeeCode(employeeCode);
        snapshot.setExpectedAmount(oldResult.getExpectedAmount());
        snapshot.setActualAmount(oldResult.getActualAmount());
        snapshot.setExpectedQuantity(oldResult.getExpectedQuantity());
        snapshot.setActualQuantity(oldResult.getActualQuantity());
        snapshot.setCurrency(oldResult.getCurrency());
        snapshot.setSourceType(oldResult.getSourceType());
        snapshot.setResultJson(toResultJson(oldResult));
        snapshot.setDetailJson(toDetailJson(oldResult));
        snapshot.setSnapshotAt(LocalDateTime.now(ZoneOffset.UTC));
        snapshot.setSnapshotBy(resolveCurrentActorCode());
        generateCodeIfMissing(snapshot, CodePrefixes.PAYROLL_RESULT_SNAPSHOT);
        applyInsertAudit(snapshot);
        payrollResultSnapshotRepository.save(snapshot);
        writeAudit(payrollRun, rerunBatchCode, PayrollRunAuditActionType.OLD_RESULT_SNAPSHOT_CREATED, null,
                employeeCode, oldResult, null, "SUCCESS", null);
    }

    private PayrollRerunEmployeeResultResponse buildEmployeeResponse(
            String employeeCode,
            PayrollResult oldResult,
            PayrollResult newResult,
            String companySecretKey) {
        java.math.BigDecimal oldActualAmount = decryptAmount(oldResult.getActualAmount(), companySecretKey);
        java.math.BigDecimal newActualAmount = decryptAmount(newResult.getActualAmount(), companySecretKey);
        java.math.BigDecimal oldExpectedAmount = decryptAmount(oldResult.getExpectedAmount(), companySecretKey);
        java.math.BigDecimal newExpectedAmount = decryptAmount(newResult.getExpectedAmount(), companySecretKey);
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

    private java.math.BigDecimal decryptAmount(String encryptedAmount, String companySecretKey) {
        if (encryptedAmount == null || encryptedAmount.isBlank()) {
            return java.math.BigDecimal.ZERO;
        }
        return new java.math.BigDecimal(CompanySecretKeyCryptoUtils.decrypt(encryptedAmount, companySecretKey));
    }

    private PayrollRunStatus resolveFinalRerunStatus(
            int successCount,
            int failureCount,
            boolean dryRun,
            PayrollRunStatus currentStatus) {
        if (dryRun) {
            return currentStatus;
        }
        if (successCount == 0 && failureCount > 0) {
            return PayrollRunStatus.FAILED;
        }
        if (failureCount > 0) {
            return PayrollRunStatus.FAILED;
        }
        return PayrollRunStatus.CALCULATED;
    }

    private void writeAudit(
            PayrollRun payrollRun,
            String rerunBatchCode,
            PayrollRunAuditActionType actionType,
            String reason,
            String employeeCode,
            PayrollResult oldResult,
            PayrollResult newResult,
            String status,
            String errorMessage) {
        PayrollRunAuditLog auditLog = new PayrollRunAuditLog();
        auditLog.setPayrollRunCode(payrollRun == null ? null : payrollRun.getCode());
        auditLog.setRerunBatchCode(rerunBatchCode);
        auditLog.setActionType(actionType);
        auditLog.setRequestedBy(resolveCurrentActorCode());
        auditLog.setReason(reason);
        auditLog.setEmployeeCode(employeeCode);
        auditLog.setOldPayrollResultCode(oldResult == null ? null : oldResult.getCode());
        auditLog.setNewPayrollResultCode(newResult == null ? null : newResult.getCode());
        String companyCode = payrollRun == null ? null : payrollRun.getCompanyCode();
        auditLog.setOldActualAmount(amountForAudit(oldResult, companyCode, true));
        auditLog.setNewActualAmount(amountForAudit(newResult, companyCode, true));
        auditLog.setOldExpectedAmount(amountForAudit(oldResult, companyCode, false));
        auditLog.setNewExpectedAmount(amountForAudit(newResult, companyCode, false));
        auditLog.setStatus(status);
        auditLog.setErrorMessage(errorMessage);
        auditLog.setRequestId(MDC.get("requestId"));
        auditLog.setTraceId(MDC.get("traceId"));
        auditLog.setEventCreatedAt(LocalDateTime.now(ZoneOffset.UTC));
        generateCodeIfMissing(auditLog, CodePrefixes.PAYROLL_RUN_AUDIT_LOG);
        applyInsertAudit(auditLog);
        payrollRunAuditLogRepository.save(auditLog);
    }

    private java.math.BigDecimal amountForAudit(PayrollResult payrollResult, String companyCode, boolean actual) {
        if (payrollResult == null || companyCode == null || companyCode.isBlank()) {
            return null;
        }
        try {
            return decryptAmount(actual ? payrollResult.getActualAmount() : payrollResult.getExpectedAmount(),
                    resolveCompanySecretKey(companyCode));
        } catch (Exception ex) {
            return null;
        }
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
        log.info("payroll_rerun action={} requestId={} traceId={} payrollRunCode={} rerunBatchCode={} employeeCode={} oldActualAmount={} newActualAmount={} executionTimeMs={} status={} reason={} errorMessage={}",
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

    private String resolveCurrentActorCode() {
        return securityContextService.getCurrentUser() == null ? "SYSTEM" : securityContextService.getCurrentUser().getCode();
    }

    private String resolveCompanySecretKey(String companyCode) {
        Company company = companyRepository.findByCode(companyCode)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(Messages.ERROR_COMPANY_NOT_FOUND_WITH_CODE, companyCode)));
        if (company.getSecretKey() == null || company.getSecretKey().isBlank()) {
            throw new BadRequestException(Messages.ERROR_EMPLOYEE_SALARY_COMPANY_SECRET_KEY_MISSING);
        }
        return company.getSecretKey();
    }

    private YearMonth resolveRunMonth(PayrollRun payrollRun) {
        if (payrollRun == null || payrollRun.getPeriod() == null || payrollRun.getPeriod().isBlank()) {
            throw new BadRequestException(Messages.ERROR_PAYROLL_MONTH_INVALID);
        }
        try {
            return YearMonth.parse(payrollRun.getPeriod().trim());
        } catch (DateTimeParseException ex) {
            throw new BadRequestException(Messages.ERROR_PAYROLL_MONTH_INVALID);
        }
    }

    private String toResultJson(PayrollResult payrollResult) {
        return "{"
                + "\"code\":\"" + escapeJson(payrollResult.getCode()) + "\","
                + "\"expectedAmount\":\"" + escapeJson(payrollResult.getExpectedAmount()) + "\","
                + "\"actualAmount\":\"" + escapeJson(payrollResult.getActualAmount()) + "\","
                + "\"expectedQuantity\":" + payrollResult.getExpectedQuantity() + ","
                + "\"actualQuantity\":" + payrollResult.getActualQuantity() + ","
                + "\"currency\":\"" + escapeJson(payrollResult.getCurrency()) + "\","
                + "\"sourceType\":\"" + payrollResult.getSourceType() + "\""
                + "}";
    }

    private String toDetailJson(PayrollResult payrollResult) {
        List<PayrollResultDetail> details = payrollResultDetailRepository
                .findByPayrollResult_CodeAndIsDeletedFalse(payrollResult.getCode());
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < details.size(); i++) {
            PayrollResultDetail detail = details.get(i);
            if (i > 0) {
                json.append(",");
            }
            json.append("{")
                    .append("\"code\":\"").append(escapeJson(detail.getCode())).append("\",")
                    .append("\"calcBasis\":\"").append(detail.getCalcBasis()).append("\",")
                    .append("\"basisHours\":").append(detail.getBasisHours()).append(",")
                    .append("\"amount\":").append(detail.getAmount()).append(",")
                    .append("\"formulaNote\":\"").append(escapeJson(detail.getFormulaNote())).append("\"")
                    .append("}");
        }
        json.append("]");
        return json.toString();
    }

    private String escapeJson(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
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
