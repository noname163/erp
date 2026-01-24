package com.dat.erp.services.impl;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dat.erp.constants.CodePrefixes;
import com.dat.erp.constants.Messages;
import com.dat.erp.dto.request.EmployeeSalaryRequest;
import com.dat.erp.dto.response.EmployeeSalaryResponse;
import com.dat.erp.entities.Company;
import com.dat.erp.entities.EmployeeSalary;
import com.dat.erp.entities.UserProfile;
import com.dat.erp.exceptions.BadRequestException;
import com.dat.erp.exceptions.ConflictException;
import com.dat.erp.exceptions.ResourceNotFoundException;
import com.dat.erp.mapper.interfaces.EmployeeSalaryMapper;
import com.dat.erp.repositories.customrepositories.CompanyRepository;
import com.dat.erp.repositories.customrepositories.EmployeeSalaryRepository;
import com.dat.erp.repositories.customrepositories.UserProfileRepository;
import com.dat.erp.services.CodeGenerator;
import com.dat.erp.services.EmployeeSalaryService;
import com.dat.erp.services.SecurityContextService;
import com.dat.erp.services.base.AbstractAuditableService;
import com.dat.erp.utils.CompanySecretKeyCryptoUtils;

@Service
public class EmployeeSalaryServiceImpl extends AbstractAuditableService implements EmployeeSalaryService {

    private final EmployeeSalaryRepository employeeSalaryRepository;
    private final CompanyRepository companyRepository;
    private final UserProfileRepository userProfileRepository;
    private final EmployeeSalaryMapper employeeSalaryMapper;

    public EmployeeSalaryServiceImpl(EmployeeSalaryRepository employeeSalaryRepository,
            CompanyRepository companyRepository,
            UserProfileRepository userProfileRepository,
            EmployeeSalaryMapper employeeSalaryMapper,
            CodeGenerator codeGenerator,
            SecurityContextService securityContextService) {
        this.employeeSalaryRepository = employeeSalaryRepository;
        this.companyRepository = companyRepository;
        this.userProfileRepository = userProfileRepository;
        this.employeeSalaryMapper = employeeSalaryMapper;
        this.codeGenerator = codeGenerator;
        this.securityContextService = securityContextService;
    }

    @Override
    @Transactional
    public EmployeeSalaryResponse createEmployeeSalary(EmployeeSalaryRequest request) {
        if (request == null) {
            throw new BadRequestException("request is invalid");
        }

        String userProfileCode = request.getUserProfileCode() == null ? null : request.getUserProfileCode().trim();
        if (userProfileCode == null || userProfileCode.isBlank()) {
            throw new BadRequestException(Messages.ERROR_EMPLOYEE_SALARY_USER_PROFILE_CODE_INVALID);
        }

        LocalDate effectiveFrom = request.getEffectiveFrom();
        LocalDate effectiveTo = request.getEffectiveTo();
        if (effectiveFrom == null || effectiveTo == null || effectiveFrom.isAfter(effectiveTo)) {
            throw new BadRequestException(Messages.ERROR_EMPLOYEE_SALARY_EFFECTIVE_DATES_INVALID);
        }

        BigDecimal totalAmount = parsePositiveBigDecimal(request.getTotalAmount(),
                Messages.ERROR_EMPLOYEE_SALARY_TOTAL_AMOUNT_INVALID);
        String currency = request.getCurrency() == null ? null : request.getCurrency().trim().toUpperCase();
        if (currency == null || currency.isBlank()) {
            throw new BadRequestException(Messages.ERROR_EMPLOYEE_SALARY_CURRENCY_INVALID);
        }

        String companyCode = securityContextService.getCurrentUser().getAccount().getCompanyCode();
        if (companyCode == null || companyCode.isBlank()) {
            throw new BadRequestException(Messages.ERROR_CURRENT_USER_COMPANY_MISSING);
        }

        Company company = companyRepository.findByCode(companyCode)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(Messages.ERROR_COMPANY_NOT_FOUND_WITH_CODE, companyCode)));
        String companySecretKey = company.getSecretKey();
        if (companySecretKey == null || companySecretKey.isBlank()) {
            throw new BadRequestException(Messages.ERROR_EMPLOYEE_SALARY_COMPANY_SECRET_KEY_MISSING);
        }

        UserProfile userProfile = userProfileRepository.findByCode(userProfileCode)
                .orElseThrow(() -> new ResourceNotFoundException(Messages.ERROR_EMPLOYEE_SALARY_EMPLOYEE_NOT_FOUND));

        String employeeCompanyCode = userProfile.getAccount() == null ? null : userProfile.getAccount().getCompanyCode();
        if (employeeCompanyCode == null || employeeCompanyCode.isBlank() || !companyCode.equals(employeeCompanyCode)) {
            throw new BadRequestException(Messages.ERROR_EMPLOYEE_SALARY_EMPLOYEE_COMPANY_MISMATCH);
        }

        if (userProfile.getIsActive() == null || !userProfile.getIsActive()) {
            throw new BadRequestException(Messages.ERROR_EMPLOYEE_SALARY_EMPLOYEE_INACTIVE);
        }

        if (employeeSalaryRepository.existsOverlappingByUserProfileCodeAndCompanyCode(userProfileCode, companyCode,
                effectiveFrom, effectiveTo)) {
            throw new ConflictException(Messages.ERROR_EMPLOYEE_SALARY_PERIOD_OVERLAPS);
        }

        EmployeeSalary employeeSalary = employeeSalaryMapper.toEntity(request);
        employeeSalary.setUserProfile(userProfile);
        employeeSalary.setTotalAmount(CompanySecretKeyCryptoUtils.encrypt(totalAmount.toPlainString(), companySecretKey));
        employeeSalary.setCurrency(currency);

        generateCodeIfMissing(employeeSalary, CodePrefixes.EMPLOYEE_SALARY);
        applyInsertAudit(employeeSalary);

        EmployeeSalary saved = employeeSalaryRepository.save(employeeSalary);
        EmployeeSalaryResponse response = employeeSalaryMapper.toResponse(saved);
        response.setTotalAmount(totalAmount.toPlainString());
        response.setCurrency(currency);
        return response;
    }

    private BigDecimal parsePositiveBigDecimal(String rawValue, String errorMessage) {
        if (rawValue == null || rawValue.isBlank()) {
            throw new BadRequestException(errorMessage);
        }
        try {
            BigDecimal value = new BigDecimal(rawValue.trim());
            if (value.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BadRequestException(errorMessage);
            }
            return value;
        } catch (NumberFormatException ex) {
            throw new BadRequestException(errorMessage);
        }
    }
}
