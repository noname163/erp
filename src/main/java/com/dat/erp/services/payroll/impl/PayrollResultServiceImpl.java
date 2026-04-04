package com.dat.erp.services.payroll.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.dat.erp.constants.CodePrefixes;
import com.dat.erp.constants.DayType;
import com.dat.erp.constants.Messages;
import com.dat.erp.constants.PayrollRunStatus;
import com.dat.erp.constants.PayrollStatus;
import com.dat.erp.dto.response.PagedResponse;
import com.dat.erp.dto.response.PayrollResultListResponse;
import com.dat.erp.dto.response.salary.DailyWorkForSalaryResponse;
import com.dat.erp.entities.Company;
import com.dat.erp.entities.DailyWork;
import com.dat.erp.entities.EmployeeSalary;
import com.dat.erp.entities.PayrollPolicy;
import com.dat.erp.entities.PayrollResult;
import com.dat.erp.entities.PayrollRun;
import com.dat.erp.entities.UserProfile;
import com.dat.erp.exceptions.BadRequestException;
import com.dat.erp.exceptions.ResourceNotFoundException;
import com.dat.erp.repositories.customrepositories.CompanyRepository;
import com.dat.erp.repositories.customrepositories.DailyWorkRepository;
import com.dat.erp.repositories.customrepositories.EmployeeSalaryRepository;
import com.dat.erp.repositories.customrepositories.PayrollResultRepository;
import com.dat.erp.repositories.customrepositories.PayrollRunRepository;
import com.dat.erp.repositories.projections.PayrollResultListProjection;
import com.dat.erp.services.CalendarDateService;
import com.dat.erp.services.CodeGenerator;
import com.dat.erp.services.EmployeePayrollPolicyService;
import com.dat.erp.services.EmployeeSalaryService;
import com.dat.erp.services.SecurityContextService;
import com.dat.erp.services.UserProfileService;
import com.dat.erp.services.base.AbstractAuditableService;
import com.dat.erp.services.payroll.PayrollResultService;
import com.dat.erp.utils.CompanySecretKeyCryptoUtils;
import com.dat.erp.utils.CustomStringUtils;
import com.dat.erp.utils.PageableUtils;

@Service
public class PayrollResultServiceImpl extends AbstractAuditableService implements PayrollResultService {

    private static final Set<DayType> WORKING_DAY_TYPES = EnumSet.of(
            DayType.NORMAL,
            DayType.HOLIDAY_WORK,
            DayType.WEEKEND_WORK);

    private static final Set<DayType> LEAVE_DAY_TYPES = EnumSet.of(
            DayType.PTO_PAID,
            DayType.PTO_UNPAID,
            DayType.UNPAID_LEAVE,
            DayType.COMPANY_DAY_OFF);

    private final UserProfileService userProfileService;
    private final EmployeePayrollPolicyService employeePayrollPolicyService;
    private final CalendarDateService calendarDateService;
    private final EmployeeSalaryRepository employeeSalaryRepository;
    private final DailyWorkRepository dailyWorkRepository;
    private final PayrollRunRepository payrollRunRepository;
    private final PayrollResultRepository payrollResultRepository;
    private final CompanyRepository companyRepository;
    private final EmployeeSalaryService employeeSalaryService;

    public PayrollResultServiceImpl(
            UserProfileService userProfileService,
            EmployeePayrollPolicyService employeePayrollPolicyService,
            CalendarDateService calendarDateService,
            EmployeeSalaryRepository employeeSalaryRepository,
            DailyWorkRepository dailyWorkRepository,
            PayrollRunRepository payrollRunRepository,
            PayrollResultRepository payrollResultRepository,
            CompanyRepository companyRepository,
            EmployeeSalaryService employeeSalaryService,
            CodeGenerator codeGenerator,
            SecurityContextService securityContextService) {
        this.userProfileService = userProfileService;
        this.employeePayrollPolicyService = employeePayrollPolicyService;
        this.calendarDateService = calendarDateService;
        this.employeeSalaryRepository = employeeSalaryRepository;
        this.dailyWorkRepository = dailyWorkRepository;
        this.payrollRunRepository = payrollRunRepository;
        this.payrollResultRepository = payrollResultRepository;
        this.companyRepository = companyRepository;
        this.employeeSalaryService = employeeSalaryService;
        this.codeGenerator = codeGenerator;
        this.securityContextService = securityContextService;
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<PayrollResultListResponse> getPayrollResults(
            String payrollRunCode,
            LocalDate createdDate,
            PayrollStatus sourceType,
            String employeeCode,
            Integer page,
            Integer size,
            String sortBy,
            String sortDir) {
        String companyCode = resolveCompanyCode();
        String companySecretKey = resolveCompanySecretKey(companyCode);
        String normalizedPayrollRunCode = CustomStringUtils.normalizeCode(payrollRunCode);
        if (normalizedPayrollRunCode == null) {
            throw new BadRequestException(Messages.ERROR_PAYROLL_RUN_CODE_INVALID);
        }
        LocalDate targetDate = createdDate == null ? LocalDate.now() : createdDate;
        LocalDateTime createdAtFrom = targetDate.atStartOfDay();
        LocalDateTime createdAtTo = targetDate.plusDays(1).atStartOfDay();
        Pageable pageable = PageableUtils.create(
                page,
                size,
                resolveSortBy(sortBy),
                sortDir == null || sortDir.isBlank() ? "DESC" : sortDir);

        Page<PayrollResultListProjection> payrollResults = payrollResultRepository.searchByConditions(
                companyCode,
                normalizedPayrollRunCode,
                createdAtFrom,
                createdAtTo,
                sourceType,
                CustomStringUtils.normalizeCode(employeeCode),
                pageable);
        return PageableUtils.mapPage(payrollResults, projection -> toListResponse(projection, companySecretKey), Messages.SUCCESS);
    }

    @Override
    @Transactional
    public void generatePayrollResult(PayrollRun payrollRun) {
        String companyCode = resolveCurrentUserCompanyCode();
        if (companyCode == null || companyCode.isBlank() || "SYSTEM".equals(companyCode)) {
            throw new BadRequestException(Messages.ERROR_CURRENT_USER_COMPANY_MISSING);
        }

        YearMonth runMonth = resolveRunMonth(payrollRun);
        LocalDate runDate = runMonth.atEndOfMonth();
        List<String> activeEmployeeCodes = userProfileService.getActiveUserProfileCodesOfCurrentCompany();
        if (activeEmployeeCodes == null || activeEmployeeCodes.isEmpty()) {
            finalizePayrollRun(payrollRun);
            return;
        }

        Map<String, PayrollPolicy> payrollPoliciesByEmployeeCode = employeePayrollPolicyService
                .getCompanyPoliciesByEmployeeCodesAndDate(activeEmployeeCodes, runDate);
        List<EmployeeSalary> activeEmployeeSalaries = employeeSalaryRepository
                .findActiveByCompanyCodeAndUserProfileCodesAndDate(companyCode, activeEmployeeCodes, runDate);

        int totalWorkingDays = calculateTotalWorkingDays(
                calendarDateService.getCalendarDateTotalsByCompanyCodeAndMonth(companyCode, runMonth));
        Map<String, Integer> leaveQuantitiesByEmployeeCode = loadLeaveQuantitiesByEmployeeCode(
                companyCode,
                activeEmployeeCodes,
                runMonth);
        Map<String, EmployeeSalary> salaryByEmployeeCode = mapSalariesByEmployeeCode(activeEmployeeSalaries);

        List<PayrollResult> payrollResults = new ArrayList<>();
        for (String employeeCode : activeEmployeeCodes) {
            EmployeeSalary employeeSalary = salaryByEmployeeCode.get(employeeCode);
            PayrollPolicy payrollPolicy = payrollPoliciesByEmployeeCode.get(employeeCode);
            if (employeeSalary == null || payrollPolicy == null) {
                continue;
            }

            payrollResults.add(buildPayrollResult(
                    payrollRun,
                    employeeSalary,
                    payrollPolicy,
                    totalWorkingDays,
                    leaveQuantitiesByEmployeeCode.getOrDefault(employeeCode, 0)));
        }

        if (!payrollResults.isEmpty()) {
            List<PayrollResult> savedPayrollResults = payrollResultRepository.saveAll(payrollResults);
            scheduleEmployeeSalaryCalculation(activeEmployeeCodes, savedPayrollResults, runDate);
        }

        finalizePayrollRun(payrollRun);
    }

    private void finalizePayrollRun(PayrollRun payrollRun) {
        payrollRun.setStatus(PayrollRunStatus.CALCULATED);
        applyUpdateAudit(payrollRun);
        payrollRunRepository.save(payrollRun);
    }

    private void scheduleEmployeeSalaryCalculation(
            List<String> employeeCodes,
            List<PayrollResult> payrollResults,
            LocalDate runDate) {
        List<String> employeeCodesSnapshot = List.copyOf(employeeCodes);
        List<PayrollResult> payrollResultsSnapshot = List.copyOf(payrollResults);
        Runnable task = () -> employeeSalaryService.employeeSalaryCalculation(
                employeeCodesSnapshot,
                payrollResultsSnapshot,
                runDate);

        if (TransactionSynchronizationManager.isSynchronizationActive()
                && TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    task.run();
                }
            });
            return;
        }

        task.run();
    }

    private YearMonth resolveRunMonth(PayrollRun payrollRun) {
        if (payrollRun == null || payrollRun.getPeriod() == null || payrollRun.getPeriod().isBlank()) {
            throw new BadRequestException(Messages.ERROR_PAYROLL_MONTH_INVALID);
        }

        try {
            return YearMonth.parse(payrollRun.getPeriod().trim());
        } catch (DateTimeParseException ex) {
            throw new BadRequestException(Messages.ERROR_PAYROLL_MONTH_INVALID);
        }
    }

    private int calculateTotalWorkingDays(Map<DayType, Integer> totalsByDayType) {
        if (totalsByDayType == null || totalsByDayType.isEmpty()) {
            return 0;
        }

        int totalWorkingDays = 0;
        for (Map.Entry<DayType, Integer> entry : totalsByDayType.entrySet()) {
            if (WORKING_DAY_TYPES.contains(entry.getKey())) {
                totalWorkingDays += entry.getValue() == null ? 0 : entry.getValue();
            }
        }
        return totalWorkingDays;
    }

    private Map<String, Integer> loadLeaveQuantitiesByEmployeeCode(
            String companyCode,
            List<String> employeeCodes,
            YearMonth runMonth) {
        LocalDate fromDate = runMonth.atDay(1);
        LocalDate toDate = runMonth.atEndOfMonth();
        List<DailyWork> dailyWorks = dailyWorkRepository.findAllForSalaryByCompanyCodeAndEmployeeCodesAndWorkingDateBetween(
                companyCode,
                employeeCodes,
                fromDate,
                toDate);

        Map<String, Integer> leaveQuantitiesByEmployeeCode = new LinkedHashMap<>();
        for (DailyWork dailyWork : dailyWorks) {
            UserProfile userProfile = dailyWork.getUserProfile();
            if (userProfile == null || userProfile.getCode() == null || dailyWork.getWorkType() == null) {
                continue;
            }
            if (!LEAVE_DAY_TYPES.contains(dailyWork.getWorkType())) {
                continue;
            }

            leaveQuantitiesByEmployeeCode.merge(
                    userProfile.getCode(),
                    toDailyWorkQuantity(dailyWork),
                    Integer::sum);
        }
        return leaveQuantitiesByEmployeeCode;
    }

    private Map<String, EmployeeSalary> mapSalariesByEmployeeCode(List<EmployeeSalary> employeeSalaries) {
        Map<String, EmployeeSalary> salaryByEmployeeCode = new LinkedHashMap<>();
        for (EmployeeSalary employeeSalary : employeeSalaries) {
            UserProfile userProfile = employeeSalary.getUserProfile();
            if (userProfile == null || userProfile.getCode() == null) {
                continue;
            }
            salaryByEmployeeCode.putIfAbsent(userProfile.getCode(), employeeSalary);
        }
        return salaryByEmployeeCode;
    }

    private PayrollResult buildPayrollResult(
            PayrollRun payrollRun,
            EmployeeSalary employeeSalary,
            PayrollPolicy payrollPolicy,
            int totalWorkingDays,
            int leaveQuantity) {
        int standardQuantityPerDay = payrollPolicy.getStandardQuantityPerDay() == null
                ? 0
                : payrollPolicy.getStandardQuantityPerDay();
        int expectedQuantity = standardQuantityPerDay * totalWorkingDays;

        PayrollResult payrollResult = PayrollResult.builder()
                .payrollRun(payrollRun)
                .employeeSalary(employeeSalary)
                .expectedAmount(employeeSalary.getTotalAmount())
                .currency(employeeSalary.getCurrency())
                .expectedQuantity(expectedQuantity)
                .unit(payrollPolicy.getUnit())
                .sourceType(PayrollStatus.RUNNING)
                .isRetro(false)
                .retroReason(null)
                .build();
        generateCodeIfMissing(payrollResult, CodePrefixes.PAYROLL_RESULT);
        applyInsertAudit(payrollResult);
        return payrollResult;
    }

    private int toDailyWorkQuantity(DailyWork dailyWork) {
        return new DailyWorkForSalaryResponse(
                dailyWork.getWorkType(),
                dailyWork.getHoursWorked() == null ? 0 : dailyWork.getHoursWorked().intValue())
                .getTotalWorkHours();
    }

    private String resolveCompanyCode() {
        String companyCode = resolveCurrentUserCompanyCode();
        if (companyCode == null || companyCode.isBlank() || "SYSTEM".equals(companyCode)) {
            throw new BadRequestException(Messages.ERROR_CURRENT_USER_COMPANY_MISSING);
        }
        return companyCode;
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

    private String resolveSortBy(String sortBy) {
        if (sortBy == null || sortBy.isBlank()) {
            return "createdAt";
        }

        return switch (sortBy.trim()) {
            case "payrollRunCode" -> "payrollRun.code";
            case "salaryName" -> "employeeSalary.salaryTemplate.name";
            case "employeeName" -> "employeeSalary.userProfile.firstName";
            case "expectedAmount" -> "expectedAmount";
            case "actualAmount" -> "actualAmount";
            case "currency" -> "currency";
            case "expectedQuantity" -> "expectedQuantity";
            case "actualQuantity" -> "actualQuantity";
            case "unitName" -> "unit.name";
            case "sourceType" -> "sourceType";
            case "isRetro" -> "isRetro";
            case "retroReason" -> "retroReason";
            case "createdDate", "createdAt" -> "createdAt";
            default -> "createdAt";
        };
    }

    private PayrollResultListResponse toListResponse(PayrollResultListProjection projection, String companySecretKey) {
        return new PayrollResultListResponse(
                normalizeText(projection.getPayrollRunCode()),
                normalizeText(projection.getSalaryName()),
                decryptExpectedAmount(projection.getExpectedAmount(), companySecretKey),
                normalizeText(projection.getEmployeeName()),
                normalizeText(projection.getActualAmount()),
                normalizeText(projection.getCurrency()),
                projection.getExpectedQuantity(),
                projection.getActualQuantity(),
                normalizeText(projection.getUnitName()),
                projection.getSourceType(),
                projection.getIsRetro(),
                normalizeText(projection.getRetroReason()),
                projection.getPeriod(),
                projection.getCreatedAt(),
                projection.getEmployeeCode()
            );
    }

    private String decryptExpectedAmount(String encryptedAmount, String companySecretKey) {
        String decrypted = CompanySecretKeyCryptoUtils.decrypt(encryptedAmount, companySecretKey);
        return normalizeText(decrypted);
    }

    private String normalizeText(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isBlank() ? null : trimmed;
    }
}
