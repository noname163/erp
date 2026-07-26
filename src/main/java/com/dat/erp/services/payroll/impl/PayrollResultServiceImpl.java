package com.dat.erp.services.payroll.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.dat.erp.constants.DayType;
import com.dat.erp.constants.Messages;
import com.dat.erp.constants.PayrollRerunMode;
import com.dat.erp.constants.PayrollStatus;
import com.dat.erp.dto.request.PayrollRerunRequest;
import com.dat.erp.dto.response.PagedResponse;
import com.dat.erp.dto.response.PayrollResultDetailResponse;
import com.dat.erp.dto.response.PayrollResultListResponse;
import com.dat.erp.dto.response.salary.DailyWorkForSalaryResponse;
import com.dat.erp.dto.response.salary.MonthlySalaryCalculationResponse;
import com.dat.erp.entities.Company;
import com.dat.erp.entities.DailyWork;
import com.dat.erp.entities.EmployeeSalary;
import com.dat.erp.entities.PayrollPolicy;
import com.dat.erp.entities.PayrollResult;
import com.dat.erp.entities.PayrollResultDetail;
import com.dat.erp.entities.PayrollRun;
import com.dat.erp.entities.UserProfile;
import com.dat.erp.exceptions.BadRequestException;
import com.dat.erp.exceptions.ResourceNotFoundException;
import com.dat.erp.mapper.interfaces.PayrollResultMapper;
import com.dat.erp.repositories.customrepositories.CompanyRepository;
import com.dat.erp.repositories.customrepositories.DailyWorkRepository;
import com.dat.erp.repositories.customrepositories.EmployeeSalaryRepository;
import com.dat.erp.repositories.customrepositories.PayrollResultDetailRepository;
import com.dat.erp.repositories.customrepositories.PayrollResultRepository;
import com.dat.erp.repositories.projections.PayrollResultListProjection;
import com.dat.erp.services.CalendarDateService;
import com.dat.erp.services.EmployeePayrollPolicyService;
import com.dat.erp.services.EmployeeSalaryService;
import com.dat.erp.services.SecurityContextService;
import com.dat.erp.services.UserProfileService;
import com.dat.erp.services.payroll.PayrollResultService;
import com.dat.erp.services.payroll.PayrollRunService;
import com.dat.erp.utils.CompanySecretKeyCryptoUtils;
import com.dat.erp.utils.CustomStringUtils;
import com.dat.erp.utils.PageableUtils;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class PayrollResultServiceImpl implements PayrollResultService {

    private final SecurityContextService securityContextService;

    private static final Logger log = LoggerFactory.getLogger(PayrollResultServiceImpl.class);

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
    private final PayrollRunService payrollRunService;
    private final PayrollResultRepository payrollResultRepository;
    private final PayrollResultDetailRepository payrollResultDetailRepository;
    private final CompanyRepository companyRepository;
    private final EmployeeSalaryService employeeSalaryService;
    private final PayrollResultMapper payrollResultMapper;

    @Override
    @Transactional(readOnly = true)
    public List<PayrollResultDetailResponse> getPayrollResultDetails(String payrollResultCode) {
        String companyCode = securityContextService.getCurrentCompanyCode();
        String normalizedPayrollResultCode = CustomStringUtils.normalizeCode(payrollResultCode);
        if (normalizedPayrollResultCode == null) {
            throw new BadRequestException(Messages.ERROR_PAYROLL_RESULT_CODE_INVALID);
        }
        payrollResultRepository.findByCodeAndCompanyCodeAndIsDeletedFalse(normalizedPayrollResultCode, companyCode)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(Messages.ERROR_PAYROLL_RESULT_NOT_FOUND, normalizedPayrollResultCode)));
        return payrollResultDetailRepository.findByPayrollResult_CodeAndIsDeletedFalse(normalizedPayrollResultCode)
                .stream()
                .map(payrollResultMapper::toDetailResponse)
                .toList();
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
        String companyCode = securityContextService.getCurrentCompanyCode();
        String companySecretKey = resolveCompanySecretKey(companyCode);
        String normalizedPayrollRunCode = CustomStringUtils.normalizeCode(payrollRunCode);
        if (normalizedPayrollRunCode == null) {
            throw new BadRequestException(Messages.ERROR_PAYROLL_RUN_CODE_INVALID);
        }
        LocalDate targetDate = createdDate == null ? LocalDate.now() : createdDate;
        LocalDateTime createdAtFrom = targetDate.atStartOfDay();
        LocalDateTime createdAtTo = targetDate.plusDays(1).atStartOfDay();
        String scopedEmployeeCode = CustomStringUtils.resolveScopedEmployeeCode(
                securityContextService.getCurrentUser(),
                employeeCode);
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
                scopedEmployeeCode,
                pageable);
        return PageableUtils.mapPage(payrollResults,
                projection -> payrollResultMapper.toListResponse(projection, companySecretKey),
                Messages.SUCCESS);
    }

    @Override
    @Transactional
    public void generatePayrollResult(PayrollRun payrollRun) {
        String companyCode = securityContextService.getCurrentCompanyCode();

        YearMonth runMonth = payrollRun.getPeriod();
        LocalDate runDate = runMonth.atEndOfMonth();
        String payrollRunCode = payrollRun == null ? null : payrollRun.getCode();
        log.info("PAYROLL_RESULT action=GENERATE_STARTED companyCode={} payrollRunCode={} period={} runDate={}",
                companyCode, payrollRunCode, runMonth, runDate);

        List<String> activeEmployeeCodes = userProfileService.getActiveUserProfileCodesOfCurrentCompany();
        if (activeEmployeeCodes == null || activeEmployeeCodes.isEmpty()) {
            log.warn(
                    "PAYROLL_RESULT action=GENERATE_FINISHED result=FAILED reason=NO_ACTIVE_EMPLOYEES companyCode={} payrollRunCode={} period={}",
                    companyCode, payrollRunCode, runMonth);
            payrollRunService.finalizePayrollRun(payrollRunCode, false);
            return;
        }
        log.info(
                "PAYROLL_RESULT action=ACTIVE_EMPLOYEES_LOADED companyCode={} payrollRunCode={} period={} employeeCount={}",
                companyCode, payrollRunCode, runMonth, activeEmployeeCodes.size());

        Map<String, PayrollPolicy> payrollPoliciesByEmployeeCode = employeePayrollPolicyService
                .getCompanyPoliciesByEmployeeCodesAndDate(activeEmployeeCodes, runDate);
        List<EmployeeSalary> activeEmployeeSalaries = employeeSalaryRepository
                .findActiveByCompanyCodeAndUserProfileCodesAndDate(companyCode, activeEmployeeCodes, runDate);
        log.info(
                "PAYROLL_RESULT action=INPUTS_LOADED companyCode={} payrollRunCode={} period={} policyCount={} salaryCount={}",
                companyCode,
                payrollRunCode,
                runMonth,
                payrollPoliciesByEmployeeCode == null ? 0 : payrollPoliciesByEmployeeCode.size(),
                activeEmployeeSalaries == null ? 0 : activeEmployeeSalaries.size());

        int totalWorkingDays = calculateTotalWorkingDays(
                calendarDateService.getCalendarDateTotalsByCompanyCodeAndMonth(companyCode, runMonth));
        Map<String, Integer> leaveQuantitiesByEmployeeCode = loadLeaveQuantitiesByEmployeeCode(
                companyCode,
                activeEmployeeCodes,
                runMonth);
        Map<String, EmployeeSalary> salaryByEmployeeCode = mapSalariesByEmployeeCode(activeEmployeeSalaries);

        List<PayrollResult> payrollResults = new ArrayList<>();
        int skippedEmployees = 0;
        for (String employeeCode : activeEmployeeCodes) {
            EmployeeSalary employeeSalary = salaryByEmployeeCode.get(employeeCode);
            PayrollPolicy payrollPolicy = payrollPoliciesByEmployeeCode.get(employeeCode);
            if (employeeSalary == null || payrollPolicy == null) {
                skippedEmployees++;
                log.warn(
                        "PAYROLL_RESULT action=EMPLOYEE_SKIPPED result=FAILED reason=MISSING_INPUT companyCode={} payrollRunCode={} period={} employeeCode={} hasSalary={} hasPolicy={}",
                        companyCode,
                        payrollRunCode,
                        runMonth,
                        employeeCode,
                        employeeSalary != null,
                        payrollPolicy != null);
                continue;
            }

            payrollResults.add(buildPayrollResult(
                    payrollRun,
                    employeeSalary,
                    payrollPolicy,
                    totalWorkingDays,
                    leaveQuantitiesByEmployeeCode.getOrDefault(employeeCode, 0)));
        }
        boolean finalStatus = false;
        if (!payrollResults.isEmpty()) {
            List<PayrollResult> savedPayrollResults = payrollResultRepository.saveAll(payrollResults);
            log.info(
                    "PAYROLL_RESULT action=PREVIEW_RESULTS_CREATED result=SUCCESS companyCode={} payrollRunCode={} period={} createdCount={} skippedCount={} totalWorkingDays={}",
                    companyCode,
                    payrollRunCode,
                    runMonth,
                    savedPayrollResults.size(),
                    skippedEmployees,
                    totalWorkingDays);
            scheduleEmployeeSalaryCalculation(companyCode, activeEmployeeCodes, savedPayrollResults, runDate);
            finalStatus = true;
        } else {
            log.warn(
                    "PAYROLL_RESULT action=GENERATE_FINISHED result=FAILED reason=NO_ELIGIBLE_EMPLOYEES companyCode={} payrollRunCode={} period={} activeEmployeeCount={} skippedCount={}",
                    companyCode,
                    payrollRunCode,
                    runMonth,
                    activeEmployeeCodes.size(),
                    skippedEmployees);
        }

        payrollRunService.finalizePayrollRun(payrollRunCode, finalStatus);
        log.info("PAYROLL_RESULT action=RUN_STATUS_UPDATED companyCode={} payrollRunCode={} period={} status={}",
                companyCode, payrollRunCode, runMonth, finalStatus);
    }

    private void scheduleEmployeeSalaryCalculation(
            String companyCode,
            List<String> employeeCodes,
            List<PayrollResult> payrollResults,
            LocalDate runDate) {
        String companyCodeSnapshot = companyCode;
        List<String> employeeCodesSnapshot = List.copyOf(employeeCodes);
        List<PayrollResult> payrollResultsSnapshot = List.copyOf(payrollResults);
        Runnable task = () -> employeeSalaryService.employeeSalaryCalculation(
                companyCodeSnapshot,
                employeeCodesSnapshot,
                payrollResultsSnapshot,
                runDate);
        String payrollRunCode = resolvePayrollRunCode(payrollResults);
        YearMonth runMonth = YearMonth.from(runDate);

        if (TransactionSynchronizationManager.isSynchronizationActive()
                && TransactionSynchronizationManager.isActualTransactionActive()) {
            log.info(
                    "PAYROLL_CALC action=SCHEDULED timing=AFTER_COMMIT companyCode={} payrollRunCode={} period={} employeeCount={} resultCount={}",
                    companyCodeSnapshot,
                    payrollRunCode,
                    runMonth,
                    employeeCodesSnapshot.size(),
                    payrollResultsSnapshot.size());
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    log.info(
                            "PAYROLL_CALC action=DISPATCHING timing=AFTER_COMMIT companyCode={} payrollRunCode={} period={} employeeCount={} resultCount={}",
                            companyCodeSnapshot,
                            payrollRunCode,
                            runMonth,
                            employeeCodesSnapshot.size(),
                            payrollResultsSnapshot.size());
                    task.run();
                }
            });
            return;
        }

        log.info(
                "PAYROLL_CALC action=DISPATCHING timing=IMMEDIATE companyCode={} payrollRunCode={} period={} employeeCount={} resultCount={}",
                companyCodeSnapshot,
                payrollRunCode,
                runMonth,
                employeeCodesSnapshot.size(),
                payrollResultsSnapshot.size());
        task.run();
    }

    private String resolvePayrollRunCode(List<PayrollResult> payrollResults) {
        if (payrollResults == null || payrollResults.isEmpty()) {
            return null;
        }
        PayrollResult payrollResult = payrollResults.get(0);
        if (payrollResult == null || payrollResult.getPayrollRun() == null) {
            return null;
        }
        return payrollResult.getPayrollRun().getCode();
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
        List<DailyWork> dailyWorks = dailyWorkRepository
                .findAllForSalaryByCompanyCodeAndEmployeeCodesAndWorkingDateBetween(
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
        return payrollResult;
    }

    private int toDailyWorkQuantity(DailyWork dailyWork) {
        return new DailyWorkForSalaryResponse(
                dailyWork.getWorkType(),
                dailyWork.getHoursWorked() == null ? 0 : dailyWork.getHoursWorked().intValue())
                .getTotalWorkHours();
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

    @Override
    public List<PayrollResult> resolveTargetResults(String payrollRunCode, String companyCode,
            PayrollRerunRequest request, PayrollRerunMode mode) {
        if (PayrollRerunMode.SELECTED_EMPLOYEES.equals(mode)) {
            List<String> employeeCodes = request.getEmployeeCodes().stream()
                    .map(CustomStringUtils::normalizeCode)
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();
            return payrollResultRepository.findActiveByRunAndCompanyAndEmployeeCodes(
                    payrollRunCode, companyCode, employeeCodes);
        }
        return payrollResultRepository.findActiveByRunAndCompany(payrollRunCode, companyCode);
    }

    @Override
    public String toResultJson(PayrollResult payrollResult) {
        return "{"
                + "\"code\":\"" + CustomStringUtils.escapeJson(payrollResult.getCode()) + "\","
                + "\"expectedAmount\":\"" + CustomStringUtils.escapeJson(payrollResult.getExpectedAmount()) + "\","
                + "\"actualAmount\":\"" + CustomStringUtils.escapeJson(payrollResult.getActualAmount()) + "\","
                + "\"expectedQuantity\":" + payrollResult.getExpectedQuantity() + ","
                + "\"actualQuantity\":" + payrollResult.getActualQuantity() + ","
                + "\"currency\":\"" + CustomStringUtils.escapeJson(payrollResult.getCurrency()) + "\","
                + "\"sourceType\":\"" + payrollResult.getSourceType() + "\""
                + "}";
    }

    @Override
    public PayrollResult buildRerunPayrollResult(PayrollRun payrollRun, PayrollResult oldResult,
            EmployeeSalary activeSalary, MonthlySalaryCalculationResponse calculation, String companySecretKey) {
        PayrollResult payrollResult = new PayrollResult();
        payrollResult.setPayrollRun(payrollRun);
        payrollResult.setEmployeeSalary(activeSalary);
        payrollResult.setExpectedAmount(
                activeSalary == null ? oldResult.getExpectedAmount() : activeSalary.getTotalAmount());
        payrollResult.setActualAmount(CompanySecretKeyCryptoUtils.encrypt(
                calculation.getFinalSalary().toPlainString(), companySecretKey));
        payrollResult.setCurrency(activeSalary == null ? oldResult.getCurrency() : activeSalary.getCurrency());
        payrollResult.setExpectedQuantity(oldResult.getExpectedQuantity());
        payrollResult.setActualQuantity(calculation.getActualWorkingHourPerMonth() == null
                ? oldResult.getActualQuantity()
                : calculation.getActualWorkingHourPerMonth().intValue());
        payrollResult.setUnit(oldResult.getUnit());
        payrollResult.setSourceType(oldResult.getSourceType());
        payrollResult.setIsRetro(Boolean.TRUE);
        payrollResult.setRetroReason("Payroll re-run");
        payrollResultRepository.saveAndFlush(payrollResult);
        return payrollResult;
    }

    @Override
    public PayrollResult saveRerunPayRollResult(PayrollResult payrollResult) {
        PayrollResult validPayrollResult = Optional.ofNullable(payrollResult)
                .orElseThrow(() -> new BadRequestException(Messages.ERROR_PAYROLL_RESULT_NULL));
        return payrollResultRepository.saveAndFlush(validPayrollResult);
    }

    @Override
    public void softDeleteOldResult(String payrollRunCode) {
        PayrollResult oldResult = payrollResultRepository
                .findByCodeAndCompanyCodeAndIsDeletedFalse(payrollRunCode,
                        securityContextService.getCurrentCompanyCode())
                .orElseThrow(() -> new ResourceNotFoundException(Messages.ERROR_PAYROLL_RESULT_NOT_FOUND));
        List<PayrollResultDetail> oldDetails = payrollResultDetailRepository
                .findByPayrollResult_CodeAndIsDeletedFalse(payrollRunCode);
        oldDetails.forEach(detail -> {
            detail.markDeleted();
        });
        if (!oldDetails.isEmpty()) {
            payrollResultDetailRepository.saveAll(oldDetails);
        }
        oldResult.markDeleted();
        payrollResultRepository.save(oldResult);
    }

}
