package com.dat.erp.services.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dat.erp.constants.CodePrefixes;
import com.dat.erp.constants.Messages;
import com.dat.erp.constants.SalaryCalculateMethod;
import com.dat.erp.dto.request.SalaryRequest;
import com.dat.erp.dto.response.PagedResponse;
import com.dat.erp.dto.response.SalaryListResponse;
import com.dat.erp.dto.response.SalaryResponse;
import com.dat.erp.dto.response.SelectionOptionResponse;
import com.dat.erp.entities.Salary;
import com.dat.erp.exceptions.BadRequestException;
import com.dat.erp.exceptions.ConflictException;
import com.dat.erp.mapper.interfaces.SalaryMapper;
import com.dat.erp.repositories.customrepositories.SalaryRepository;
import com.dat.erp.services.CodeGenerator;
import com.dat.erp.services.SalaryService;
import com.dat.erp.services.SecurityContextService;
import com.dat.erp.services.base.AbstractAuditableService;
import com.dat.erp.utils.CustomStringUtils;
import com.dat.erp.utils.PageableUtils;

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

            String name = request.getName().trim();

            String key = name.toLowerCase();
            if (!requestNames.add(key)) {
                throw new BadRequestException(Messages.ERROR_SALARY_NAME_INVALID);
            }
            normalizedNames.add(name.toUpperCase());

            SalaryCalculateMethod calculateMethod = parseCalculateMethod(request.getCalculateMethod());
            if (calculateMethod == null) {
                throw new BadRequestException(Messages.ERROR_SALARY_CALCULATE_METHOD_INVALID);
            }
        }

        String companyCode = requireCurrentUserCompanyCode();

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

    @Override
    public PagedResponse<SalaryListResponse> getSalaries(String name, Integer page, Integer size, String sortBy,
            String sortDir) {
        String companyCode = requireCurrentUserCompanyCode();
        String normalizedName = CustomStringUtils.trimToNull(name);
        Pageable pageable = PageableUtils.create(page, size, sortBy, sortDir);
        Page<Salary> salaries = salaryRepository.findOptionsByFilters(companyCode, normalizedName, pageable);

        return PageableUtils.mapPage(salaries, salaryMapper::toListResponse, Messages.SUCCESS);
    }

    @Override
    public PagedResponse<SelectionOptionResponse> getSalaryOptionsByCompanyCode(String name, Integer page, Integer size,
            String sortBy, String sortDir) {
        String companyCode = requireCurrentUserCompanyCode();
        String normalizedName = CustomStringUtils.trimToNull(name);
        Pageable pageable = PageableUtils.create(page, size, sortBy, sortDir);
        Page<Salary> salaries = salaryRepository.findOptionsByFilters(companyCode, normalizedName, pageable);

        return PageableUtils.mapPage(salaries, salaryMapper::toOptionResponse, Messages.SUCCESS);
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
