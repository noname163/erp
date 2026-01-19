package com.dat.erp.services.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.dat.erp.constants.CodePrefixes;
import com.dat.erp.constants.Messages;
import com.dat.erp.dto.request.SalaryTemplateDetailRequest;
import com.dat.erp.entities.Salary;
import com.dat.erp.entities.SalaryTemplate;
import com.dat.erp.entities.SalaryTemplateDetail;
import com.dat.erp.entities.SystemUnit;
import com.dat.erp.exceptions.BadRequestException;
import com.dat.erp.repositories.customrepositories.SalaryRepository;
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

    public SalaryTemplateDetailServiceImpl(SalaryTemplateDetailRepository salaryTemplateDetailRepository,
            SalaryRepository salaryRepository,
            SystemUnitRepository systemUnitRepository,
            CodeGenerator codeGenerator,
            SecurityContextService securityContextService) {
        this.salaryTemplateDetailRepository = salaryTemplateDetailRepository;
        this.salaryRepository = salaryRepository;
        this.systemUnitRepository = systemUnitRepository;
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

        List<SalaryTemplateDetail> details = new ArrayList<>(requests.size());
        for (SalaryTemplateDetailRequest request : requests) {
            if (request == null) {
                throw new BadRequestException(Messages.ERROR_SALARY_TEMPLATE_DETAILS_INVALID);
            }
            Salary salary = salaryRepository.findByCode(request.getSalaryCode())
                    .orElseThrow(() -> new BadRequestException(Messages.ERROR_SALARY_TEMPLATE_DETAIL_SALARY_CODE_INVALID));
            SystemUnit unit = systemUnitRepository.findByCode(request.getUnitCode())
                    .orElseThrow(() -> new BadRequestException(Messages.ERROR_SALARY_TEMPLATE_DETAIL_UNIT_CODE_INVALID));

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
                    .build();

            generateCodeIfMissing(detail, CodePrefixes.SALARY_TEMPLATE_DETAIL);
            applyInsertAudit(detail);
            details.add(detail);
        }

        return salaryTemplateDetailRepository.saveAll(details);
    }
}
