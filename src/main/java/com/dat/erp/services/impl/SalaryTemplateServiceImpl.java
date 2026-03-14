package com.dat.erp.services.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dat.erp.constants.CodePrefixes;
import com.dat.erp.constants.Messages;
import com.dat.erp.dto.request.SalaryTemplateDetailRequest;
import com.dat.erp.dto.request.SalaryTemplateRequest;
import com.dat.erp.dto.response.PagedResponse;
import com.dat.erp.dto.response.SalaryTemplateDetailListResponse;
import com.dat.erp.dto.response.SalaryTemplateListResponse;
import com.dat.erp.dto.response.SalaryTemplateResponse;
import com.dat.erp.dto.response.SelectionOptionResponse;
import com.dat.erp.entities.SalaryTemplate;
import com.dat.erp.exceptions.BadRequestException;
import com.dat.erp.exceptions.ConflictException;
import com.dat.erp.mapper.interfaces.SalaryTemplateMapper;
import com.dat.erp.repositories.customrepositories.SalaryTemplateRepository;
import com.dat.erp.services.CodeGenerator;
import com.dat.erp.services.SalaryTemplateDetailService;
import com.dat.erp.services.SalaryTemplateService;
import com.dat.erp.services.SecurityContextService;
import com.dat.erp.services.base.AbstractAuditableService;
import com.dat.erp.utils.PageableUtils;

@Service
public class SalaryTemplateServiceImpl extends AbstractAuditableService implements SalaryTemplateService {

    private final SalaryTemplateRepository salaryTemplateRepository;
    private final SalaryTemplateMapper salaryTemplateMapper;
    private final SalaryTemplateDetailService salaryTemplateDetailService;

    public SalaryTemplateServiceImpl(SalaryTemplateRepository salaryTemplateRepository,
            SalaryTemplateMapper salaryTemplateMapper,
            SalaryTemplateDetailService salaryTemplateDetailService,
            CodeGenerator codeGenerator,
            SecurityContextService securityContextService) {
        this.salaryTemplateRepository = salaryTemplateRepository;
        this.salaryTemplateMapper = salaryTemplateMapper;
        this.salaryTemplateDetailService = salaryTemplateDetailService;
        this.codeGenerator = codeGenerator;
        this.securityContextService = securityContextService;
    }

    @Override
    @Transactional
    public SalaryTemplateResponse createSalaryTemplate(SalaryTemplateRequest request) {
        if (request == null) {
            throw new BadRequestException("request is invalid");
        }

        LocalDate effectiveFrom = request.getEffectiveFrom();
        LocalDate effectiveTo = request.getEffectiveTo();
        if (effectiveFrom.isAfter(effectiveTo)) {
            throw new BadRequestException(Messages.ERROR_SALARY_TEMPLATE_EFFECTIVE_DATES_INVALID);
        }

        String name = request.getName().trim();

        List<SalaryTemplateDetailRequest> details = request.getDetails();

        BigDecimal requestTotalAmount = parsePositiveBigDecimal(request.getTotalAmount(),
                Messages.ERROR_SALARY_TEMPLATE_TOTAL_AMOUNT_INVALID);
        BigDecimal calculatedTotalAmount = validateAndCalculateDetails(details);

        if (requestTotalAmount.compareTo(calculatedTotalAmount) != 0) {
            throw new BadRequestException(Messages.ERROR_SALARY_TEMPLATE_TOTAL_AMOUNT_MISMATCH);
        }

        String companyCode = securityContextService.getCurrentUser().getAccount().getCompanyCode();
        if (companyCode == null || companyCode.isBlank()) {
            throw new BadRequestException(Messages.ERROR_CURRENT_USER_COMPANY_MISSING);
        }
        if (salaryTemplateRepository.existsOverlappingByNameAndCompanyCode(name, companyCode, effectiveFrom,
                effectiveTo)) {
            throw new ConflictException(Messages.ERROR_SALARY_TEMPLATE_NAME_EXISTS);
        }

        SalaryTemplate template = salaryTemplateMapper.toEntity(request);

        template.setName(name);
        template.setCurrency(request.getCurrency() == null ? null : request.getCurrency().trim().toUpperCase());
        template.setTotalAmount(calculatedTotalAmount);

        generateCodeIfMissing(template, CodePrefixes.SALARY_TEMPLATE);
        applyInsertAudit(template);

        SalaryTemplate saved = salaryTemplateRepository.save(template);
        salaryTemplateDetailService.createSalaryTemplateDetails(details, saved);
        return salaryTemplateMapper.toResponse(saved);
    }

    @Override
    public PagedResponse<SalaryTemplateListResponse> getSalaryTemplates(String name, String currency,
            LocalDate effectiveFrom, LocalDate effectiveTo, Integer page, Integer size, String sortBy, String sortDir) {
        if (effectiveFrom != null && effectiveTo != null && effectiveFrom.isAfter(effectiveTo)) {
            throw new BadRequestException(Messages.ERROR_SALARY_TEMPLATE_EFFECTIVE_DATES_INVALID);
        }

        String companyCode = resolveCurrentUserCompanyCode();
        Pageable pageable = PageableUtils.create(page, size, sortBy, sortDir);
        Page<SalaryTemplate> data = salaryTemplateRepository.searchByConditions(companyCode, name, currency, effectiveFrom,
                effectiveTo, pageable);
        return PageableUtils.mapPage(data, salaryTemplateMapper::toListResponse, Messages.SUCCESS);
    }

    @Override
    public PagedResponse<SelectionOptionResponse> getSalaryTemplateOptions(String name, Integer page, Integer size,
            String sortBy, String sortDir) {
        String companyCode = resolveCurrentUserCompanyCode();
        String normalizedName = name == null || name.isBlank() ? null : name.trim();
        Pageable pageable = PageableUtils.create(page, size, sortBy, sortDir);

        Page<SalaryTemplate> data = salaryTemplateRepository.findOptionsByFilters(companyCode, normalizedName, pageable);
        return PageableUtils.mapPage(data, template -> {
            SelectionOptionResponse option = new SelectionOptionResponse();
            option.setCode(template.getCode());
            option.setName(template.getName());
            return option;
        }, Messages.SUCCESS);
    }

    @Override
    public List<SalaryTemplateDetailListResponse> getSalaryTemplateDetails(String salaryTemplateCode) {
        return salaryTemplateDetailService.getSalaryTemplateDetails(salaryTemplateCode);
    }

    private BigDecimal validateAndCalculateDetails(List<SalaryTemplateDetailRequest> details) {
        Set<Integer> sequenceOrders = new HashSet<>();

        BigDecimal total = BigDecimal.ZERO;
        for (SalaryTemplateDetailRequest detail : details) {
            if (detail == null) {
                throw new BadRequestException(Messages.ERROR_SALARY_TEMPLATE_DETAILS_INVALID);
            }

            BigDecimal amount = parsePositiveBigDecimal(detail.getAmount(),
                    Messages.ERROR_SALARY_TEMPLATE_DETAIL_AMOUNT_INVALID);
            int sequenceOrder = parsePositiveInt(detail.getSequenceOrder(),
                    Messages.ERROR_SALARY_TEMPLATE_DETAIL_SEQUENCE_ORDER_INVALID);

            if (!sequenceOrders.add(sequenceOrder)) {
                throw new BadRequestException(Messages.ERROR_SALARY_TEMPLATE_DETAILS_INVALID);
            }

            total = total.add(amount);
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
