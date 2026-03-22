package com.dat.erp.services.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dat.erp.constants.CodePrefixes;
import com.dat.erp.constants.Messages;
import com.dat.erp.dto.request.payroll.PayRateRuleRequest;
import com.dat.erp.dto.request.payroll.PayrollPolicyRequest;
import com.dat.erp.dto.response.payroll.PayrollPolicyResponse;
import com.dat.erp.entities.PayRateRule;
import com.dat.erp.entities.PayrollPolicy;
import com.dat.erp.exceptions.BadRequestException;
import com.dat.erp.exceptions.ConflictException;
import com.dat.erp.exceptions.ResourceNotFoundException;
import com.dat.erp.repositories.customrepositories.PayRateRuleRepository;
import com.dat.erp.repositories.customrepositories.PayrollPolicyRepository;
import com.dat.erp.services.PayrollPolicyService;
import com.dat.erp.services.base.AbstractAuditableService;

@Service
public class PayrollPolicyServiceImpl extends AbstractAuditableService implements PayrollPolicyService {

    private final PayrollPolicyRepository payrollPolicyRepository;
    private final PayRateRuleRepository payRateRuleRepository;

    public PayrollPolicyServiceImpl(
            PayrollPolicyRepository payrollPolicyRepository,
            PayRateRuleRepository payRateRuleRepository) {
        this.payrollPolicyRepository = payrollPolicyRepository;
        this.payRateRuleRepository = payRateRuleRepository;
    }

    @Override
    @Transactional
    public PayrollPolicyResponse create(PayrollPolicyRequest request) {
        validateRequest(request);
        String companyCode = resolveCurrentUserCompanyCode();
        ensureNoOverlap(companyCode, request.effectiveFrom(), request.effectiveTo(), null);

        PayrollPolicy policy = new PayrollPolicy();
        mapPolicy(policy, request);
        generateCodeIfMissing(policy, CodePrefixes.PAYROLL_POLICY);
        applyInsertAudit(policy);
        PayrollPolicy savedPolicy = payrollPolicyRepository.save(policy);
        List<PayRateRule> rateRules = saveRateRules(savedPolicy, request.rateRules());
        return toResponse(savedPolicy, rateRules);
    }

    @Override
    @Transactional
    public PayrollPolicyResponse update(String code, PayrollPolicyRequest request) {
        validateRequest(request);
        PayrollPolicy policy = findOwnedPolicy(code);
        ensureNoOverlap(policy.getCompanyCode(), request.effectiveFrom(), request.effectiveTo(), policy.getCode());

        mapPolicy(policy, request);
        applyUpdateAudit(policy);
        PayrollPolicy savedPolicy = payrollPolicyRepository.save(policy);

        softDeleteRateRules(savedPolicy.getCode());
        List<PayRateRule> rateRules = saveRateRules(savedPolicy, request.rateRules());
        return toResponse(savedPolicy, rateRules);
    }

    @Override
    @Transactional(readOnly = true)
    public PayrollPolicyResponse get(String code) {
        PayrollPolicy policy = findOwnedPolicy(code);
        return toResponse(policy, findRateRules(policy.getCode()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PayrollPolicyResponse> list() {
        String companyCode = resolveCurrentUserCompanyCode();
        return payrollPolicyRepository.findByCompanyCodeAndIsDeletedFalseOrderByEffectiveFromDesc(companyCode).stream()
                .map(policy -> toResponse(policy, findRateRules(policy.getCode())))
                .toList();
    }

    @Override
    @Transactional
    public void delete(String code) {
        PayrollPolicy policy = findOwnedPolicy(code);
        policy.setIsDeleted(true);
        applyUpdateAudit(policy);
        payrollPolicyRepository.save(policy);
        softDeleteRateRules(policy.getCode());
    }

    private void validateRequest(PayrollPolicyRequest request) {
        if (request == null || request.name() == null || request.name().isBlank()) {
            throw new BadRequestException(Messages.ERROR_PAYROLL_POLICY_NAME_INVALID);
        }
        if (request.effectiveFrom() == null || request.effectiveTo() == null || request.effectiveFrom().isAfter(request.effectiveTo())) {
            throw new BadRequestException(Messages.ERROR_PAYROLL_PERIOD_INVALID);
        }
    }

    private void ensureNoOverlap(String companyCode, java.time.LocalDate effectiveFrom, java.time.LocalDate effectiveTo,
            String ignoreCode) {
        boolean overlap = payrollPolicyRepository.findByCompanyCodeAndIsDeletedFalseOrderByEffectiveFromDesc(companyCode).stream()
                .filter(policy -> !Objects.equals(policy.getCode(), ignoreCode))
                .anyMatch(policy -> !policy.getEffectiveFrom().isAfter(effectiveTo) && !policy.getEffectiveTo().isBefore(effectiveFrom));
        if (overlap) {
            throw new ConflictException(Messages.ERROR_PAYROLL_POLICY_OVERLAPS);
        }
    }

    private PayrollPolicy findOwnedPolicy(String code) {
        PayrollPolicy policy = payrollPolicyRepository.findByCodeAndIsDeletedFalse(code)
                .orElseThrow(() -> new ResourceNotFoundException(Messages.ERROR_PAYROLL_POLICY_NOT_FOUND));
        if (!Objects.equals(policy.getCompanyCode(), resolveCurrentUserCompanyCode())) {
            throw new ResourceNotFoundException(Messages.ERROR_PAYROLL_POLICY_NOT_FOUND);
        }
        return policy;
    }

    private void mapPolicy(PayrollPolicy policy, PayrollPolicyRequest request) {
        policy.setName(request.name().trim());
        policy.setProrationBasis(request.prorationBasis());
        policy.setStandardDaysPerWeek(request.standardDaysPerWeek());
        policy.setPayHolidayIfOff(request.payHolidayIfOff());
        policy.setRoundingRule(request.roundingRule());
        policy.setStandardHoursPerDay(request.standardHoursPerDay());
        policy.setStandardMinutesPerDay(request.standardMinutesPerDay());
        policy.setRoundingMode(request.roundingMode());
        policy.setRoundAt(request.roundAt());
        policy.setZeroDenominatorAction(request.zeroDenominatorAction());
        policy.setHolidayWeekendOverlapRule(request.holidayWeekendOverlapRule());
        policy.setApprovalMode(request.approvalMode());
        policy.setFreezeSnapshotRequired(request.freezeSnapshotRequired());
        policy.setNightPremiumStart(request.nightPremiumStart());
        policy.setNightPremiumEnd(request.nightPremiumEnd());
        policy.setOtRequiresApproval(request.otRequiresApproval());
        policy.setOtMinimumMinutes(request.otMinimumMinutes());
        policy.setOtRoundingMinutes(request.otRoundingMinutes());
        policy.setEffectiveFrom(request.effectiveFrom());
        policy.setEffectiveTo(request.effectiveTo());
    }

    private List<PayRateRule> saveRateRules(PayrollPolicy policy, List<PayRateRuleRequest> requests) {
        List<PayRateRule> rateRules = new ArrayList<>();
        if (requests == null || requests.isEmpty()) {
            return rateRules;
        }
        for (PayRateRuleRequest request : requests) {
            PayRateRule rateRule = new PayRateRule();
            rateRule.setPolicy(policy);
            rateRule.setRateName(request.rateName());
            rateRule.setRateType(request.rateType());
            rateRule.setDayType(request.dayType());
            rateRule.setMultiplier(request.multiplier());
            rateRule.setAppliesTo(request.appliesTo());
            rateRule.setPriority(request.priority() == null ? 100 : request.priority());
            rateRule.setStartTime(request.startTime());
            rateRule.setEndTime(request.endTime());
            rateRule.setMinimumMinutes(request.minimumMinutes());
            rateRule.setRoundingMinutes(request.roundingMinutes());
            rateRule.setRequiresApproval(request.requiresApproval());
            rateRule.setDepartmentCode(request.departmentCode());
            rateRule.setLocationCode(request.locationCode());
            rateRule.setEmploymentType(request.employmentType());
            rateRule.setSalaryCode(request.salaryCode());
            rateRule.setIsStackable(request.isStackable());
            rateRule.setEffectiveFrom(request.effectiveFrom() == null ? policy.getEffectiveFrom() : request.effectiveFrom());
            rateRule.setEffectiveTo(request.effectiveTo() == null ? policy.getEffectiveTo() : request.effectiveTo());
            generateCodeIfMissing(rateRule, CodePrefixes.PAY_RATE_RULE);
            applyInsertAudit(rateRule);
            rateRules.add(rateRule);
        }
        return payRateRuleRepository.saveAll(rateRules);
    }

    private void softDeleteRateRules(String policyCode) {
        List<PayRateRule> rules = findRateRules(policyCode);
        if (rules.isEmpty()) {
            return;
        }
        for (PayRateRule rule : rules) {
            rule.setIsDeleted(true);
            applyUpdateAudit(rule);
        }
        payRateRuleRepository.saveAll(rules);
    }

    private List<PayRateRule> findRateRules(String policyCode) {
        return payRateRuleRepository.findByPolicy_CodeAndIsDeletedFalseOrderByPriorityAscIdAsc(policyCode);
    }

    private PayrollPolicyResponse toResponse(PayrollPolicy policy, List<PayRateRule> rateRules) {
        return new PayrollPolicyResponse(
                policy.getCode(),
                policy.getName(),
                policy.getProrationBasis(),
                policy.getStandardDaysPerWeek(),
                policy.getPayHolidayIfOff(),
                policy.getRoundingRule(),
                policy.getStandardHoursPerDay(),
                policy.getStandardMinutesPerDay(),
                policy.getRoundingMode(),
                policy.getRoundAt(),
                policy.getZeroDenominatorAction(),
                policy.getHolidayWeekendOverlapRule(),
                policy.getApprovalMode(),
                policy.getFreezeSnapshotRequired(),
                policy.getNightPremiumStart(),
                policy.getNightPremiumEnd(),
                policy.getOtRequiresApproval(),
                policy.getOtMinimumMinutes(),
                policy.getOtRoundingMinutes(),
                policy.getEffectiveFrom(),
                policy.getEffectiveTo(),
                rateRules.stream().map(this::toRateRuleRequest).toList());
    }

    private PayRateRuleRequest toRateRuleRequest(PayRateRule rateRule) {
        return new PayRateRuleRequest(
                rateRule.getRateName(),
                rateRule.getRateType(),
                rateRule.getDayType(),
                rateRule.getMultiplier(),
                rateRule.getAppliesTo(),
                rateRule.getPriority(),
                rateRule.getStartTime(),
                rateRule.getEndTime(),
                rateRule.getMinimumMinutes(),
                rateRule.getRoundingMinutes(),
                rateRule.getRequiresApproval(),
                rateRule.getDepartmentCode(),
                rateRule.getLocationCode(),
                rateRule.getEmploymentType(),
                rateRule.getSalaryCode(),
                rateRule.getIsStackable(),
                rateRule.getEffectiveFrom(),
                rateRule.getEffectiveTo());
    }
}
