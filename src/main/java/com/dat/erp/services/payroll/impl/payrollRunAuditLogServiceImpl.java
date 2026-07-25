package com.dat.erp.services.payroll.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import com.dat.erp.constants.Messages;
import com.dat.erp.dto.request.PayrollRunAuditLogRequest;
import com.dat.erp.entities.Company;
import com.dat.erp.entities.PayrollResult;
import com.dat.erp.entities.PayrollRun;
import com.dat.erp.entities.PayrollRunAuditLog;
import com.dat.erp.entities.UserProfile;
import com.dat.erp.exceptions.ResourceNotFoundException;
import com.dat.erp.repositories.customrepositories.CompanyRepository;
import com.dat.erp.repositories.customrepositories.PayrollRunAuditLogRepository;
import com.dat.erp.services.SecurityContextService;
import com.dat.erp.services.payroll.payrollRunAuditLogService;
import com.dat.erp.utils.CompanySecretKeyCryptoUtils;

@Service
public class payrollRunAuditLogServiceImpl implements payrollRunAuditLogService {

    private final PayrollRunAuditLogRepository payrollRunAuditLogRepository;
    private final CompanyRepository companyRepository;
    private final SecurityContextService securityContextService;

    public payrollRunAuditLogServiceImpl(
            PayrollRunAuditLogRepository payrollRunAuditLogRepository,
            CompanyRepository companyRepository,
            SecurityContextService securityContextService) {
        this.payrollRunAuditLogRepository = payrollRunAuditLogRepository;
        this.companyRepository = companyRepository;
        this.securityContextService = securityContextService;
    }

    @Override
    public void writeAudit(PayrollRun payrollRun, PayrollRunAuditLogRequest request) {
        String companyCode = payrollRun == null ? null : payrollRun.getCompanyCode();
        PayrollRunAuditLog auditLog = new PayrollRunAuditLog(
                payrollRun == null ? null : payrollRun.getCode(),
                request.rerunBatchCode(),
                request.actionType(),
                request.reason(),
                request.employeeCode(),
                request.oldResult() == null ? null : request.oldResult().getCode(),
                request.newResult() == null ? null : request.newResult().getCode(),
                amountForAudit(request.oldResult(), companyCode, true),
                amountForAudit(request.newResult(), companyCode, true),
                amountForAudit(request.oldResult(), companyCode, false),
                amountForAudit(request.newResult(), companyCode, false),
                request.errorMessage(),
                MDC.get("requestId"),
                MDC.get("traceId"),
                LocalDateTime.now(ZoneOffset.UTC));
        auditLog.assignRequestedPersonCode(securityContextService.getCurrentUserCode());
        payrollRunAuditLogRepository.save(auditLog);
    }

    private BigDecimal amountForAudit(PayrollResult payrollResult, String companyCode, boolean actual) {
        if (payrollResult == null || companyCode == null || companyCode.isBlank()) {
            return null;
        }
        try {
            return CompanySecretKeyCryptoUtils.decryptAmount(
                    actual ? payrollResult.getActualAmount() : payrollResult.getExpectedAmount(),
                    resolveCompanySecretKey(companyCode));
        } catch (Exception ex) {
            return null;
        }
    }

    private String resolveCompanySecretKey(String companyCode) {
        Company company = companyRepository.findByCode(companyCode)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(Messages.ERROR_COMPANY_NOT_FOUND_WITH_CODE, companyCode)));
        if (company.getSecretKey() == null || company.getSecretKey().isBlank()) {
            throw new ResourceNotFoundException(Messages.ERROR_EMPLOYEE_SALARY_COMPANY_SECRET_KEY_MISSING);
        }
        return company.getSecretKey();
    }
}
