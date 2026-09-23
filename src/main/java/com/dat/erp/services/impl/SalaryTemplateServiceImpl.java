package com.dat.erp.services.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dat.erp.constants.CodePrefixes;
import com.dat.erp.constants.Messages;
import com.dat.erp.constants.SalaryCalculateMethod;
import com.dat.erp.dto.request.SalaryTemplateDetailRequest;
import com.dat.erp.dto.request.SalaryTemplateRequest;
import com.dat.erp.dto.response.PagedResponse;
import com.dat.erp.dto.response.SalaryTemplateDetailListResponse;
import com.dat.erp.dto.response.SalaryTemplateListResponse;
import com.dat.erp.dto.response.SalaryTemplateResponse;
import com.dat.erp.dto.response.SelectionOptionResponse;
import com.dat.erp.entities.SalaryTemplate;
import com.dat.erp.entities.SalaryTemplateDetail;
import com.dat.erp.exceptions.BadRequestException;
import com.dat.erp.exceptions.ConflictException;
import com.dat.erp.mapper.interfaces.SalaryTemplateMapper;
import com.dat.erp.repositories.customrepositories.SalaryTemplateRepository;
import com.dat.erp.services.CodeGenerator;
import com.dat.erp.services.SalaryTemplateDetailService;
import com.dat.erp.services.SalaryTemplateService;
import com.dat.erp.services.SecurityContextService;
import com.dat.erp.utils.CustomStringUtils;
import com.dat.erp.utils.PageableUtils;

@Service
public class SalaryTemplateServiceImpl implements SalaryTemplateService {

    private final SecurityContextService securityContextService;
    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

    private final SalaryTemplateRepository salaryTemplateRepository;
    private final SalaryTemplateMapper salaryTemplateMapper;
    private final SalaryTemplateDetailService salaryTemplateDetailService;
    private final CodeGenerator codeGenerator;

    public SalaryTemplateServiceImpl(SalaryTemplateRepository salaryTemplateRepository,
            SalaryTemplateMapper salaryTemplateMapper,
            SalaryTemplateDetailService salaryTemplateDetailService,
            CodeGenerator codeGenerator,
            SecurityContextService securityContextService) {
        this.securityContextService = securityContextService;
        this.salaryTemplateRepository = salaryTemplateRepository;
        this.salaryTemplateMapper = salaryTemplateMapper;
        this.salaryTemplateDetailService = salaryTemplateDetailService;
        this.codeGenerator = codeGenerator;
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

        CustomStringUtils.parsePositiveBigDecimal(request.getTotalAmount(),
                Messages.ERROR_SALARY_TEMPLATE_TOTAL_AMOUNT_INVALID);
        validateDetails(details);

        String companyCode = securityContextService.getCurrentCompanyCode();
        if (salaryTemplateRepository.existsOverlappingByNameAndCompanyCode(name, companyCode, effectiveFrom,
                effectiveTo)) {
            throw new ConflictException(Messages.ERROR_SALARY_TEMPLATE_NAME_EXISTS);
        }

        SalaryTemplate template = salaryTemplateMapper.toEntity(request);
        template.initializeCode(codeGenerator.nextCode(CodePrefixes.SALARY_TEMPLATE));
        template.setName(name);
        template.setCurrency(request.getCurrency() == null ? null : request.getCurrency().trim().toUpperCase());
        template.setTotalAmount(BigDecimal.ZERO);

        SalaryTemplate saved = salaryTemplateRepository.save(template);
        List<SalaryTemplateDetail> createdDetails = salaryTemplateDetailService.createSalaryTemplateDetails(details, saved);
        saved.setTotalAmount(calculateTemplateTotal(createdDetails));

        SalaryTemplate updated = salaryTemplateRepository.save(saved);
        return salaryTemplateMapper.toResponse(updated);
    }

    @Override
    public PagedResponse<SalaryTemplateListResponse> getSalaryTemplates(String name, String currency,
            LocalDate effectiveFrom, LocalDate effectiveTo, Integer page, Integer size, String sortBy, String sortDir) {
        if (effectiveFrom != null && effectiveTo != null && effectiveFrom.isAfter(effectiveTo)) {
            throw new BadRequestException(Messages.ERROR_SALARY_TEMPLATE_EFFECTIVE_DATES_INVALID);
        }

        String companyCode = securityContextService.getCurrentCompanyCode();
        Pageable pageable = PageableUtils.create(page, size, sortBy, sortDir);
        Page<SalaryTemplate> data = salaryTemplateRepository.searchByConditions(companyCode, name, currency, effectiveFrom,
                effectiveTo, pageable);
        return PageableUtils.mapPage(data, salaryTemplateMapper::toListResponse, Messages.SUCCESS);
    }

    @Override
    public PagedResponse<SelectionOptionResponse> getSalaryTemplateOptions(String name, Integer page, Integer size,
            String sortBy, String sortDir) {
        String companyCode = securityContextService.getCurrentCompanyCode();
        String normalizedName = CustomStringUtils.trimToNull(name);
        Pageable pageable = PageableUtils.create(page, size, sortBy, sortDir);

        Page<SalaryTemplate> data = salaryTemplateRepository.findOptionsByFilters(companyCode, normalizedName, pageable);
        return PageableUtils.mapPage(data, salaryTemplateMapper::toOptionResponse, Messages.SUCCESS);
    }

    @Override
    public List<SalaryTemplateDetailListResponse> getSalaryTemplateDetails(String salaryTemplateCode) {
        return salaryTemplateDetailService.getSalaryTemplateDetails(salaryTemplateCode);
    }

    private void validateDetails(List<SalaryTemplateDetailRequest> details) {
        Set<Integer> sequenceOrders = new HashSet<>();

        for (SalaryTemplateDetailRequest detail : details) {
            if (detail == null) {
                throw new BadRequestException(Messages.ERROR_SALARY_TEMPLATE_DETAILS_INVALID);
            }

            CustomStringUtils.parsePositiveBigDecimal(detail.getAmount(),
                    Messages.ERROR_SALARY_TEMPLATE_DETAIL_AMOUNT_INVALID);
            int sequenceOrder = parsePositiveInt(detail.getSequenceOrder(),
                    Messages.ERROR_SALARY_TEMPLATE_DETAIL_SEQUENCE_ORDER_INVALID);

            if (!sequenceOrders.add(sequenceOrder)) {
                throw new BadRequestException(Messages.ERROR_SALARY_TEMPLATE_DETAILS_INVALID);
            }
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

    private BigDecimal calculateTemplateTotal(List<SalaryTemplateDetail> details) {
        if (details == null || details.isEmpty()) {
            throw new BadRequestException(Messages.ERROR_SALARY_TEMPLATE_TOTAL_AMOUNT_INVALID);
        }

        Map<String, SalaryTemplateDetail> detailsBySalaryCode = new LinkedHashMap<>();
        for (SalaryTemplateDetail detail : details) {
            if (detail == null || detail.getSalary() == null || detail.getSalary().getCode() == null) {
                continue;
            }
            detailsBySalaryCode.put(detail.getSalary().getCode(), detail);
        }

        Map<String, BigDecimal> rawAmountsBySalaryCode = new HashMap<>();
        BigDecimal total = BigDecimal.ZERO;
        for (SalaryTemplateDetail detail : details) {
            if (detail == null) {
                continue;
            }
            BigDecimal rawAmount = resolveRawAmount(detail, detailsBySalaryCode, rawAmountsBySalaryCode, new HashSet<>());
            boolean isDeduct = detail.getSalary() != null && Boolean.TRUE.equals(detail.getSalary().getIsDeduct());
            total = total.add(isDeduct ? rawAmount.negate() : rawAmount);
        }

        if (total.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException(Messages.ERROR_SALARY_TEMPLATE_TOTAL_AMOUNT_INVALID);
        }
        return total.stripTrailingZeros();
    }

    private BigDecimal resolveRawAmount(
            SalaryTemplateDetail detail,
            Map<String, SalaryTemplateDetail> detailsBySalaryCode,
            Map<String, BigDecimal> rawAmountsBySalaryCode,
            Set<String> activePath) {
        String salaryCode = detail.getSalary() == null ? null : detail.getSalary().getCode();
        if (salaryCode != null && rawAmountsBySalaryCode.containsKey(salaryCode)) {
            return rawAmountsBySalaryCode.get(salaryCode);
        }
        if (salaryCode != null && !activePath.add(salaryCode)) {
            throw new BadRequestException(Messages.ERROR_SALARY_TEMPLATE_DETAILS_INVALID);
        }

        BigDecimal configuredAmount = CustomStringUtils.parsePositiveBigDecimal(detail.getAmount(),
                Messages.ERROR_SALARY_TEMPLATE_DETAIL_AMOUNT_INVALID);
        BigDecimal dependencyAmount = null;
        if (detail.getDependenceCode() != null && detail.getDependenceCode().getCode() != null) {
            SalaryTemplateDetail dependencyDetail = detailsBySalaryCode.get(detail.getDependenceCode().getCode());
            if (dependencyDetail == null) {
                throw new BadRequestException(Messages.ERROR_EMPLOYEE_SALARY_DETAIL_DEPENDENCE_CODE_INVALID);
            }
            dependencyAmount = resolveRawAmount(dependencyDetail, detailsBySalaryCode, rawAmountsBySalaryCode, activePath);
        }

        SalaryCalculateMethod calculateMethod = detail.getSalary() == null ? SalaryCalculateMethod.FIXED
                : detail.getSalary().getCalculateMethod();
        BigDecimal rawAmount = switch (calculateMethod) {
            case PERCENT -> calculatePercentAmount(configuredAmount, dependencyAmount);
            case DIVIDE -> calculateDivideAmount(configuredAmount, dependencyAmount);
            default -> configuredAmount;
        };

        if (salaryCode != null) {
            rawAmountsBySalaryCode.put(salaryCode, rawAmount);
            activePath.remove(salaryCode);
        }
        return rawAmount;
    }

    private BigDecimal calculatePercentAmount(BigDecimal configuredAmount, BigDecimal dependencyAmount) {
        if (dependencyAmount == null) {
            return configuredAmount;
        }
        return dependencyAmount.multiply(configuredAmount).divide(ONE_HUNDRED, 12, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateDivideAmount(BigDecimal configuredAmount, BigDecimal dependencyAmount) {
        if (dependencyAmount == null) {
            return configuredAmount;
        }
        return dependencyAmount.divide(configuredAmount, 12, RoundingMode.HALF_UP);
    }
}
