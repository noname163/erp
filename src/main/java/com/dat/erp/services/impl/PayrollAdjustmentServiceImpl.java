package com.dat.erp.services.impl;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dat.erp.constants.CodePrefixes;
import com.dat.erp.constants.Messages;
import com.dat.erp.constants.PayrollAdjustmentType;
import com.dat.erp.dto.request.payroll.PayrollAdjustmentRequest;
import com.dat.erp.dto.response.payroll.PayrollAdjustmentResponse;
import com.dat.erp.entities.PayrollAdjustment;
import com.dat.erp.entities.Salary;
import com.dat.erp.entities.UserProfile;
import com.dat.erp.exceptions.BadRequestException;
import com.dat.erp.exceptions.ResourceNotFoundException;
import com.dat.erp.repositories.customrepositories.PayrollAdjustmentRepository;
import com.dat.erp.repositories.customrepositories.SalaryRepository;
import com.dat.erp.repositories.customrepositories.UserProfileRepository;
import com.dat.erp.services.PayrollAdjustmentService;
import com.dat.erp.services.base.AbstractAuditableService;

@Service
public class PayrollAdjustmentServiceImpl extends AbstractAuditableService implements PayrollAdjustmentService {

    private final PayrollAdjustmentRepository payrollAdjustmentRepository;
    private final UserProfileRepository userProfileRepository;
    private final SalaryRepository salaryRepository;

    public PayrollAdjustmentServiceImpl(
            PayrollAdjustmentRepository payrollAdjustmentRepository,
            UserProfileRepository userProfileRepository,
            SalaryRepository salaryRepository) {
        this.payrollAdjustmentRepository = payrollAdjustmentRepository;
        this.userProfileRepository = userProfileRepository;
        this.salaryRepository = salaryRepository;
    }

    @Override
    @Transactional
    public PayrollAdjustmentResponse create(PayrollAdjustmentRequest request) {
        ValidationContext context = validateRequest(request);
        PayrollAdjustment adjustment = new PayrollAdjustment();
        mapAdjustment(adjustment, request, context.userProfile(), context.salary());
        generateCodeIfMissing(adjustment, CodePrefixes.PAYROLL_ADJUSTMENT);
        applyInsertAudit(adjustment);
        return toResponse(payrollAdjustmentRepository.save(adjustment));
    }

    @Override
    @Transactional
    public PayrollAdjustmentResponse update(String code, PayrollAdjustmentRequest request) {
        ValidationContext context = validateRequest(request);
        PayrollAdjustment adjustment = findOwnedAdjustment(code);
        mapAdjustment(adjustment, request, context.userProfile(), context.salary());
        applyUpdateAudit(adjustment);
        return toResponse(payrollAdjustmentRepository.save(adjustment));
    }

    @Override
    @Transactional(readOnly = true)
    public PayrollAdjustmentResponse get(String code) {
        return toResponse(findOwnedAdjustment(code));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PayrollAdjustmentResponse> list() {
        String companyCode = resolveCurrentUserCompanyCode();
        return payrollAdjustmentRepository.findByCompanyCodeAndIsDeletedFalseOrderByEffectivePayrollMonthDescCreatedAtDesc(companyCode)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void delete(String code) {
        PayrollAdjustment adjustment = findOwnedAdjustment(code);
        adjustment.setIsDeleted(true);
        applyUpdateAudit(adjustment);
        payrollAdjustmentRepository.save(adjustment);
    }

    private ValidationContext validateRequest(PayrollAdjustmentRequest request) {
        if (request == null || request.reason() == null || request.reason().isBlank()) {
            throw new BadRequestException(Messages.ERROR_PAYROLL_ADJUSTMENT_REASON_REQUIRED);
        }
        parsePositiveAmount(request.amount());
        try {
            YearMonth.parse(request.effectivePayrollMonth());
        } catch (Exception ex) {
            throw new BadRequestException(Messages.ERROR_PAYROLL_PERIOD_INVALID);
        }

        String companyCode = resolveCurrentUserCompanyCode();
        UserProfile userProfile = userProfileRepository.findByCodeAndIsDeletedFalse(request.userProfileCode())
                .orElseThrow(() -> new ResourceNotFoundException(Messages.ERROR_DAILY_WORK_EMPLOYEE_NOT_FOUND));
        if (!Objects.equals(userProfile.getCompanyCode(), companyCode)) {
            throw new ResourceNotFoundException(Messages.ERROR_DAILY_WORK_EMPLOYEE_NOT_FOUND);
        }

        Salary salary = null;
        if (request.salaryCode() != null && !request.salaryCode().isBlank()) {
            salary = salaryRepository.findByCodeAndIsDeletedFalse(request.salaryCode().trim())
                    .orElseThrow(() -> new ResourceNotFoundException(Messages.ERROR_EMPLOYEE_SALARY_DETAIL_SALARY_CODE_INVALID));
            if (!Objects.equals(salary.getCompanyCode(), companyCode)) {
                throw new ResourceNotFoundException(Messages.ERROR_EMPLOYEE_SALARY_DETAIL_SALARY_CODE_INVALID);
            }
        }

        if (request.adjustmentType() == PayrollAdjustmentType.RETRO && request.reason().isBlank()) {
            throw new BadRequestException(Messages.ERROR_PAYROLL_ADJUSTMENT_REASON_REQUIRED);
        }
        return new ValidationContext(userProfile, salary);
    }

    private PayrollAdjustment findOwnedAdjustment(String code) {
        PayrollAdjustment adjustment = payrollAdjustmentRepository.findByCodeAndIsDeletedFalse(code)
                .orElseThrow(() -> new ResourceNotFoundException(Messages.ERROR_PAYROLL_ADJUSTMENT_NOT_FOUND));
        if (!Objects.equals(adjustment.getCompanyCode(), resolveCurrentUserCompanyCode())) {
            throw new ResourceNotFoundException(Messages.ERROR_PAYROLL_ADJUSTMENT_NOT_FOUND);
        }
        return adjustment;
    }

    private void mapAdjustment(PayrollAdjustment adjustment, PayrollAdjustmentRequest request, UserProfile userProfile, Salary salary) {
        adjustment.setUserProfile(userProfile);
        adjustment.setSalary(salary);
        adjustment.setAdjustmentType(request.adjustmentType());
        adjustment.setApprovalStatus(request.approvalStatus());
        adjustment.setAmount(parsePositiveAmount(request.amount()).toPlainString());
        adjustment.setQuantity(request.quantity());
        adjustment.setUnitCode(request.unitCode());
        adjustment.setReason(request.reason().trim());
        adjustment.setSourceMonth(request.sourceMonth());
        adjustment.setEffectivePayrollMonth(request.effectivePayrollMonth());
        adjustment.setIsRetro(request.isRetro() == null ? request.adjustmentType() == PayrollAdjustmentType.RETRO : request.isRetro());
        adjustment.setReferenceCode(request.referenceCode());
        adjustment.setEffectiveDate(request.effectiveDate());
    }

    private PayrollAdjustmentResponse toResponse(PayrollAdjustment adjustment) {
        return new PayrollAdjustmentResponse(
                adjustment.getCode(),
                adjustment.getUserProfile() == null ? null : adjustment.getUserProfile().getCode(),
                adjustment.getSalary() == null ? null : adjustment.getSalary().getCode(),
                adjustment.getAdjustmentType(),
                adjustment.getApprovalStatus(),
                adjustment.getAmount(),
                adjustment.getQuantity(),
                adjustment.getUnitCode(),
                adjustment.getReason(),
                adjustment.getSourceMonth(),
                adjustment.getEffectivePayrollMonth(),
                adjustment.getIsRetro(),
                adjustment.getReferenceCode(),
                adjustment.getEffectiveDate());
    }

    private BigDecimal parsePositiveAmount(String amount) {
        try {
            BigDecimal parsed = new BigDecimal(amount);
            if (parsed.compareTo(BigDecimal.ZERO) <= 0) {
                throw new NumberFormatException("amount must be positive");
            }
            return parsed;
        } catch (Exception ex) {
            throw new BadRequestException(Messages.ERROR_EMPLOYEE_SALARY_DETAIL_AMOUNT_INVALID);
        }
    }

    private record ValidationContext(UserProfile userProfile, Salary salary) {
    }
}
