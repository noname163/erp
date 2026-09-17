package com.dat.erp.services.payroll.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import com.dat.erp.dto.request.PayrollRunAuditLogRequest;
import com.dat.erp.entities.PayrollResult;
import com.dat.erp.entities.PayrollRun;
import com.dat.erp.entities.PayrollRunAuditLog;
import com.dat.erp.repositories.customrepositories.PayrollRunAuditLogRepository;
import com.dat.erp.services.SecurityContextService;
import com.dat.erp.services.payroll.PayrollRunAuditLogService;
import com.dat.erp.utils.CompanySecretKeyCryptoUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PayrollRunAuditLogServiceImpl implements PayrollRunAuditLogService {

    private final PayrollRunAuditLogRepository payrollRunAuditLogRepository;
    private final SecurityContextService securityContextService;

    @Override
    public void writeAudit(PayrollRun payrollRun, PayrollRunAuditLogRequest request) {
        PayrollRunAuditLog auditLog = new PayrollRunAuditLog(
                payrollRun == null ? null : payrollRun.getCode(),
                request.rerunBatchCode(),
                request.actionType(),
                request.reason(),
                request.employeeCode(),
                request.oldResult() == null ? null : request.oldResult().getCode(),
                request.newResult() == null ? null : request.newResult().getCode(),
                amountForAudit(request.oldResult(), true),
                amountForAudit(request.newResult(), true),
                amountForAudit(request.oldResult(), false),
                amountForAudit(request.newResult(), false),
                request.errorMessage(),
                MDC.get("requestId"),
                MDC.get("traceId"),
                LocalDateTime.now(ZoneOffset.UTC));
        auditLog.assignRequestedPersonCode(securityContextService.getCurrentUserCode());
        payrollRunAuditLogRepository.save(auditLog);
    }

    private BigDecimal amountForAudit(PayrollResult payrollResult, boolean actual) {
        if (payrollResult == null) {
            return null;
        }
        try {
            return CompanySecretKeyCryptoUtils.decryptAmount(
                    actual ? payrollResult.getActualAmount() : payrollResult.getExpectedAmount(),
                    securityContextService.getCurrentCompanySecretKey());
        } catch (Exception ex) {
            return null;
        }
    }
}
