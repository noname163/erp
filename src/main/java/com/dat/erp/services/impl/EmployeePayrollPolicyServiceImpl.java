package com.dat.erp.services.impl;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dat.erp.constants.CodePrefixes;
import com.dat.erp.constants.Messages;
import com.dat.erp.dto.request.EmployeePayrollPolicyRequest;
import com.dat.erp.dto.response.EmployeePayrollPolicyResponse;
import com.dat.erp.entities.EmployeePayrollPolicy;
import com.dat.erp.entities.PayrollPolicy;
import com.dat.erp.entities.UserProfile;
import com.dat.erp.exceptions.BadRequestException;
import com.dat.erp.exceptions.ConflictException;
import com.dat.erp.exceptions.ResourceNotFoundException;
import com.dat.erp.repositories.customrepositories.EmployeePayrollPolicyRepository;
import com.dat.erp.repositories.customrepositories.PayrollPolicyRepository;
import com.dat.erp.repositories.customrepositories.UserProfileRepository;
import com.dat.erp.services.CodeGenerator;
import com.dat.erp.services.EmployeePayrollPolicyService;
import com.dat.erp.services.SecurityContextService;
import com.dat.erp.services.base.AbstractAuditableService;

@Service
public class EmployeePayrollPolicyServiceImpl extends AbstractAuditableService implements EmployeePayrollPolicyService {

    private final EmployeePayrollPolicyRepository employeePayrollPolicyRepository;
    private final UserProfileRepository userProfileRepository;
    private final PayrollPolicyRepository payrollPolicyRepository;

    public EmployeePayrollPolicyServiceImpl(
            EmployeePayrollPolicyRepository employeePayrollPolicyRepository,
            UserProfileRepository userProfileRepository,
            PayrollPolicyRepository payrollPolicyRepository,
            CodeGenerator codeGenerator,
            SecurityContextService securityContextService) {
        this.employeePayrollPolicyRepository = employeePayrollPolicyRepository;
        this.userProfileRepository = userProfileRepository;
        this.payrollPolicyRepository = payrollPolicyRepository;
        this.codeGenerator = codeGenerator;
        this.securityContextService = securityContextService;
    }

    @Override
    @Transactional
    public EmployeePayrollPolicyResponse createEmployeePayrollPolicy(EmployeePayrollPolicyRequest request) {
        validateRequest(request);

        UserProfile userProfile = userProfileRepository.findByCodeAndIsDeletedFalseForUpdate(request.getUserProfileCode())
                .orElseThrow(() -> new ResourceNotFoundException(Messages.ERROR_EMPLOYEE_PAYROLL_POLICY_USER_PROFILE_CODE_INVALID));
        PayrollPolicy payrollPolicy = payrollPolicyRepository.findByCodeAndIsDeletedFalse(request.getPayrollPolicyCode())
                .orElseThrow(() -> new ResourceNotFoundException(Messages.ERROR_EMPLOYEE_PAYROLL_POLICY_POLICY_CODE_INVALID));

        ensureCurrentCompanyOwns(userProfile.getCompanyCode());
        ensureCurrentCompanyOwns(payrollPolicy.getCompanyCode());

        if (employeePayrollPolicyRepository.existsActiveOverlap(userProfile.getCode(), request.getEffectiveFrom(),
                request.getEffectiveTo(), null)) {
            throw new ConflictException(Messages.ERROR_EMPLOYEE_PAYROLL_POLICY_OVERLAPS);
        }

        EmployeePayrollPolicy employeePayrollPolicy = EmployeePayrollPolicy.builder()
                .userProfile(userProfile)
                .payrollPolicy(payrollPolicy)
                .effectiveFrom(request.getEffectiveFrom())
                .effectiveTo(request.getEffectiveTo())
                .isActive(true)
                .build();
        generateCodeIfMissing(employeePayrollPolicy, CodePrefixes.EMPLOYEE_PAYROLL_POLICY);
        applyInsertAudit(employeePayrollPolicy);

        return toResponse(employeePayrollPolicyRepository.save(employeePayrollPolicy));
    }

    @Override
    @Transactional
    public EmployeePayrollPolicyResponse deactivateEmployeePayrollPolicy(String code) {
        EmployeePayrollPolicy employeePayrollPolicy = employeePayrollPolicyRepository.findByCodeAndIsDeletedFalse(code)
                .orElseThrow(() -> new ResourceNotFoundException(Messages.ERROR_EMPLOYEE_PAYROLL_POLICY_NOT_FOUND));
        ensureCurrentCompanyOwns(employeePayrollPolicy.getCompanyCode());

        employeePayrollPolicy.setIsActive(false);
        applyUpdateAudit(employeePayrollPolicy);
        return toResponse(employeePayrollPolicyRepository.save(employeePayrollPolicy));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeePayrollPolicyResponse> getEmployeePayrollPolicies(String userProfileCode) {
        UserProfile userProfile = userProfileRepository.findByCodeAndIsDeletedFalse(userProfileCode)
                .orElseThrow(() -> new ResourceNotFoundException(Messages.ERROR_EMPLOYEE_PAYROLL_POLICY_USER_PROFILE_CODE_INVALID));
        ensureCurrentCompanyOwns(userProfile.getCompanyCode());

        return employeePayrollPolicyRepository.findByUserProfile_CodeAndIsDeletedFalseOrderByEffectiveFromDesc(userProfileCode)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private void validateRequest(EmployeePayrollPolicyRequest request) {
        if (request == null || request.getEffectiveFrom() == null || request.getEffectiveTo() == null
                || request.getEffectiveFrom().isAfter(request.getEffectiveTo())) {
            throw new BadRequestException(Messages.ERROR_EMPLOYEE_PAYROLL_POLICY_EFFECTIVE_DATES_INVALID);
        }
        if (request.getUserProfileCode() == null || request.getUserProfileCode().isBlank()) {
            throw new BadRequestException(Messages.ERROR_EMPLOYEE_PAYROLL_POLICY_USER_PROFILE_CODE_INVALID);
        }
        if (request.getPayrollPolicyCode() == null || request.getPayrollPolicyCode().isBlank()) {
            throw new BadRequestException(Messages.ERROR_EMPLOYEE_PAYROLL_POLICY_POLICY_CODE_INVALID);
        }
    }

    private void ensureCurrentCompanyOwns(String companyCode) {
        String currentCompanyCode = resolveCurrentUserCompanyCode();
        if (!Objects.equals(currentCompanyCode, companyCode)) {
            throw new ResourceNotFoundException(Messages.ERROR_EMPLOYEE_PAYROLL_POLICY_NOT_FOUND);
        }
    }

    private EmployeePayrollPolicyResponse toResponse(EmployeePayrollPolicy employeePayrollPolicy) {
        return new EmployeePayrollPolicyResponse(
                employeePayrollPolicy.getCode(),
                employeePayrollPolicy.getUserProfile().getCode(),
                employeePayrollPolicy.getPayrollPolicy().getCode(),
                employeePayrollPolicy.getEffectiveFrom(),
                employeePayrollPolicy.getEffectiveTo(),
                employeePayrollPolicy.getIsActive());
    }
}
