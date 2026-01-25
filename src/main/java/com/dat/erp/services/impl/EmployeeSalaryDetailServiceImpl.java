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
import com.dat.erp.entities.SystemUnit;
import com.dat.erp.exceptions.BadRequestException;
import com.dat.erp.exceptions.ConflictException;
import com.dat.erp.exceptions.ResourceNotFoundException;
import com.dat.erp.repositories.customrepositories.EmployeeSalaryDetailRepository;
import com.dat.erp.repositories.customrepositories.EmployeeSalaryRepository;
import com.dat.erp.repositories.customrepositories.SalaryRepository;
import com.dat.erp.repositories.customrepositories.SystemUnitRepository;
import com.dat.erp.services.CodeGenerator;
import com.dat.erp.services.EmployeeSalaryDetailService;
import com.dat.erp.services.SecurityContextService;
import com.dat.erp.services.base.AbstractAuditableService;

@Service
public class EmployeeSalaryDetailServiceImpl extends AbstractAuditableService implements EmployeeSalaryDetailService {

    private final EmployeeSalaryDetailRepository employeeSalaryDetailRepository;
    private final EmployeeSalaryRepository employeeSalaryRepository;
    private final SalaryRepository salaryRepository;
    private final SystemUnitRepository systemUnitRepository;

    public EmployeeSalaryDetailServiceImpl(EmployeeSalaryDetailRepository employeeSalaryDetailRepository,
            EmployeeSalaryRepository employeeSalaryRepository,
            SalaryRepository salaryRepository,
            SystemUnitRepository systemUnitRepository,
            CodeGenerator codeGenerator,
            SecurityContextService securityContextService) {
        this.employeeSalaryDetailRepository = employeeSalaryDetailRepository;
        this.employeeSalaryRepository = employeeSalaryRepository;
        this.salaryRepository = salaryRepository;
        this.systemUnitRepository = systemUnitRepository;
        this.codeGenerator = codeGenerator;
        this.securityContextService = securityContextService;
    }

    @Override
    @Transactional
    public String createEmployeeSalaryDetails(List<EmployeeSalaryDetailRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            throw new BadRequestException(Messages.ERROR_EMPLOYEE_SALARY_DETAILS_INVALID);
        }

        String companyCode = securityContextService.getCurrentUser().getAccount().getCompanyCode();
        if (companyCode == null || companyCode.isBlank()) {
            throw new BadRequestException(Messages.ERROR_CURRENT_USER_COMPANY_MISSING);
        }

        String employeeSalaryCode = normalizeCode(requests.get(0) == null ? null : requests.get(0).getEmployeeSalaryCode());
        if (employeeSalaryCode == null) {
            throw new BadRequestException(Messages.ERROR_EMPLOYEE_SALARY_DETAIL_EMPLOYEE_SALARY_CODE_INVALID);
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

            String requestEmployeeSalaryCode = normalizeCode(request.getEmployeeSalaryCode());
            if (requestEmployeeSalaryCode == null || !employeeSalaryCode.equals(requestEmployeeSalaryCode)) {
                throw new BadRequestException(Messages.ERROR_EMPLOYEE_SALARY_DETAIL_EMPLOYEE_SALARY_CODE_INVALID);
            }

            String salaryCode = normalizeCode(request.getSalaryCode());
            if (salaryCode == null) {
                throw new BadRequestException(Messages.ERROR_EMPLOYEE_SALARY_DETAIL_SALARY_CODE_INVALID);
            }
            if (!salaryCodesInRequest.add(salaryCode)) {
                throw new ConflictException(Messages.ERROR_EMPLOYEE_SALARY_DETAIL_ALREADY_EXISTS);
            }

            if (employeeSalaryDetailRepository.existsByEmployeeSalary_CodeAndSalary_CodeAndIsDeletedFalse(employeeSalaryCode,
                    salaryCode)) {
                throw new ConflictException(Messages.ERROR_EMPLOYEE_SALARY_DETAIL_ALREADY_EXISTS);
            }

            String unitCode = normalizeCode(request.getUnitCode());
            if (unitCode == null) {
                throw new BadRequestException(Messages.ERROR_EMPLOYEE_SALARY_DETAIL_UNIT_CODE_INVALID);
            }

            BigDecimal amount = parsePositiveBigDecimal(request.getAmount(), Messages.ERROR_EMPLOYEE_SALARY_DETAIL_AMOUNT_INVALID);
            Integer quantity = request.getQuantity();
            if (quantity == null || quantity <= 0) {
                throw new BadRequestException(Messages.ERROR_EMPLOYEE_SALARY_DETAIL_QUANTITY_INVALID);
            }

            Salary salary = salaryRepository.findByCode(salaryCode)
                    .orElseThrow(() -> new BadRequestException(Messages.ERROR_EMPLOYEE_SALARY_DETAIL_SALARY_CODE_INVALID));
            SystemUnit unit = systemUnitRepository.findByCode(unitCode)
                    .orElseThrow(() -> new BadRequestException(Messages.ERROR_EMPLOYEE_SALARY_DETAIL_UNIT_CODE_INVALID));

            EmployeeSalaryDetail detail = EmployeeSalaryDetail.builder()
                    .employeeSalary(employeeSalary)
                    .salary(salary)
                    .unit(unit)
                    .amount(amount.toPlainString())
                    .quantity(quantity)
                    .build();
            generateCodeIfMissing(detail, CodePrefixes.EMPLOYEE_SALARY_DETAIL);
            applyInsertAudit(detail);
            details.add(detail);
        }

        employeeSalaryDetailRepository.saveAll(details);

        String userProfileCode = employeeSalary.getUserProfile() == null ? null : employeeSalary.getUserProfile().getCode();
        String messageTarget = (userProfileCode == null || userProfileCode.isBlank()) ? employeeSalaryCode : userProfileCode;
        return String.format(Messages.EMPLOYEE_SALARY_DETAIL_CREATE_SUCCESS, messageTarget);
    }

    private static String normalizeCode(String rawValue) {
        String value = rawValue == null ? null : rawValue.trim();
        return (value == null || value.isBlank()) ? null : value;
    }

    private static BigDecimal parsePositiveBigDecimal(String rawValue, String errorMessage) {
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
