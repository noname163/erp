package com.dat.erp.services.payroll.impl;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneOffset;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dat.erp.constants.CodePrefixes;
import com.dat.erp.constants.Messages;
import com.dat.erp.constants.PayrollRunStatus;
import com.dat.erp.dto.response.PagedResponse;
import com.dat.erp.dto.response.PayrollRunResponse;
import com.dat.erp.entities.PayrollRun;
import com.dat.erp.exceptions.BadRequestException;
import com.dat.erp.exceptions.ConflictException;
import com.dat.erp.repositories.customrepositories.PayrollRunRepository;
import com.dat.erp.services.CodeGenerator;
import com.dat.erp.services.SecurityContextService;
import com.dat.erp.services.base.AbstractAuditableService;
import com.dat.erp.services.payroll.PayrollResultService;
import com.dat.erp.services.payroll.PayrollRunService;
import com.dat.erp.utils.PageableUtils;

@Service
public class PayrollRunServiceImpl extends AbstractAuditableService implements PayrollRunService {

    private static final String DEFAULT_SORT_BY = "runAt";
    private static final String DEFAULT_SORT_DIR = "DESC";
    private static final LocalDateTime MIN_FILTER_DATE = LocalDateTime.of(1900, 1, 1, 0, 0);
    private static final LocalDateTime MAX_FILTER_DATE = LocalDateTime.of(2999, 12, 31, 23, 59, 59);

    private final PayrollRunRepository payrollRunRepository;
    private final PayrollResultService payrollResultService;

    public PayrollRunServiceImpl(
            PayrollRunRepository payrollRunRepository,
            PayrollResultService payrollResultService,
            CodeGenerator codeGenerator,
            SecurityContextService securityContextService) {
        this.payrollRunRepository = payrollRunRepository;
        this.payrollResultService = payrollResultService;
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

        String companyCode = resolveCompanyCode();
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
        return PageableUtils.mapPage(payrollRuns, this::toResponse, Messages.SUCCESS);
    }

    @Override
    @Transactional
    public PayrollRunResponse runPayroll() {
        String companyCode = resolveCompanyCode();
        String period = YearMonth.now().toString();

        if (payrollRunRepository.findByCompanyCodeAndPeriodAndIsDeletedFalse(companyCode, period).isPresent()) {
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
        payrollResultService.generatePayrollResult(savedPayrollRun);
        return toResponse(savedPayrollRun);
    }

    private void validateDateRange(LocalDateTime from, LocalDateTime to, String errorMessage) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new BadRequestException(errorMessage);
        }
    }

    private String resolveCompanyCode() {
        String companyCode = resolveCurrentUserCompanyCode();
        if (companyCode == null || companyCode.isBlank() || "SYSTEM".equals(companyCode)) {
            throw new BadRequestException(Messages.ERROR_CURRENT_USER_COMPANY_MISSING);
        }
        return companyCode;
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

    private PayrollRunResponse toResponse(PayrollRun payrollRun) {
        return new PayrollRunResponse(
                payrollRun.getCode(),
                payrollRun.getPeriod(),
                payrollRun.getStatus(),
                payrollRun.getRunAt(),
                payrollRun.getClosedAt(),
                normalizeAuditValue(payrollRun.getCreatedBy()),
                normalizeAuditValue(payrollRun.getUpdatedBy()));
    }

    private String normalizeAuditValue(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isBlank() ? null : trimmed;
    }

    private LocalDateTime resolveNullValueForRange(LocalDateTime from, LocalDateTime to) {
        return from == null && to != null ? MAX_FILTER_DATE : MIN_FILTER_DATE;
    }
}
