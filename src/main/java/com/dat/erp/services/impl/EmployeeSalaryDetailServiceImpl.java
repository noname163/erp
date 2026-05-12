package com.dat.erp.services.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
        Map<EmployeeSalaryDetailRequest, String> normalizedSalaryCodes = new HashMap<>(requests.size());
        Map<EmployeeSalaryDetailRequest, String> normalizedDependenceCodes = new HashMap<>(requests.size());
        Set<String> salaryCodesToLoad = new HashSet<>();
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
            normalizedSalaryCodes.put(request, salaryCode);
            salaryCodesToLoad.add(salaryCode);

            String dependenceCode = CustomStringUtils.normalizeCode(request.getDependenceCode());
            if (request.getDependenceCode() != null && dependenceCode == null) {
                throw new BadRequestException(Messages.ERROR_EMPLOYEE_SALARY_DETAIL_DEPENDENCE_CODE_INVALID);
            }
            if (dependenceCode != null && !salaryCodesInRequest.contains(dependenceCode)) {
                throw new BadRequestException(
                        Messages.ERROR_EMPLOYEE_SALARY_DETAIL_DEPENDENCE_CODE_MUST_EXIST_IN_REQUEST);
            }
            normalizedDependenceCodes.put(request, dependenceCode);
            if (dependenceCode != null) {
                salaryCodesToLoad.add(dependenceCode);
            }
        }

        List<String> existingSalaryCodes = employeeSalaryDetailRepository.findExistingSalaryCodes(employeeSalaryCode,
                salaryCodesInRequest);
        if (!existingSalaryCodes.isEmpty()) {
            throw new ConflictException(Messages.ERROR_EMPLOYEE_SALARY_DETAIL_ALREADY_EXISTS);
        }

        Map<String, Salary> salaryByCode = salaryRepository.findAllByCodeIn(salaryCodesToLoad).stream()
                .collect(Collectors.toMap(Salary::getCode, salary -> salary));

        for (EmployeeSalaryDetailRequest request : requests) {
            String salaryCode = normalizedSalaryCodes.get(request);
            String dependenceCode = normalizedDependenceCodes.get(request);

            BigDecimal amount = CustomStringUtils.parsePositiveBigDecimal(request.getAmount(),
                    Messages.ERROR_EMPLOYEE_SALARY_DETAIL_AMOUNT_INVALID);

            Salary salary = salaryByCode.get(salaryCode);
            if (salary == null) {
                throw new BadRequestException(Messages.ERROR_EMPLOYEE_SALARY_DETAIL_SALARY_CODE_INVALID);
            }

            Salary dependenceSalary = null;
            if (dependenceCode != null) {
                dependenceSalary = salaryByCode.get(dependenceCode);
                if (dependenceSalary == null) {
                    throw new BadRequestException(Messages.ERROR_EMPLOYEE_SALARY_DETAIL_DEPENDENCE_CODE_INVALID);
                }
            }

            EmployeeSalaryDetail detail = EmployeeSalaryDetail.builder()
                    .employeeSalary(employeeSalary)
                    .salary(salary)
                    .dependenceCode(dependenceSalary)
                    .amount(amount.toPlainString())
                    .dayType(request.getDayType())
                    .isFixed(Boolean.TRUE.equals(request.getIsFixed()))
                    .unitType(request.getUnitType())
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
