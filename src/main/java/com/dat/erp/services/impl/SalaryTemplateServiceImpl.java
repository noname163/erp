package com.dat.erp.services.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dat.erp.constants.CodePrefixes;
import com.dat.erp.constants.Messages;
import com.dat.erp.dto.request.SalaryTemplateDetailRequest;
import com.dat.erp.dto.request.SalaryTemplateRequest;
import com.dat.erp.dto.response.SalaryTemplateResponse;
import com.dat.erp.entities.SalaryTemplate;
import com.dat.erp.exceptions.BadRequestException;
import com.dat.erp.exceptions.ConflictException;
import com.dat.erp.mapper.interfaces.SalaryTemplateMapper;
import com.dat.erp.repositories.customrepositories.SalaryTemplateRepository;
import com.dat.erp.services.SalaryTemplateDetailService;
import com.dat.erp.services.SalaryTemplateService;
import com.dat.erp.services.base.AbstractAuditableService;

@Service
public class SalaryTemplateServiceImpl extends AbstractAuditableService implements SalaryTemplateService {

    private final SalaryTemplateRepository salaryTemplateRepository;
    private final SalaryTemplateMapper salaryTemplateMapper;
    private final SalaryTemplateDetailService salaryTemplateDetailService;

    public SalaryTemplateServiceImpl(SalaryTemplateRepository salaryTemplateRepository,
            SalaryTemplateMapper salaryTemplateMapper,
            SalaryTemplateDetailService salaryTemplateDetailService) {
        this.salaryTemplateRepository = salaryTemplateRepository;
        this.salaryTemplateMapper = salaryTemplateMapper;
        this.salaryTemplateDetailService = salaryTemplateDetailService;
    }

    @Override
    @Transactional
    public SalaryTemplateResponse createSalaryTemplate(SalaryTemplateRequest request) {
        if (request == null) {
            throw new BadRequestException("request is invalid");
        }

        String companyCode = securityContextService.getCurrentUser().getAccount().getCompanyCode();
        LocalDate effectiveFrom = request.getEffectiveFrom();
        LocalDate effectiveTo = request.getEffectiveTo();
        if (effectiveFrom == null || effectiveTo == null || effectiveFrom.isAfter(effectiveTo)) {
            throw new BadRequestException(Messages.ERROR_SALARY_TEMPLATE_EFFECTIVE_DATES_INVALID);
        }

        String name = request.getName() == null ? null : request.getName().trim();
        if (name == null || name.isBlank()) {
            throw new BadRequestException("name is invalid");
        }
        if (salaryTemplateRepository.existsOverlappingByNameAndCompanyCode(name, companyCode, effectiveFrom,
                effectiveTo)) {
            throw new ConflictException(Messages.ERROR_SALARY_TEMPLATE_NAME_EXISTS);
        }

        List<SalaryTemplateDetailRequest> details = request.getDetails();
        if (details == null || details.isEmpty()) {
            throw new BadRequestException(Messages.ERROR_SALARY_TEMPLATE_DETAILS_INVALID);
        }

        BigDecimal requestTotalAmount = parsePositiveBigDecimal(request.getTotalAmount(),
                Messages.ERROR_SALARY_TEMPLATE_TOTAL_AMOUNT_INVALID);
        BigDecimal calculatedTotalAmount = validateAndCalculateDetails(details);

        if (requestTotalAmount.compareTo(calculatedTotalAmount) != 0) {
            throw new BadRequestException(Messages.ERROR_SALARY_TEMPLATE_TOTAL_AMOUNT_MISMATCH);
        }

        SalaryTemplate template = salaryTemplateMapper.toEntity(request);
        if (template.getTotalAmount() == null || template.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException(Messages.ERROR_SALARY_TEMPLATE_TOTAL_AMOUNT_INVALID);
        }

        template.setName(name);
        template.setCurrency(request.getCurrency() == null ? null : request.getCurrency().trim().toUpperCase());
        template.setTotalAmount(calculatedTotalAmount);

        generateCodeIfMissing(template, CodePrefixes.SALARY_TEMPLATE);
        applyInsertAudit(template);

        SalaryTemplate saved = salaryTemplateRepository.save(template);
        salaryTemplateDetailService.createSalaryTemplateDetails(details, saved);
        return salaryTemplateMapper.toResponse(saved);
    }

    private BigDecimal validateAndCalculateDetails(List<SalaryTemplateDetailRequest> details) {
        Set<String> salaryCodes = new HashSet<>();
        Set<Integer> sequenceOrders = new HashSet<>();

        BigDecimal total = BigDecimal.ZERO;
        for (SalaryTemplateDetailRequest detail : details) {
            if (detail == null) {
                throw new BadRequestException(Messages.ERROR_SALARY_TEMPLATE_DETAILS_INVALID);
            }

            String salaryCode = detail.getSalaryCode() == null ? null : detail.getSalaryCode().trim();
            if (salaryCode == null || salaryCode.isBlank()) {
                throw new BadRequestException(Messages.ERROR_SALARY_TEMPLATE_DETAIL_SALARY_CODE_INVALID);
            }
            if (!salaryCodes.add(salaryCode)) {
                throw new BadRequestException(Messages.ERROR_SALARY_TEMPLATE_DETAILS_INVALID);
            }

            String unitCode = detail.getUnitCode() == null ? null : detail.getUnitCode().trim();
            if (unitCode == null || unitCode.isBlank()) {
                throw new BadRequestException(Messages.ERROR_SALARY_TEMPLATE_DETAIL_UNIT_CODE_INVALID);
            }

            BigDecimal amount = parsePositiveBigDecimal(detail.getAmount(),
                    Messages.ERROR_SALARY_TEMPLATE_DETAIL_AMOUNT_INVALID);
            int quantity = parsePositiveInt(detail.getQuantity(), Messages.ERROR_SALARY_TEMPLATE_DETAIL_QUANTITY_INVALID);
            int sequenceOrder = parsePositiveInt(detail.getSequenceOrder(),
                    Messages.ERROR_SALARY_TEMPLATE_DETAIL_SEQUENCE_ORDER_INVALID);

            if (!sequenceOrders.add(sequenceOrder)) {
                throw new BadRequestException(Messages.ERROR_SALARY_TEMPLATE_DETAILS_INVALID);
            }

            total = total.add(amount.multiply(BigDecimal.valueOf(quantity)));
        }

        if (total.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException(Messages.ERROR_SALARY_TEMPLATE_TOTAL_AMOUNT_INVALID);
        }
        return total;
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

    private int parsePositiveInt(String rawValue, String errorMessage) {
        if (rawValue == null || rawValue.isBlank()) {
            throw new BadRequestException(errorMessage);
        }
        try {
            int value = Integer.parseInt(rawValue.trim());
            if (value <= 0) {
                throw new BadRequestException(errorMessage);
            }
            return value;
        } catch (NumberFormatException ex) {
            throw new BadRequestException(errorMessage);
        }
    }
}
