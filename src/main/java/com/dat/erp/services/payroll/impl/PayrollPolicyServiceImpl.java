package com.dat.erp.services.payroll.impl;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dat.erp.constants.CodePrefixes;
import com.dat.erp.constants.Messages;
import com.dat.erp.dto.request.PayrollPolicyRequest;
import com.dat.erp.dto.response.PayrollPolicyResponse;
import com.dat.erp.entities.PayrollPolicy;
import com.dat.erp.entities.SystemUnit;
import com.dat.erp.exceptions.BadRequestException;
import com.dat.erp.exceptions.ConflictException;
import com.dat.erp.repositories.customrepositories.PayrollPolicyRepository;
import com.dat.erp.repositories.customrepositories.SystemUnitRepository;
import com.dat.erp.services.CodeGenerator;
import com.dat.erp.services.SecurityContextService;
import com.dat.erp.services.base.AbstractAuditableService;
import com.dat.erp.services.payroll.PayrollPolicyService;
import com.dat.erp.utils.CustomStringUtils;

@Service
public class PayrollPolicyServiceImpl extends AbstractAuditableService implements PayrollPolicyService {

    private final PayrollPolicyRepository payrollPolicyRepository;
    private final SystemUnitRepository systemUnitRepository;

    public PayrollPolicyServiceImpl(PayrollPolicyRepository payrollPolicyRepository,
            SystemUnitRepository systemUnitRepository,
            CodeGenerator codeGenerator,
            SecurityContextService securityContextService) {
        this.payrollPolicyRepository = payrollPolicyRepository;
        this.systemUnitRepository = systemUnitRepository;
        this.codeGenerator = codeGenerator;
        this.securityContextService = securityContextService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PayrollPolicyResponse> getPayrollPolicies(String name, LocalDate effectiveFrom, LocalDate effectiveTo,
            String unitCode) {
        if (effectiveFrom != null && effectiveTo != null && effectiveFrom.isAfter(effectiveTo)) {
            throw new BadRequestException(Messages.ERROR_PAYROLL_POLICY_EFFECTIVE_DATES_INVALID);
        }

        String companyCode = resolveCurrentUserCompanyCode();
        if (companyCode == null || companyCode.isBlank() || "SYSTEM".equals(companyCode)) {
            throw new BadRequestException(Messages.ERROR_CURRENT_USER_COMPANY_MISSING);
        }

        return payrollPolicyRepository.findByFilters(companyCode, normalizeText(name), effectiveFrom, effectiveTo,
                CustomStringUtils.normalizeCode(unitCode))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public PayrollPolicyResponse createPayrollPolicy(PayrollPolicyRequest request) {
        validateRequest(request);

        String companyCode = resolveCurrentUserCompanyCode();
        if (companyCode == null || companyCode.isBlank() || "SYSTEM".equals(companyCode)) {
            throw new BadRequestException(Messages.ERROR_CURRENT_USER_COMPANY_MISSING);
        }

        String name = request.getName().trim();
        if (payrollPolicyRepository.existsOverlappingByNameAndCompanyCode(name, companyCode,
                request.getEffectiveFrom(), request.getEffectiveTo())) {
            throw new ConflictException(Messages.ERROR_PAYROLL_POLICY_NAME_EXISTS);
        }

        SystemUnit unit = null;
        String unitCode = CustomStringUtils.normalizeCode(request.getUnitCode());
        if (unitCode != null) {
            unit = systemUnitRepository.findByCodeAndIsDeletedFalse(unitCode)
                    .orElseThrow(() -> new BadRequestException(Messages.ERROR_PAYROLL_POLICY_UNIT_CODE_INVALID));
        }

        PayrollPolicy payrollPolicy = PayrollPolicy.builder()
                .name(name)
                .standardQuantityPerDay(request.getStandardQuantityPerDay())
                .unit(unit)
                .standardStartTime(request.getStandardStartTime())
                .standardEndTime(request.getStandardEndTime())
                .roundingRule(normalizeText(request.getRoundingRule()))
                .effectiveFrom(request.getEffectiveFrom())
                .effectiveTo(request.getEffectiveTo())
                .build();
        generateCodeIfMissing(payrollPolicy, CodePrefixes.PAYROLL_POLICY);
        applyInsertAudit(payrollPolicy);

        return toResponse(payrollPolicyRepository.save(payrollPolicy));
    }

    private void validateRequest(PayrollPolicyRequest request) {
        if (request == null) {
            throw new BadRequestException("request is invalid");
        }

        if (request.getName() == null || request.getName().isBlank()) {
            throw new BadRequestException(Messages.ERROR_PAYROLL_POLICY_NAME_INVALID);
        }

        if (request.getEffectiveFrom() == null || request.getEffectiveTo() == null
                || request.getEffectiveFrom().isAfter(request.getEffectiveTo())) {
            throw new BadRequestException(Messages.ERROR_PAYROLL_POLICY_EFFECTIVE_DATES_INVALID);
        }

        if (request.getStandardQuantityPerDay() != null && request.getStandardQuantityPerDay() <= 0) {
            throw new BadRequestException(Messages.ERROR_PAYROLL_POLICY_STANDARD_QUANTITY_PER_DAY_INVALID);
        }

        String unitCode = CustomStringUtils.normalizeCode(request.getUnitCode());
        if ((request.getStandardQuantityPerDay() != null && unitCode == null)
                || (request.getStandardQuantityPerDay() == null && unitCode != null)) {
            throw new BadRequestException(Messages.ERROR_PAYROLL_POLICY_UNIT_CODE_INVALID);
        }

        LocalTime standardStartTime = request.getStandardStartTime();
        LocalTime standardEndTime = request.getStandardEndTime();
        if ((standardStartTime == null) != (standardEndTime == null)
                || (standardStartTime != null && !standardEndTime.isAfter(standardStartTime))) {
            throw new BadRequestException(Messages.ERROR_PAYROLL_POLICY_STANDARD_TIME_INVALID);
        }

        if (request.getRoundingRule() != null && request.getRoundingRule().trim().isEmpty()) {
            throw new BadRequestException(Messages.ERROR_PAYROLL_POLICY_ROUNDING_RULE_INVALID);
        }
    }

    private PayrollPolicyResponse toResponse(PayrollPolicy payrollPolicy) {
        return new PayrollPolicyResponse(
                payrollPolicy.getCode(),
                payrollPolicy.getName(),
                payrollPolicy.getStandardQuantityPerDay(),
                payrollPolicy.getUnit() == null ? null : payrollPolicy.getUnit().getCode(),
                payrollPolicy.getStandardStartTime(),
                payrollPolicy.getStandardEndTime(),
                payrollPolicy.getRoundingRule(),
                payrollPolicy.getEffectiveFrom(),
                payrollPolicy.getEffectiveTo());
    }

    private String normalizeText(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isBlank() ? null : trimmed;
    }
}
