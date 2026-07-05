package com.dat.erp.services.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.data.domain.PageImpl;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dat.erp.constants.CodePrefixes;
import com.dat.erp.constants.DayType;
import com.dat.erp.constants.Messages;
import com.dat.erp.constants.PayrollResultCalcBasis;
import com.dat.erp.dto.request.EmployeeSalaryRequest;
import com.dat.erp.dto.response.EmployeeSalaryListResponse;
import com.dat.erp.dto.response.EmployeeSalaryResponse;
import com.dat.erp.dto.response.PagedResponse;
import com.dat.erp.dto.response.salary.MonthlySalaryDetailAuditResponse;
import com.dat.erp.dto.response.salary.MonthlySalaryCalculationResponse;
import com.dat.erp.entities.Company;
import com.dat.erp.entities.EmployeeSalary;
import com.dat.erp.entities.PayrollResult;
import com.dat.erp.entities.PayrollResultDetail;
import com.dat.erp.entities.UserProfile;
import com.dat.erp.exceptions.BadRequestException;
import com.dat.erp.exceptions.ConflictException;
import com.dat.erp.exceptions.ResourceNotFoundException;
import com.dat.erp.mapper.interfaces.EmployeeSalaryMapper;
import com.dat.erp.repositories.customrepositories.CompanyRepository;
import com.dat.erp.repositories.customrepositories.EmployeeSalaryRepository;
import com.dat.erp.repositories.customrepositories.PayrollResultDetailRepository;
import com.dat.erp.repositories.customrepositories.PayrollResultRepository;
import com.dat.erp.repositories.customrepositories.UserProfileRepository;
import com.dat.erp.services.CodeGenerator;
import com.dat.erp.services.EmployeeSalaryDetailService;
import com.dat.erp.services.EmployeeSalaryService;
import com.dat.erp.services.MonthlySalaryCalculationService;
import com.dat.erp.services.SecurityContextService;
import com.dat.erp.services.base.AbstractAuditableService;
import com.dat.erp.utils.CompanySecretKeyCryptoUtils;
import com.dat.erp.utils.CustomStringUtils;

@Service
public class EmployeeSalaryServiceImpl extends AbstractAuditableService implements EmployeeSalaryService {

    private final EmployeeSalaryRepository employeeSalaryRepository;
    private final CompanyRepository companyRepository;
    private final UserProfileRepository userProfileRepository;
    private final EmployeeSalaryMapper employeeSalaryMapper;
    private final EmployeeSalaryDetailService employeeSalaryDetailService;
    private final MonthlySalaryCalculationService monthlySalaryCalculationService;
    private final PayrollResultRepository payrollResultRepository;
    private final PayrollResultDetailRepository payrollResultDetailRepository;

    public EmployeeSalaryServiceImpl(EmployeeSalaryRepository employeeSalaryRepository,
            CompanyRepository companyRepository,
            UserProfileRepository userProfileRepository,
            EmployeeSalaryMapper employeeSalaryMapper,
            CodeGenerator codeGenerator,
            SecurityContextService securityContextService,
            EmployeeSalaryDetailService employeeSalaryDetailService,
            MonthlySalaryCalculationService monthlySalaryCalculationService,
            PayrollResultRepository payrollResultRepository,
            PayrollResultDetailRepository payrollResultDetailRepository) {
        this.employeeSalaryRepository = employeeSalaryRepository;
        this.companyRepository = companyRepository;
        this.userProfileRepository = userProfileRepository;
        this.employeeSalaryMapper = employeeSalaryMapper;
        this.codeGenerator = codeGenerator;
        this.employeeSalaryDetailService = employeeSalaryDetailService;
        this.monthlySalaryCalculationService = monthlySalaryCalculationService;
        this.payrollResultRepository = payrollResultRepository;
        this.payrollResultDetailRepository = payrollResultDetailRepository;
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

        BigDecimal totalAmount = CustomStringUtils.parsePositiveBigDecimal(request.getTotalAmount(),
                Messages.ERROR_EMPLOYEE_SALARY_TOTAL_AMOUNT_INVALID);
        String currency = request.getCurrency().trim().toUpperCase();

        String companyCode = requireCurrentUserCompanyCode();

        String companySecretKey = resolveCompanySecretKey(companyCode);

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

        String companyCode = requireCurrentUserCompanyCode();
        String companySecretKey = resolveCompanySecretKey(companyCode);

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
                salary.getCurrency(),
                salary.getSalaryBasisType());
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
    @Async("payrollCalculationTaskExecutor")
    @Transactional
    public void employeeSalaryCalculation(String companyCode, List<String> employeeCodes, List<PayrollResult> payrollResults,
            LocalDate runDate) {
        if (runDate == null) {
            throw new BadRequestException(Messages.ERROR_PAYROLL_MONTH_INVALID);
        }
        if (employeeCodes == null || employeeCodes.isEmpty() || payrollResults == null || payrollResults.isEmpty()) {
            return;
        }
        if (companyCode == null || companyCode.isBlank() || "SYSTEM".equals(companyCode)) {
            throw new BadRequestException(Messages.ERROR_CURRENT_USER_COMPANY_MISSING);
        }

        YearMonth runMonth = YearMonth.from(runDate);
        Map<String, PayrollResult> payrollResultsByEmployeeCode = new LinkedHashMap<>();
        String companySecretKey = resolveCompanySecretKey(companyCode);
        for (PayrollResult payrollResult : payrollResults) {
            if (payrollResult == null || payrollResult.getEmployeeSalary() == null
                    || payrollResult.getEmployeeSalary().getUserProfile() == null) {
                continue;
            }

            UserProfile userProfile = payrollResult.getEmployeeSalary().getUserProfile();
            String employeeCode = CustomStringUtils.normalizeCode(userProfile.getCode());
            if (employeeCode != null) {
                payrollResultsByEmployeeCode.putIfAbsent(employeeCode, payrollResult);
            }
        }

        List<PayrollResult> updatedPayrollResults = new ArrayList<>();
        for (String employeeCode : employeeCodes.stream()
                .map(CustomStringUtils::normalizeCode)
                .filter(Objects::nonNull)
                .distinct()
                .toList()) {
            PayrollResult payrollResult = payrollResultsByEmployeeCode.get(employeeCode);
            if (payrollResult == null) {
                continue;
            }

            MonthlySalaryCalculationResponse calculation = monthlySalaryCalculationService
                    .calculateEmployeeMonthlySalary(employeeCode, runMonth);

            payrollResult.setActualAmount(CompanySecretKeyCryptoUtils.encrypt(
                    calculation.getFinalSalary().toPlainString(), companySecretKey));
            if (calculation.getActualWorkingHourPerMonth() != null) {
                payrollResult.setActualQuantity(calculation.getActualWorkingHourPerMonth().intValue());
            }
            applyUpdateAudit(payrollResult);
            updatedPayrollResults.add(payrollResult);
            replacePayrollResultDetails(payrollResult, calculation);
        }

        if (!updatedPayrollResults.isEmpty()) {
            payrollResultRepository.saveAll(updatedPayrollResults);
        }
    }

    private void replacePayrollResultDetails(PayrollResult payrollResult, MonthlySalaryCalculationResponse calculation) {
        if (payrollResult.getCode() != null) {
            List<PayrollResultDetail> existingDetails = payrollResultDetailRepository
                    .findByPayrollResult_CodeAndIsDeletedFalse(payrollResult.getCode());
            existingDetails.forEach(detail -> {
                detail.setIsDeleted(true);
                applyUpdateAudit(detail);
            });
            if (!existingDetails.isEmpty()) {
                payrollResultDetailRepository.saveAll(existingDetails);
            }
        }

        List<PayrollResultDetail> details = new ArrayList<>();
        details.add(buildSummaryDetail(payrollResult, calculation));
        if (calculation.getPaidLeaveHours() != null && calculation.getPaidLeaveHours().compareTo(BigDecimal.ZERO) > 0) {
            details.add(buildLeaveDetail(payrollResult, PayrollResultCalcBasis.PAID_LEAVE, calculation.getPaidLeaveHours(),
                    calculation.getStandardMoneyPerHour(), BigDecimal.ZERO, "Paid leave counted as paid working time"));
        }
        if (calculation.getUnpaidLeaveHours() != null && calculation.getUnpaidLeaveHours().compareTo(BigDecimal.ZERO) > 0) {
            details.add(buildLeaveDetail(payrollResult, PayrollResultCalcBasis.UNPAID_LEAVE,
                    calculation.getUnpaidLeaveHours(), calculation.getStandardMoneyPerHour(),
                    calculation.getUnpaidLeaveHours().multiply(calculation.getStandardMoneyPerHour()).negate(),
                    "Unpaid leave excluded from paid working time"));
        }
        if (calculation.getLateEarlyDeductionHours() != null
                && calculation.getLateEarlyDeductionHours().compareTo(BigDecimal.ZERO) > 0) {
            details.add(buildLeaveDetail(payrollResult, PayrollResultCalcBasis.LATE_EARLY_DEDUCTION,
                    calculation.getLateEarlyDeductionHours(), calculation.getStandardMoneyPerHour(),
                    calculation.getLateEarlyDeductionHours().multiply(calculation.getStandardMoneyPerHour()).negate(),
                    "Late arrival and early leave deducted from paid working time"));
        }
        for (MonthlySalaryDetailAuditResponse audit : calculation.getAuditTrail()) {
            details.add(buildAuditDetail(payrollResult, audit));
        }
        payrollResultDetailRepository.saveAll(details);
    }

    private PayrollResultDetail buildSummaryDetail(PayrollResult payrollResult, MonthlySalaryCalculationResponse calculation) {
        PayrollResultDetail detail = new PayrollResultDetail();
        detail.setPayrollResult(payrollResult);
        detail.setCalcBasis(PayrollResultCalcBasis.HOURS);
        detail.setBasisHours(calculation.getExpectedWorkingHourPerMonth());
        detail.setPaidDays(calculation.getActualWorkingHourPerMonth());
        detail.setRatePerDay(calculation.getStandardMoneyPerHour());
        detail.setAmount(calculation.getFinalSalary());
        detail.setFormulaNote("basis=" + calculation.getSalaryBasisType() + ", expected="
                + calculation.getExpectedBasisValue() + ", actual=" + calculation.getActualBasisValue()
                + ", unit=" + calculation.getBasisUnit());
        preparePayrollResultDetail(detail);
        return detail;
    }

    private PayrollResultDetail buildLeaveDetail(PayrollResult payrollResult, PayrollResultCalcBasis calcBasis,
            BigDecimal hours, BigDecimal rate, BigDecimal amount, String formulaNote) {
        PayrollResultDetail detail = new PayrollResultDetail();
        detail.setPayrollResult(payrollResult);
        detail.setCalcBasis(calcBasis);
        detail.setBasisHours(hours);
        detail.setRatePerDay(rate);
        detail.setAmount(amount);
        detail.setFormulaNote(formulaNote);
        preparePayrollResultDetail(detail);
        return detail;
    }

    private PayrollResultDetail buildAuditDetail(PayrollResult payrollResult, MonthlySalaryDetailAuditResponse audit) {
        PayrollResultDetail detail = new PayrollResultDetail();
        detail.setPayrollResult(payrollResult);
        detail.setCalcBasis(resolveCalcBasis(audit.getDayType()));
        detail.setBasisHours(audit.getBaseAmount());
        detail.setRatePerDay(audit.getConfiguredAmount());
        detail.setMultiplierApplied(audit.getDependencyAmount());
        detail.setAmount(audit.getResult());
        detail.setFormulaNote("salaryCode=" + audit.getSalaryCode() + ", method=" + audit.getCalculateMethod()
                + ", dependency=" + audit.getDependenceCode());
        preparePayrollResultDetail(detail);
        return detail;
    }

    private PayrollResultCalcBasis resolveCalcBasis(DayType dayType) {
        if (dayType == DayType.HOLIDAY_WORK) {
            return PayrollResultCalcBasis.HOLIDAY_WORK;
        }
        if (dayType == DayType.WEEKEND_WORK) {
            return PayrollResultCalcBasis.WEEKEND_WORK;
        }
        return PayrollResultCalcBasis.SALARY_COMPONENT;
    }

    private void preparePayrollResultDetail(PayrollResultDetail detail) {
        detail.setCompanyCode(payrollResultCompanyCode(detail));
        generateCodeIfMissing(detail, CodePrefixes.PAYROLL_RESULT_DETAIL);
        applyInsertAudit(detail);
    }

    private String payrollResultCompanyCode(PayrollResultDetail detail) {
        return detail.getPayrollResult() == null ? null : detail.getPayrollResult().getCompanyCode();
    }

    private String resolveCompanySecretKey(String companyCode) {
        Company company = companyRepository.findByCode(companyCode)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(Messages.ERROR_COMPANY_NOT_FOUND_WITH_CODE, companyCode)));

        String companySecretKey = company.getSecretKey();
        if (companySecretKey == null || companySecretKey.isBlank()) {
            throw new BadRequestException(Messages.ERROR_EMPLOYEE_SALARY_COMPANY_SECRET_KEY_MISSING);
        }
        return companySecretKey;
    }
}
