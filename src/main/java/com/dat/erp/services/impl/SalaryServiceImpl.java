package com.dat.erp.services.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dat.erp.constants.CodePrefixes;
import com.dat.erp.constants.Messages;
import com.dat.erp.constants.SalaryCalculateMethod;
import com.dat.erp.dto.request.SalaryRequest;
import com.dat.erp.dto.response.SalaryResponse;
import com.dat.erp.entities.Salary;
import com.dat.erp.exceptions.BadRequestException;
import com.dat.erp.exceptions.ConflictException;
import com.dat.erp.mapper.interfaces.SalaryMapper;
import com.dat.erp.repositories.customrepositories.SalaryRepository;
import com.dat.erp.services.CodeGenerator;
import com.dat.erp.services.SalaryService;
import com.dat.erp.services.SecurityContextService;
import com.dat.erp.services.base.AbstractAuditableService;

@Service
public class SalaryServiceImpl extends AbstractAuditableService implements SalaryService {

    private final SalaryRepository salaryRepository;
    private final SalaryMapper salaryMapper;

    public SalaryServiceImpl(SalaryRepository salaryRepository,
            SalaryMapper salaryMapper,
            CodeGenerator codeGenerator,
            SecurityContextService securityContextService) {
        this.salaryRepository = salaryRepository;
        this.salaryMapper = salaryMapper;
        this.codeGenerator = codeGenerator;
        this.securityContextService = securityContextService;
    }

    @Override
    @Transactional
    public List<SalaryResponse> createSalaries(List<SalaryRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            throw new BadRequestException(Messages.ERROR_SALARY_REQUESTS_INVALID);
        }

        Set<String> requestNames = new HashSet<>();
        List<String> normalizedNames = new ArrayList<>(requests.size());
        for (SalaryRequest request : requests) {
            if (request == null) {
                throw new BadRequestException(Messages.ERROR_SALARY_REQUESTS_INVALID);
            }

            String name = request.getName() == null ? null : request.getName().trim();
            if (name == null || name.isBlank()) {
                throw new BadRequestException(Messages.ERROR_SALARY_NAME_INVALID);
            }

            String key = name.toLowerCase();
            if (!requestNames.add(key)) {
                throw new BadRequestException(Messages.ERROR_SALARY_NAME_INVALID);
            }
            normalizedNames.add(name.toUpperCase());

            SalaryCalculateMethod calculateMethod = parseCalculateMethod(request.getCalculateMethod());
            if (calculateMethod == null) {
                throw new BadRequestException(Messages.ERROR_SALARY_CALCULATE_METHOD_INVALID);
            }

            if (request.getIsDeduct() == null) {
                throw new BadRequestException(Messages.ERROR_SALARY_IS_DEDUCT_INVALID);
            }
        }

        String companyCode = securityContextService.getCurrentUser().getAccount().getCompanyCode();
        if (companyCode == null || companyCode.isBlank()) {
            throw new BadRequestException(Messages.ERROR_CURRENT_USER_COMPANY_MISSING);
        }

        for (String name : normalizedNames) {
            if (salaryRepository.existsByNameIgnoreCaseAndCompanyCodeAndIsDeletedFalse(name, companyCode)) {
                throw new ConflictException(Messages.ERROR_SALARY_NAME_EXISTS);
            }
        }

        List<Salary> entities = salaryMapper.toEntities(requests);
        for (int i = 0; i < entities.size(); i++) {
            Salary salary = entities.get(i);
            salary.setName(normalizedNames.get(i));
            salary.setCalculateMethod(parseCalculateMethod(requests.get(i).getCalculateMethod()));
            salary.setIsDeduct(requests.get(i).getIsDeduct());

            generateCodeIfMissing(salary, CodePrefixes.SALARY);
            applyInsertAudit(salary);
        }

        List<Salary> persisted = salaryRepository.saveAll(entities);
        return salaryMapper.toResponses(persisted);
    }

    private SalaryCalculateMethod parseCalculateMethod(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return null;
        }
        try {
            return SalaryCalculateMethod.valueOf(rawValue.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}

