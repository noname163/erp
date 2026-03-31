package com.dat.erp.services.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dat.erp.constants.CodePrefixes;
import com.dat.erp.constants.Messages;
import com.dat.erp.dto.request.EmployeeSalaryRequest;
import com.dat.erp.dto.response.EmployeeSalaryListResponse;
import com.dat.erp.dto.response.PagedResponse;
import com.dat.erp.dto.response.SelectionOptionResponse;
import com.dat.erp.dto.response.EmployeeSalaryResponse;
import com.dat.erp.entities.Company;
import com.dat.erp.entities.EmployeeSalary;
import com.dat.erp.entities.PayrollResult;
import com.dat.erp.entities.UserProfile;
import com.dat.erp.exceptions.BadRequestException;
import com.dat.erp.exceptions.ConflictException;
import com.dat.erp.exceptions.ResourceNotFoundException;
import com.dat.erp.mapper.interfaces.EmployeeSalaryMapper;
import com.dat.erp.repositories.customrepositories.CompanyRepository;
import com.dat.erp.repositories.customrepositories.EmployeeSalaryRepository;
import com.dat.erp.repositories.customrepositories.UserProfileRepository;
import com.dat.erp.services.CodeGenerator;
import com.dat.erp.services.EmployeeSalaryDetailService;
import com.dat.erp.services.EmployeeSalaryService;
import com.dat.erp.services.SecurityContextService;
import com.dat.erp.services.base.AbstractAuditableService;
import com.dat.erp.utils.CompanySecretKeyCryptoUtils;

@Service
public class EmployeeSalaryServiceImpl extends AbstractAuditableService implements EmployeeSalaryService {

    private final EmployeeSalaryRepository employeeSalaryRepository;
    private final CompanyRepository companyRepository;
    private final UserProfileRepository userProfileRepository;
    private final EmployeeSalaryMapper employeeSalaryMapper;
    private final EmployeeSalaryDetailService employeeSalaryDetailService;

    public EmployeeSalaryServiceImpl(EmployeeSalaryRepository employeeSalaryRepository,
            CompanyRepository companyRepository,
            UserProfileRepository userProfileRepository,
            EmployeeSalaryMapper employeeSalaryMapper,
            CodeGenerator codeGenerator,
            SecurityContextService securityContextService,
            EmployeeSalaryDetailService employeeSalaryDetailService) {
        this.employeeSalaryRepository = employeeSalaryRepository;
        this.companyRepository = companyRepository;
        this.userProfileRepository = userProfileRepository;
        this.employeeSalaryMapper = employeeSalaryMapper;
        this.codeGenerator = codeGenerator;
        this.employeeSalaryDetailService = employeeSalaryDetailService;
        this.securityContextService = securityContextService;
    }

    @Override
    @Transactional
    public EmployeeSalaryResponse createEmployeeSalary(EmployeeSalaryRequest request) {
        if (request == null) {
            throw new BadRequestException("request is invalid");
        }

        String userProfileCode = request.getUserProfileCode().trim();

        LocalDate effectiveFrom = request.getEffectiveFrom();
        LocalDate effectiveTo = request.getEffectiveTo();
        if (effectiveFrom.isAfter(effectiveTo)) {
            throw new BadRequestException(Messages.ERROR_EMPLOYEE_SALARY_EFFECTIVE_DATES_INVALID);
        }

        BigDecimal totalAmount = parsePositiveBigDecimal(request.getTotalAmount(),
                Messages.ERROR_EMPLOYEE_SALARY_TOTAL_AMOUNT_INVALID);
        String currency = request.getCurrency().trim().toUpperCase();

        String companyCode = securityContextService.getCurrentUser().getAccount().getCompanyCode();
        if (companyCode == null || companyCode.isBlank()) {
            throw new BadRequestException(Messages.ERROR_CURRENT_USER_COMPANY_MISSING);
        }

        Company company = companyRepository.findByCode(companyCode)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(Messages.ERROR_COMPANY_NOT_FOUND_WITH_CODE, companyCode)));
        String companySecretKey = company.getSecretKey();
        if (companySecretKey == null || companySecretKey.isBlank()) {
            throw new BadRequestException(Messages.ERROR_EMPLOYEE_SALARY_COMPANY_SECRET_KEY_MISSING);
        }

        UserProfile userProfile = userProfileRepository.findByCode(userProfileCode)
                .orElseThrow(() -> new ResourceNotFoundException(Messages.ERROR_EMPLOYEE_SALARY_EMPLOYEE_NOT_FOUND));

        String employeeCompanyCode = userProfile.getAccount() == null ? null
                : userProfile.getAccount().getCompanyCode();
        if (employeeCompanyCode == null || employeeCompanyCode.isBlank() || !companyCode.equals(employeeCompanyCode)) {
            throw new BadRequestException(Messages.ERROR_EMPLOYEE_SALARY_EMPLOYEE_COMPANY_MISMATCH);
        }

        if (userProfile.getIsActive() == null || !userProfile.getIsActive()) {
            throw new BadRequestException(Messages.ERROR_EMPLOYEE_SALARY_EMPLOYEE_INACTIVE);
        }

        if (employeeSalaryRepository.existsOverlappingByUserProfileCodeAndCompanyCode(userProfileCode, companyCode,
                effectiveFrom, effectiveTo)) {
            throw new ConflictException(Messages.ERROR_EMPLOYEE_SALARY_PERIOD_OVERLAPS);
        }

        EmployeeSalary employeeSalary = employeeSalaryMapper.toEntity(request);
        employeeSalary.setUserProfile(userProfile);
        employeeSalary
                .setTotalAmount(CompanySecretKeyCryptoUtils.encrypt(totalAmount.toPlainString(), companySecretKey));
        employeeSalary.setCurrency(currency);

        generateCodeIfMissing(employeeSalary, CodePrefixes.EMPLOYEE_SALARY);
        applyInsertAudit(employeeSalary);

        EmployeeSalary saved = employeeSalaryRepository.save(employeeSalary);
        EmployeeSalaryResponse response = employeeSalaryMapper.toResponse(saved);
        response.setTotalAmount(totalAmount.toPlainString());
        response.setCurrency(currency);
        employeeSalaryDetailService.createEmployeeSalaryDetails(request.getSalaryDetails(), response.getCode());
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<EmployeeSalaryListResponse> getEmployeeSalaries(String employeeName, BigDecimal minAmount,
            BigDecimal maxAmount, LocalDate effectiveFrom, LocalDate effectiveTo, Integer page, Integer size,
            String sortBy, String sortDir) {
        if (effectiveFrom != null && effectiveTo != null && effectiveFrom.isAfter(effectiveTo)) {
            throw new BadRequestException(Messages.ERROR_EMPLOYEE_SALARY_EFFECTIVE_DATES_INVALID);
        }

        BigDecimal normalizedMinAmount = validateNonNegativeAmount(minAmount);
        BigDecimal normalizedMaxAmount = validateNonNegativeAmount(maxAmount);
        if (normalizedMinAmount != null && normalizedMaxAmount != null
                && normalizedMinAmount.compareTo(normalizedMaxAmount) > 0) {
            throw new BadRequestException(Messages.ERROR_EMPLOYEE_SALARY_AMOUNT_RANGE_INVALID);
        }

        String companyCode = resolveCurrentUserCompanyCode();
        Company company = companyRepository.findByCode(companyCode)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(Messages.ERROR_COMPANY_NOT_FOUND_WITH_CODE, companyCode)));

        String companySecretKey = company.getSecretKey();
        if (companySecretKey == null || companySecretKey.isBlank()) {
            throw new BadRequestException(Messages.ERROR_EMPLOYEE_SALARY_COMPANY_SECRET_KEY_MISSING);
        }

        List<EmployeeSalary> employeeSalaries = employeeSalaryRepository.searchByConditions(companyCode, employeeName,
                effectiveFrom, effectiveTo);

        List<EmployeeSalaryListResponse> mapped = employeeSalaries.stream()
                .map(salary -> toListResponse(salary, companySecretKey))
                .filter(item -> isAmountInRange(item.getTotalAmount(), normalizedMinAmount, normalizedMaxAmount))
                .sorted(resolveComparator(sortBy, sortDir))
                .toList();

        int pageNumber = page == null || page < 0 ? 0 : page;
        int pageSize = size == null || size <= 0 ? 20 : Math.min(size, 100);
        int totalElements = mapped.size();
        int fromIndex = Math.min(pageNumber * pageSize, totalElements);
        int toIndex = Math.min(fromIndex + pageSize, totalElements);
        List<EmployeeSalaryListResponse> pageContent = mapped.subList(fromIndex, toIndex);

        return PagedResponse.fromPage(
                new PageImpl<>(pageContent,
                        org.springframework.data.domain.PageRequest.of(pageNumber, pageSize),
                        totalElements),
                Messages.SUCCESS);
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

    private BigDecimal validateNonNegativeAmount(BigDecimal amount) {
        if (amount == null) {
            return null;
        }
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException(Messages.ERROR_EMPLOYEE_SALARY_AMOUNT_RANGE_INVALID);
        }
        return amount;
    }

    private EmployeeSalaryListResponse toListResponse(EmployeeSalary salary, String companySecretKey) {
        UserProfile userProfile = salary.getUserProfile();
        String employeeName = buildFullName(userProfile == null ? null : userProfile.getFirstName(),
                userProfile == null ? null : userProfile.getLastName());

        String decryptedAmount = CompanySecretKeyCryptoUtils.decrypt(salary.getTotalAmount(), companySecretKey);
        BigDecimal totalAmount = new BigDecimal(decryptedAmount);

        return new EmployeeSalaryListResponse(
                salary.getCode(),
                employeeName,
                salary.getEffectiveFrom(),
                salary.getEffectiveTo(),
                totalAmount.stripTrailingZeros().toPlainString(),
                salary.getCurrency());
    }

    private String buildFullName(String firstName, String lastName) {
        String fn = firstName == null ? "" : firstName.trim();
        String ln = lastName == null ? "" : lastName.trim();
        String fullName = (fn + " " + ln).trim();
        return fullName.isBlank() ? null : fullName;
    }

    private boolean isAmountInRange(String totalAmount, BigDecimal minAmount, BigDecimal maxAmount) {
        BigDecimal amount = new BigDecimal(totalAmount);
        if (minAmount != null && amount.compareTo(minAmount) < 0) {
            return false;
        }
        if (maxAmount != null && amount.compareTo(maxAmount) > 0) {
            return false;
        }
        return true;
    }

    private Comparator<EmployeeSalaryListResponse> resolveComparator(String sortBy, String sortDir) {
        String normalizedSortBy = sortBy == null ? "effectiveFrom" : sortBy.trim();
        boolean isDesc = sortDir == null || !"ASC".equalsIgnoreCase(sortDir.trim());

        Comparator<EmployeeSalaryListResponse> comparator = switch (normalizedSortBy) {
            case "employeeName" -> Comparator.comparing(EmployeeSalaryListResponse::getEmployeeName,
                    Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
            case "effectiveTo" -> Comparator.comparing(EmployeeSalaryListResponse::getEffectiveTo,
                    Comparator.nullsLast(LocalDate::compareTo));
            case "totalAmount" -> Comparator.comparing(
                    item -> new BigDecimal(item.getTotalAmount()),
                    Comparator.nullsLast(BigDecimal::compareTo));
            case "currency" -> Comparator.comparing(EmployeeSalaryListResponse::getCurrency,
                    Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
            case "salaryCode" -> Comparator.comparing(EmployeeSalaryListResponse::getSalaryCode,
                    Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
            default -> Comparator.comparing(EmployeeSalaryListResponse::getEffectiveFrom,
                    Comparator.nullsLast(LocalDate::compareTo));
        };

        return isDesc ? comparator.reversed() : comparator;
    }

    @Override
    public List<PayrollResult> employeeSalaryCalculation(List<Long> employeeCodes, LocalDate runDate) {
        // Get list active employeeSalary by call employee salary service
        // Call employee payroll policy to get map of pay roll policy for each employee of current month
        // Call employee daily work service to get map of daily work for each employee of current month      
        throw new UnsupportedOperationException("Unimplemented method 'employeeSalaryCalculation'");
    }
}
