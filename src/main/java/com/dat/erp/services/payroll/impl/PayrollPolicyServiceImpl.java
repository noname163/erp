package com.dat.erp.services.payroll.impl;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dat.erp.constants.Messages;
import com.dat.erp.dto.request.PayrollPolicyRequest;
import com.dat.erp.dto.response.PayrollPolicyResponse;
import com.dat.erp.entities.PayrollPolicy;
import com.dat.erp.entities.SystemUnit;
import com.dat.erp.exceptions.BadRequestException;
import com.dat.erp.exceptions.ConflictException;
import com.dat.erp.mapper.interfaces.PayrollPolicyMapper;
import com.dat.erp.repositories.customrepositories.PayrollPolicyRepository;
import com.dat.erp.repositories.customrepositories.SystemUnitRepository;
import com.dat.erp.services.CodeGenerator;
import com.dat.erp.services.SecurityContextService;
import com.dat.erp.services.payroll.PayrollPolicyService;
import com.dat.erp.utils.CustomStringUtils;

@Service
public class PayrollPolicyServiceImpl implements PayrollPolicyService {

    private final SecurityContextService securityContextService;

    private final PayrollPolicyRepository payrollPolicyRepository;
    private final SystemUnitRepository systemUnitRepository;
    private final PayrollPolicyMapper payrollPolicyMapper;

    public PayrollPolicyServiceImpl(PayrollPolicyRepository payrollPolicyRepository,
            SystemUnitRepository systemUnitRepository,
            PayrollPolicyMapper payrollPolicyMapper,
            CodeGenerator codeGenerator,
            SecurityContextService securityContextService) {
        this.securityContextService = securityContextService;
        this.payrollPolicyRepository = payrollPolicyRepository;
        this.systemUnitRepository = systemUnitRepository;
        this.payrollPolicyMapper = payrollPolicyMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PayrollPolicyResponse> getPayrollPolicies(String name, LocalDate effectiveFrom, LocalDate effectiveTo,
            String unitCode) {
        if (effectiveFrom != null && effectiveTo != null && effectiveFrom.isAfter(effectiveTo)) {
            throw new BadRequestException(Messages.ERROR_PAYROLL_POLICY_EFFECTIVE_DATES_INVALID);
        }

        String companyCode = securityContextService.getCurrentCompanyCode();

        return payrollPolicyMapper.toResponses(
                payrollPolicyRepository.findByFilters(
                        companyCode,
                        CustomStringUtils.trimToNull(name),
                        effectiveFrom,
                        effectiveTo,
                        CustomStringUtils.normalizeCode(unitCode)));
    }

    @Override
    @Transactional
    public PayrollPolicyResponse createPayrollPolicy(PayrollPolicyRequest request) {
        if (request != null && request.getEffectiveTo() == null) request.setEffectiveTo(LocalDate.of(9999, 12, 31));
        validateRequest(request);
        if (request.getStatutorySettings() != null) request.getStatutorySettings().validateOverrides();

        String companyCode = securityContextService.getCurrentCompanyCode();

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
                .statutorySettings(request.getStatutorySettings() == null ? new com.dat.erp.data.PayrollStatutorySettings() : request.getStatutorySettings())
                .standardQuantityPerDay(request.getStandardQuantityPerDay())
                .unit(unit)
                .standardStartTime(request.getStandardStartTime())
                .standardEndTime(request.getStandardEndTime())
                .roundingRule(CustomStringUtils.trimToNull(request.getRoundingRule()))
                .effectiveFrom(request.getEffectiveFrom())
                .effectiveTo(request.getEffectiveTo())
                .build();

        return payrollPolicyMapper.toResponse(payrollPolicyRepository.save(payrollPolicy));
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
}
