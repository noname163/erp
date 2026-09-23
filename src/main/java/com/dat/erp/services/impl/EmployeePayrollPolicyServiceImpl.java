package com.dat.erp.services.impl;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dat.erp.constants.CodePrefixes;
import com.dat.erp.constants.Messages;
import com.dat.erp.dto.request.EmployeePayrollPolicyBatchRequest;
import com.dat.erp.dto.request.EmployeePayrollPolicyRequest;
import com.dat.erp.dto.response.EmployeePayrollPolicyResponse;
import com.dat.erp.entities.EmployeePayrollPolicy;
import com.dat.erp.entities.PayrollPolicy;
import com.dat.erp.entities.UserProfile;
import com.dat.erp.exceptions.BadRequestException;
import com.dat.erp.exceptions.ConflictException;
import com.dat.erp.exceptions.ResourceNotFoundException;
import com.dat.erp.mapper.interfaces.EmployeePayrollPolicyMapper;
import com.dat.erp.repositories.customrepositories.EmployeePayrollPolicyRepository;
import com.dat.erp.repositories.customrepositories.PayrollPolicyRepository;
import com.dat.erp.repositories.customrepositories.UserProfileRepository;
import com.dat.erp.services.CodeGenerator;
import com.dat.erp.services.EmployeePayrollPolicyService;
import com.dat.erp.services.SecurityContextService;
import com.dat.erp.utils.CustomStringUtils;

@Service
public class EmployeePayrollPolicyServiceImpl implements EmployeePayrollPolicyService {


    private final SecurityContextService securityContextService;

    private final EmployeePayrollPolicyRepository employeePayrollPolicyRepository;
    private final UserProfileRepository userProfileRepository;
    private final PayrollPolicyRepository payrollPolicyRepository;
    private final EmployeePayrollPolicyMapper employeePayrollPolicyMapper;
    private final CodeGenerator codeGenerator;

    public EmployeePayrollPolicyServiceImpl(
            EmployeePayrollPolicyRepository employeePayrollPolicyRepository,
            UserProfileRepository userProfileRepository,
            PayrollPolicyRepository payrollPolicyRepository,
            EmployeePayrollPolicyMapper employeePayrollPolicyMapper,
            CodeGenerator codeGenerator,
            SecurityContextService securityContextService) {
        this.securityContextService = securityContextService;
        this.employeePayrollPolicyRepository = employeePayrollPolicyRepository;
        this.userProfileRepository = userProfileRepository;
        this.payrollPolicyRepository = payrollPolicyRepository;
        this.employeePayrollPolicyMapper = employeePayrollPolicyMapper;
        this.codeGenerator = codeGenerator;
    }

    @Override
    @Transactional
    public EmployeePayrollPolicyResponse createEmployeePayrollPolicy(EmployeePayrollPolicyRequest request) {
        validateRequest(request);

        UserProfile userProfile = userProfileRepository.findByCodeAndIsDeletedFalseForUpdate(request.getUserProfileCode())
                .orElseThrow(() -> new ResourceNotFoundException(Messages.ERROR_EMPLOYEE_PAYROLL_POLICY_USER_PROFILE_CODE_INVALID));
        PayrollPolicy payrollPolicy = getPayrollPolicy(request.getPayrollPolicyCode());

        ensureCurrentCompanyOwns(userProfile.getCompanyCode());
        ensureCurrentCompanyOwns(payrollPolicy.getCompanyCode());

        ensureNoActiveOverlap(securityContextService.getCurrentCompanyCode(), List.of(userProfile.getCode()), request.getEffectiveFrom(),
                request.getEffectiveTo());

        return toResponse(employeePayrollPolicyRepository
                .save(buildEmployeePayrollPolicy(userProfile, payrollPolicy, request.getEffectiveFrom(), request.getEffectiveTo())));
    }

    @Override
    @Transactional
    public List<EmployeePayrollPolicyResponse> applyPayrollPolicyToEmployees(EmployeePayrollPolicyBatchRequest request) {
        validateRequest(request);

        String companyCode = securityContextService.getCurrentCompanyCode();
        PayrollPolicy payrollPolicy = getPayrollPolicy(request.getPolicyCode());
        ensureCurrentCompanyOwns(payrollPolicy.getCompanyCode());

        List<String> normalizedEmployeeCodes = request.getEmployeeCodes().stream()
                .map(CustomStringUtils::normalizeCode)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (normalizedEmployeeCodes.isEmpty()) {
            throw new BadRequestException(Messages.ERROR_EMPLOYEE_PAYROLL_POLICY_USER_PROFILE_CODES_INVALID);
        }

        List<UserProfile> userProfiles = userProfileRepository
                .findAllByCodeInAndIsDeletedFalseForUpdate(normalizedEmployeeCodes);
        if (userProfiles.size() != normalizedEmployeeCodes.size()) {
            throw new ResourceNotFoundException(Messages.ERROR_EMPLOYEE_PAYROLL_POLICY_USER_PROFILE_CODES_INVALID);
        }

        userProfiles.forEach(userProfile -> ensureCurrentCompanyOwns(userProfile.getCompanyCode()));
        ensureNoActiveOverlap(companyCode, normalizedEmployeeCodes, request.getEffectiveFrom(), request.getEffectiveTo());

        Map<String, UserProfile> userProfileByCode = userProfiles.stream()
                .collect(java.util.stream.Collectors.toMap(UserProfile::getCode, Function.identity()));

        List<EmployeePayrollPolicy> employeePayrollPolicies = normalizedEmployeeCodes.stream()
                .map(userProfileCode -> buildEmployeePayrollPolicy(userProfileByCode.get(userProfileCode), payrollPolicy,
                        request.getEffectiveFrom(), request.getEffectiveTo()))
                .toList();

        return employeePayrollPolicyMapper.toResponses(employeePayrollPolicyRepository.saveAll(employeePayrollPolicies));
    }

    @Override
    @Transactional
    public EmployeePayrollPolicyResponse deactivateEmployeePayrollPolicy(String code) {
        EmployeePayrollPolicy employeePayrollPolicy = employeePayrollPolicyRepository.findByCodeAndIsDeletedFalse(code)
                .orElseThrow(() -> new ResourceNotFoundException(Messages.ERROR_EMPLOYEE_PAYROLL_POLICY_NOT_FOUND));
        ensureCurrentCompanyOwns(employeePayrollPolicy.getCompanyCode());

        employeePayrollPolicy.setIsActive(false);
        return toResponse(employeePayrollPolicyRepository.save(employeePayrollPolicy));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeePayrollPolicyResponse> getEmployeePayrollPolicies(String userProfileCode) {
        UserProfile userProfile = userProfileRepository.findByCodeAndIsDeletedFalse(userProfileCode)
                .orElseThrow(() -> new ResourceNotFoundException(Messages.ERROR_EMPLOYEE_PAYROLL_POLICY_USER_PROFILE_CODE_INVALID));
        ensureCurrentCompanyOwns(userProfile.getCompanyCode());

        return employeePayrollPolicyRepository.findByUserProfileCodeAndIsDeletedFalseOrderByEffectiveFromDesc(userProfileCode)
                .stream()
                .map(employeePayrollPolicyMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, PayrollPolicy> getCompanyPoliciesByEmployeeCodesAndDate(List<String> employeeCodes, LocalDate date) {
        if (date == null) {
            throw new BadRequestException(Messages.ERROR_EMPLOYEE_PAYROLL_POLICY_EFFECTIVE_DATES_INVALID);
        }
        if (employeeCodes == null || employeeCodes.isEmpty()) {
            return Map.of();
        }

        String companyCode = securityContextService.getCurrentCompanyCode();

        List<String> normalizedEmployeeCodes = employeeCodes.stream()
                .map(CustomStringUtils::normalizeCode)
                .filter(code -> code != null)
                .distinct()
                .toList();
        if (normalizedEmployeeCodes.isEmpty()) {
            return Map.of();
        }

        Map<String, PayrollPolicy> policiesByEmployeeCode = new LinkedHashMap<>();
        List<EmployeePayrollPolicy> employeePolicies = employeePayrollPolicyRepository
                .findActivePoliciesByEmployeeCodesAndDate(companyCode, normalizedEmployeeCodes, date);
        for (EmployeePayrollPolicy employeePolicy : employeePolicies) {
            UserProfile userProfile = employeePolicy.getUserProfile();
            PayrollPolicy payrollPolicy = employeePolicy.getPayrollPolicy();
            if (userProfile == null || userProfile.getCode() == null || payrollPolicy == null) {
                continue;
            }
            policiesByEmployeeCode.putIfAbsent(userProfile.getCode(), payrollPolicy);
        }

        return policiesByEmployeeCode;
    }

    private PayrollPolicy getPayrollPolicy(String payrollPolicyCode) {
        return payrollPolicyRepository.findByCodeAndIsDeletedFalse(payrollPolicyCode)
                .orElseThrow(() -> new ResourceNotFoundException(Messages.ERROR_EMPLOYEE_PAYROLL_POLICY_POLICY_CODE_INVALID));
    }

    private void ensureNoActiveOverlap(String companyCode, List<String> userProfileCodes, LocalDate effectiveFrom,
            LocalDate effectiveTo) {
        // Replacing an open-ended assignment closes it the day before the new version.
        List<EmployeePayrollPolicy> current = employeePayrollPolicyRepository.findActivePoliciesByEmployeeCodesAndDate(companyCode, userProfileCodes, effectiveFrom);
        if (current != null) {
            for (EmployeePayrollPolicy previous : current) {
                if (LocalDate.of(9999, 12, 31).equals(previous.getEffectiveTo()) && previous.getEffectiveFrom().isBefore(effectiveFrom)) {
                    previous.setEffectiveTo(effectiveFrom.minusDays(1));
                    employeePayrollPolicyRepository.saveAndFlush(previous);
                }
            }
        }
        List<String> overlappingUserProfileCodes = employeePayrollPolicyRepository.findActiveOverlapUserProfileCodes(
                companyCode, userProfileCodes, effectiveFrom, effectiveTo);
        if (!overlappingUserProfileCodes.isEmpty()) {
            throw new ConflictException(Messages.ERROR_EMPLOYEE_PAYROLL_POLICY_OVERLAPS);
        }
    }

    private EmployeePayrollPolicy buildEmployeePayrollPolicy(UserProfile userProfile, PayrollPolicy payrollPolicy,
            LocalDate effectiveFrom, LocalDate effectiveTo) {
        EmployeePayrollPolicy employeePayrollPolicy = EmployeePayrollPolicy.builder()
                .userProfile(userProfile)
                .payrollPolicy(payrollPolicy)
                .effectiveFrom(effectiveFrom)
                .effectiveTo(effectiveTo)
                .isActive(true)
                .build();
        employeePayrollPolicy.initializeCode(codeGenerator.nextCode(CodePrefixes.EMPLOYEE_PAYROLL_POLICY));
        return employeePayrollPolicy;
    }

    private void validateRequest(EmployeePayrollPolicyRequest request) {
        if (request != null && request.getEffectiveTo() == null) request.setEffectiveTo(LocalDate.of(9999, 12, 31));
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

    private void validateRequest(EmployeePayrollPolicyBatchRequest request) {
        if (request != null && request.getEffectiveTo() == null) request.setEffectiveTo(LocalDate.of(9999, 12, 31));
        if (request == null || request.getEffectiveFrom() == null || request.getEffectiveTo() == null
                || request.getEffectiveFrom().isAfter(request.getEffectiveTo())) {
            throw new BadRequestException(Messages.ERROR_EMPLOYEE_PAYROLL_POLICY_EFFECTIVE_DATES_INVALID);
        }
        if (request.getPolicyCode() == null || request.getPolicyCode().isBlank()) {
            throw new BadRequestException(Messages.ERROR_EMPLOYEE_PAYROLL_POLICY_POLICY_CODE_INVALID);
        }
        if (request.getEmployeeCodes() == null || request.getEmployeeCodes().isEmpty()) {
            throw new BadRequestException(Messages.ERROR_EMPLOYEE_PAYROLL_POLICY_USER_PROFILE_CODES_INVALID);
        }
    }

    private void ensureCurrentCompanyOwns(String companyCode) {
        String currentCompanyCode = securityContextService.getCurrentCompanyCode();
        if (!Objects.equals(currentCompanyCode, companyCode)) {
            throw new ResourceNotFoundException(Messages.ERROR_EMPLOYEE_PAYROLL_POLICY_NOT_FOUND);
        }
    }

    private EmployeePayrollPolicyResponse toResponse(EmployeePayrollPolicy employeePayrollPolicy) {
        return employeePayrollPolicyMapper.toResponse(employeePayrollPolicy);
    }
}
