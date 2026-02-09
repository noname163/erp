package com.dat.erp.services.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dat.erp.constants.CodePrefixes;
import com.dat.erp.constants.Messages;
import com.dat.erp.dto.request.EmployeeSalaryDetailRequest;
import com.dat.erp.entities.EmployeeSalary;
import com.dat.erp.entities.EmployeeSalaryDetail;
import com.dat.erp.entities.Salary;
import com.dat.erp.exceptions.BadRequestException;
import com.dat.erp.exceptions.ConflictException;
import com.dat.erp.exceptions.ResourceNotFoundException;
import com.dat.erp.repositories.customrepositories.EmployeeSalaryDetailRepository;
import com.dat.erp.repositories.customrepositories.EmployeeSalaryRepository;
import com.dat.erp.repositories.customrepositories.SalaryRepository;
import com.dat.erp.services.CodeGenerator;
import com.dat.erp.services.EmployeeSalaryDetailService;
import com.dat.erp.services.SecurityContextService;
import com.dat.erp.services.base.AbstractAuditableService;
import com.dat.erp.utils.CustomStringUtils;

@Service
public class EmployeeSalaryDetailServiceImpl extends AbstractAuditableService implements EmployeeSalaryDetailService {

    private final EmployeeSalaryDetailRepository employeeSalaryDetailRepository;
    private final EmployeeSalaryRepository employeeSalaryRepository;
    private final SalaryRepository salaryRepository;

    public EmployeeSalaryDetailServiceImpl(EmployeeSalaryDetailRepository employeeSalaryDetailRepository,
            EmployeeSalaryRepository employeeSalaryRepository,
            SalaryRepository salaryRepository,
            CodeGenerator codeGenerator,
            SecurityContextService securityContextService) {
        this.employeeSalaryDetailRepository = employeeSalaryDetailRepository;
        this.employeeSalaryRepository = employeeSalaryRepository;
        this.salaryRepository = salaryRepository;
        this.codeGenerator = codeGenerator;
        this.securityContextService = securityContextService;
    }

    @Override
    @Transactional
    public String createEmployeeSalaryDetails(List<EmployeeSalaryDetailRequest> requests, String employeeSalaryCode) {
        if (requests == null || requests.isEmpty()) {
            throw new BadRequestException(Messages.ERROR_EMPLOYEE_SALARY_DETAILS_INVALID);
        }

        String companyCode = securityContextService.getCurrentUser().getAccount().getCompanyCode();
        if (companyCode == null || companyCode.isBlank()) {
            throw new BadRequestException(Messages.ERROR_CURRENT_USER_COMPANY_MISSING);
        }

        EmployeeSalary employeeSalary = employeeSalaryRepository.findByCodeAndIsDeletedFalse(employeeSalaryCode)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(Messages.ERROR_EMPLOYEE_SALARY_NOT_FOUND_WITH_CODE, employeeSalaryCode)));

        if (employeeSalary.getCompanyCode() == null || !companyCode.equals(employeeSalary.getCompanyCode())) {
            throw new BadRequestException(Messages.ERROR_EMPLOYEE_SALARY_DETAIL_EMPLOYEE_SALARY_COMPANY_MISMATCH);
        }

        Set<String> salaryCodesInRequest = new HashSet<>();
        List<EmployeeSalaryDetail> details = new ArrayList<>(requests.size());
        for (EmployeeSalaryDetailRequest request : requests) {
            if (request == null) {
                throw new BadRequestException(Messages.ERROR_EMPLOYEE_SALARY_DETAILS_INVALID);
            }

            String requestEmployeeSalaryCode = CustomStringUtils.normalizeCode(employeeSalaryCode);
            if (requestEmployeeSalaryCode == null || !employeeSalaryCode.equals(requestEmployeeSalaryCode)) {
                throw new BadRequestException(Messages.ERROR_EMPLOYEE_SALARY_DETAIL_EMPLOYEE_SALARY_CODE_INVALID);
            }

            String salaryCode = CustomStringUtils.normalizeCode(request.getSalaryCode());
            if (salaryCode == null) {
                throw new BadRequestException(Messages.ERROR_EMPLOYEE_SALARY_DETAIL_SALARY_CODE_INVALID);
            }
            if (!salaryCodesInRequest.add(salaryCode)) {
                throw new ConflictException(Messages.ERROR_EMPLOYEE_SALARY_DETAIL_ALREADY_EXISTS);
            }

            if (employeeSalaryDetailRepository.existsByEmployeeSalary_CodeAndSalary_CodeAndIsDeletedFalse(
                    employeeSalaryCode,
                    salaryCode)) {
                throw new ConflictException(Messages.ERROR_EMPLOYEE_SALARY_DETAIL_ALREADY_EXISTS);
            }

            BigDecimal amount = CustomStringUtils.parsePositiveBigDecimal(request.getAmount(),
                    Messages.ERROR_EMPLOYEE_SALARY_DETAIL_AMOUNT_INVALID);

            Salary salary = salaryRepository.findByCode(salaryCode)
                    .orElseThrow(
                            () -> new BadRequestException(Messages.ERROR_EMPLOYEE_SALARY_DETAIL_SALARY_CODE_INVALID));

            EmployeeSalaryDetail detail = EmployeeSalaryDetail.builder()
                    .employeeSalary(employeeSalary)
                    .salary(salary)
                    .amount(amount.toPlainString())
                    .build();
            generateCodeIfMissing(detail, CodePrefixes.EMPLOYEE_SALARY_DETAIL);
            applyInsertAudit(detail);
            details.add(detail);
        }

        employeeSalaryDetailRepository.saveAll(details);

        String userProfileCode = employeeSalary.getUserProfile() == null ? null
                : employeeSalary.getUserProfile().getCode();
        String messageTarget = (userProfileCode == null || userProfileCode.isBlank()) ? employeeSalaryCode
                : userProfileCode;
        return String.format(Messages.EMPLOYEE_SALARY_DETAIL_CREATE_SUCCESS, messageTarget);
    }

}
