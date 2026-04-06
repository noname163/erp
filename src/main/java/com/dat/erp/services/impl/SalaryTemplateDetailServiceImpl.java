package com.dat.erp.services.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.dat.erp.constants.CodePrefixes;
import com.dat.erp.constants.Messages;
import com.dat.erp.dto.request.SalaryTemplateDetailRequest;
import com.dat.erp.dto.response.SalaryTemplateDetailListResponse;
import com.dat.erp.entities.Salary;
import com.dat.erp.entities.SalaryTemplate;
import com.dat.erp.entities.SalaryTemplateDetail;
import com.dat.erp.entities.SystemUnit;
import com.dat.erp.exceptions.BadRequestException;
import com.dat.erp.exceptions.ResourceNotFoundException;
import com.dat.erp.mapper.interfaces.SalaryTemplateDetailMapper;
import com.dat.erp.repositories.customrepositories.SalaryRepository;
import com.dat.erp.repositories.customrepositories.SalaryTemplateRepository;
import com.dat.erp.repositories.customrepositories.SalaryTemplateDetailRepository;
import com.dat.erp.repositories.customrepositories.SystemUnitRepository;
import com.dat.erp.services.CodeGenerator;
import com.dat.erp.services.SalaryTemplateDetailService;
import com.dat.erp.services.SecurityContextService;
import com.dat.erp.services.base.AbstractAuditableService;

@Service
public class SalaryTemplateDetailServiceImpl extends AbstractAuditableService implements SalaryTemplateDetailService {

    private final SalaryTemplateDetailRepository salaryTemplateDetailRepository;
    private final SalaryRepository salaryRepository;
    private final SystemUnitRepository systemUnitRepository;
    private final SalaryTemplateRepository salaryTemplateRepository;
    private final SalaryTemplateDetailMapper salaryTemplateDetailMapper;

    public SalaryTemplateDetailServiceImpl(SalaryTemplateDetailRepository salaryTemplateDetailRepository,
            SalaryRepository salaryRepository,
            SystemUnitRepository systemUnitRepository,
            SalaryTemplateRepository salaryTemplateRepository,
            SalaryTemplateDetailMapper salaryTemplateDetailMapper,
            CodeGenerator codeGenerator,
            SecurityContextService securityContextService) {
        this.salaryTemplateDetailRepository = salaryTemplateDetailRepository;
        this.salaryRepository = salaryRepository;
        this.systemUnitRepository = systemUnitRepository;
        this.salaryTemplateRepository = salaryTemplateRepository;
        this.salaryTemplateDetailMapper = salaryTemplateDetailMapper;
        this.codeGenerator = codeGenerator;
        this.securityContextService = securityContextService;
    }

    @Override
    public List<SalaryTemplateDetail> createSalaryTemplateDetails(List<SalaryTemplateDetailRequest> requests,
            SalaryTemplate salaryTemplate) {
        if (requests == null || requests.isEmpty()) {
            throw new BadRequestException(Messages.ERROR_SALARY_TEMPLATE_DETAILS_INVALID);
        }
        if (salaryTemplate == null || salaryTemplate.getCode() == null || salaryTemplate.getCode().isBlank()) {
            throw new BadRequestException("salaryTemplate is invalid");
        }

        Map<SalaryTemplateDetailRequest, String> normalizedSalaryCodes = new HashMap<>(requests.size());
        Map<SalaryTemplateDetailRequest, String> normalizedUnitCodes = new HashMap<>(requests.size());
        Set<String> salaryCodesToLoad = new HashSet<>();
        Set<String> unitCodesToLoad = new HashSet<>();
        List<SalaryTemplateDetail> details = new ArrayList<>(requests.size());
        for (SalaryTemplateDetailRequest request : requests) {
            if (request == null) {
                throw new BadRequestException(Messages.ERROR_SALARY_TEMPLATE_DETAILS_INVALID);
            }

            String salaryCode = request.getSalaryCode() == null ? null : request.getSalaryCode().trim();
            if (salaryCode == null || salaryCode.isBlank()) {
                throw new BadRequestException(Messages.ERROR_SALARY_TEMPLATE_DETAIL_SALARY_CODE_INVALID);
            }
            normalizedSalaryCodes.put(request, salaryCode);
            salaryCodesToLoad.add(salaryCode);

            String unitCode = request.getUnitCode() == null ? null : request.getUnitCode().trim();
            if (unitCode == null || unitCode.isBlank()) {
                throw new BadRequestException(Messages.ERROR_SALARY_TEMPLATE_DETAIL_UNIT_CODE_INVALID);
            }
            normalizedUnitCodes.put(request, unitCode);
            unitCodesToLoad.add(unitCode);
        }

        Map<String, Salary> salaryByCode = salaryRepository.findAllByCodeIn(salaryCodesToLoad).stream()
                .collect(Collectors.toMap(Salary::getCode, salary -> salary));
        Map<String, SystemUnit> unitByCode = systemUnitRepository.findAllByCodeIn(unitCodesToLoad).stream()
                .collect(Collectors.toMap(SystemUnit::getCode, unit -> unit));

        for (SalaryTemplateDetailRequest request : requests) {
            Salary salary = salaryByCode.get(normalizedSalaryCodes.get(request));
            if (salary == null) {
                throw new BadRequestException(Messages.ERROR_SALARY_TEMPLATE_DETAIL_SALARY_CODE_INVALID);
            }

            SystemUnit unit = unitByCode.get(normalizedUnitCodes.get(request));
            if (unit == null) {
                throw new BadRequestException(Messages.ERROR_SALARY_TEMPLATE_DETAIL_UNIT_CODE_INVALID);
            }

            Integer quantity;
            Integer sequenceOrder;
            try {
                quantity = Integer.parseInt(request.getQuantity());
            } catch (NumberFormatException ex) {
                throw new BadRequestException(Messages.ERROR_SALARY_TEMPLATE_DETAIL_QUANTITY_INVALID);
            }
            try {
                sequenceOrder = Integer.parseInt(request.getSequenceOrder());
            } catch (NumberFormatException ex) {
                throw new BadRequestException(Messages.ERROR_SALARY_TEMPLATE_DETAIL_SEQUENCE_ORDER_INVALID);
            }

            SalaryTemplateDetail detail = SalaryTemplateDetail.builder()
                    .salaryTemplate(salaryTemplate)
                    .salary(salary)
                    .unit(unit)
                    .amount(request.getAmount() == null ? null : request.getAmount().trim())
                    .quantity(quantity)
                    .sequenceOrder(sequenceOrder)
                    .isFixed(Boolean.TRUE.equals(request.getIsFixed()))
                    .build();

            generateCodeIfMissing(detail, CodePrefixes.SALARY_TEMPLATE_DETAIL);
            applyInsertAudit(detail);
            details.add(detail);
        }

        return salaryTemplateDetailRepository.saveAll(details);
    }

    @Override
    public List<SalaryTemplateDetailListResponse> getSalaryTemplateDetails(String salaryTemplateCode) {
        if (salaryTemplateCode == null || salaryTemplateCode.isBlank()) {
            throw new BadRequestException("salaryTemplateCode is invalid");
        }

        String companyCode = resolveCurrentUserCompanyCode();
        String normalizedCode = salaryTemplateCode.trim();

        salaryTemplateRepository.findByCodeAndCompanyCodeAndIsDeletedFalse(normalizedCode, companyCode)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(Messages.ERROR_SALARY_TEMPLATE_NOT_FOUND_WITH_CODE, normalizedCode)));

        return salaryTemplateDetailMapper.toListResponses(
                salaryTemplateDetailRepository.findBySalaryTemplateCodeAndCompanyCode(normalizedCode, companyCode));
    }
}
