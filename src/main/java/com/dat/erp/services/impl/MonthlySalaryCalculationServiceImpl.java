package com.dat.erp.services.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dat.erp.constants.DayType;
import com.dat.erp.constants.Messages;
import com.dat.erp.constants.SalaryCalculateMethod;
import com.dat.erp.dto.response.salary.MonthlySalaryCalculationResponse;
import com.dat.erp.dto.response.salary.MonthlySalaryDetailAuditResponse;
import com.dat.erp.entities.DailyWork;
import com.dat.erp.entities.EmployeeSalary;
import com.dat.erp.entities.EmployeeSalaryDetail;
import com.dat.erp.entities.PayrollPolicy;
import com.dat.erp.entities.Salary;
import com.dat.erp.exceptions.BadRequestException;
import com.dat.erp.exceptions.ResourceNotFoundException;
import com.dat.erp.repositories.customrepositories.DailyWorkRepository;
import com.dat.erp.repositories.customrepositories.EmployeeSalaryDetailRepository;
import com.dat.erp.repositories.customrepositories.EmployeeSalaryRepository;
import com.dat.erp.services.CalendarDateService;
import com.dat.erp.services.EmployeePayrollPolicyService;
import com.dat.erp.services.MonthlySalaryCalculationService;
import com.dat.erp.services.SecurityContextService;
import com.dat.erp.utils.CustomStringUtils;

@Service
public class MonthlySalaryCalculationServiceImpl implements MonthlySalaryCalculationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MonthlySalaryCalculationServiceImpl.class);
    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

    private final SecurityContextService securityContextService;
    private final EmployeeSalaryRepository employeeSalaryRepository;
    private final EmployeeSalaryDetailRepository employeeSalaryDetailRepository;
    private final DailyWorkRepository dailyWorkRepository;
    private final EmployeePayrollPolicyService employeePayrollPolicyService;
    private final CalendarDateService calendarDateService;

    public MonthlySalaryCalculationServiceImpl(SecurityContextService securityContextService,
            EmployeeSalaryRepository employeeSalaryRepository,
            EmployeeSalaryDetailRepository employeeSalaryDetailRepository,
            DailyWorkRepository dailyWorkRepository,
            EmployeePayrollPolicyService employeePayrollPolicyService,
            CalendarDateService calendarDateService) {
        this.securityContextService = securityContextService;
        this.employeeSalaryRepository = employeeSalaryRepository;
        this.employeeSalaryDetailRepository = employeeSalaryDetailRepository;
        this.dailyWorkRepository = dailyWorkRepository;
        this.employeePayrollPolicyService = employeePayrollPolicyService;
        this.calendarDateService = calendarDateService;
    }

    @Override
    @Transactional(readOnly = true)
    public MonthlySalaryCalculationResponse calculateEmployeeMonthlySalary(String employeeCode, YearMonth month) {
        String normalizedEmployeeCode = CustomStringUtils.normalizeCode(employeeCode);
        if (normalizedEmployeeCode == null) {
            throw new BadRequestException(Messages.ERROR_PAYROLL_EMPLOYEE_CODE_INVALID);
        }
        if (month == null) {
            throw new BadRequestException(Messages.ERROR_PAYROLL_MONTH_INVALID);
        }

        String companyCode = securityContextService.getCurrentUser().getAccount().getCompanyCode();
        LocalDate asOfDate = month.atEndOfMonth();
        EmployeeSalary employeeSalary = employeeSalaryRepository
                .findFirstActiveByEmployeeCodeAndCompanyCodeAndDate(normalizedEmployeeCode, companyCode, asOfDate)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(Messages.ERROR_PAYROLL_EMPLOYEE_SALARY_NOT_FOUND, normalizedEmployeeCode, month)));

        PayrollPolicy payrollPolicy = employeePayrollPolicyService
                .getCompanyPoliciesByEmployeeCodesAndDate(List.of(normalizedEmployeeCode), asOfDate)
                .get(normalizedEmployeeCode);
        if (payrollPolicy == null) {
            throw new ResourceNotFoundException(
                    String.format(Messages.ERROR_PAYROLL_POLICY_NOT_FOUND, normalizedEmployeeCode, month));
        }

        BigDecimal expectedWorkingHourPerMonth = resolveExpectedWorkingHours(companyCode, payrollPolicy, month);
        if (expectedWorkingHourPerMonth.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException(Messages.ERROR_PAYROLL_EXPECTED_WORKING_HOURS_INVALID);
        }

        List<EmployeeSalaryDetail> details = employeeSalaryDetailRepository
                .findForPayrollByEmployeeSalaryCodeAndCompanyCode(employeeSalary.getCode(), companyCode);

        Map<DayType, BigDecimal> actualHoursByDayType = aggregateHoursByDayType(companyCode, normalizedEmployeeCode, month);
        BigDecimal actualWorkingHourPerMonth = actualHoursByDayType.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal fixedDeductBase = details.stream()
                .filter(detail -> detail.getSalary() != null && Boolean.TRUE.equals(detail.getSalary().getIsDeduct()))
                .filter(detail -> Boolean.TRUE.equals(detail.getIsFixed()))
                .map(detail -> toAmount(detail.getAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal standardMoneyPerHour = fixedDeductBase.divide(expectedWorkingHourPerMonth, 12, RoundingMode.HALF_UP);

        List<MonthlySalaryDetailAuditResponse> auditTrail = new ArrayList<>();
        String salaryUnit = employeeSalary.getCurrency();
        BigDecimal finalSalary;
        if (actualWorkingHourPerMonth.compareTo(expectedWorkingHourPerMonth) == 0) {
            finalSalary = details.stream().map(detail -> toAmount(detail.getAmount())).reduce(BigDecimal.ZERO, BigDecimal::add);
            details.forEach(detail -> auditTrail.add(new MonthlySalaryDetailAuditResponse(
                    detail.getSalary() == null ? null : detail.getSalary().getCode(),
                    detail.getDayType(),
                    detail.getSalary() == null ? null : detail.getSalary().getCalculateMethod(),
                    detail.getDependenceCode() == null ? null : detail.getDependenceCode().getCode(),
                    salaryUnit,
                    null,
                    toAmount(detail.getAmount()),
                    null,
                    toAmount(detail.getAmount()),
                    null)));
        } else {
            finalSalary = calculateWithHourlyModel(details, actualHoursByDayType, standardMoneyPerHour, salaryUnit, auditTrail);
        }

        BigDecimal roundedFinalSalary = finalSalary.setScale(4, RoundingMode.HALF_UP);
        auditTrail.forEach(item -> item.setTotalAmount(roundedFinalSalary));

        return new MonthlySalaryCalculationResponse(normalizedEmployeeCode, month, expectedWorkingHourPerMonth,
                actualWorkingHourPerMonth, standardMoneyPerHour, roundedFinalSalary,
                Collections.unmodifiableMap(actualHoursByDayType),
                List.copyOf(auditTrail));
    }

    private BigDecimal calculateWithHourlyModel(List<EmployeeSalaryDetail> details,
            Map<DayType, BigDecimal> actualHoursByDayType,
            BigDecimal standardMoneyPerHour,
            String salaryUnit,
            List<MonthlySalaryDetailAuditResponse> auditTrail) {
        Map<String, EmployeeSalaryDetail> detailsByCode = new LinkedHashMap<>();
        for (EmployeeSalaryDetail detail : details) {
            Salary salary = detail.getSalary();
            if (salary != null && salary.getCode() != null) {
                detailsByCode.put(salary.getCode(), detail);
            }
        }

        Map<String, BigDecimal> computedByCode = new LinkedHashMap<>();
        Set<String> activePath = new HashSet<>();
        for (String salaryCode : detailsByCode.keySet()) {
            evaluateDetail(salaryCode, detailsByCode, actualHoursByDayType, standardMoneyPerHour, salaryUnit, computedByCode,
                    activePath, auditTrail);
        }

        return computedByCode.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal evaluateDetail(String salaryCode,
            Map<String, EmployeeSalaryDetail> detailsByCode,
            Map<DayType, BigDecimal> actualHoursByDayType,
            BigDecimal standardMoneyPerHour,
            String salaryUnit,
            Map<String, BigDecimal> computedByCode,
            Set<String> activePath,
            List<MonthlySalaryDetailAuditResponse> auditTrail) {
        if (computedByCode.containsKey(salaryCode)) {
            return computedByCode.get(salaryCode);
        }
        if (!activePath.add(salaryCode)) {
            throw new BadRequestException(String.format(Messages.ERROR_PAYROLL_CIRCULAR_DEPENDENCY, activePath));
        }

        EmployeeSalaryDetail detail = detailsByCode.get(salaryCode);
        if (detail == null) {
            throw new BadRequestException(String.format(Messages.ERROR_PAYROLL_DEPENDENCE_CODE_MISSING, salaryCode));
        }

        DayType dayType = detail.getDayType();
        BigDecimal actualHours = actualHoursByDayType.get(dayType);
        if (dayType == null || actualHours == null) {
            throw new BadRequestException(String.format(Messages.ERROR_PAYROLL_DAY_TYPE_HOURS_MISSING, dayType));
        }

        BigDecimal hourlyBase = standardMoneyPerHour.multiply(actualHours);
        BigDecimal configuredAmount = toAmount(detail.getAmount());

        String dependenceCode = detail.getDependenceCode() == null ? null : detail.getDependenceCode().getCode();
        BigDecimal dependencyAmount = null;
        if (dependenceCode != null) {
            if (!detailsByCode.containsKey(dependenceCode)) {
                throw new BadRequestException(String.format(Messages.ERROR_PAYROLL_DEPENDENCE_CODE_MISSING, dependenceCode));
            }
            dependencyAmount = evaluateDetail(dependenceCode, detailsByCode, actualHoursByDayType, standardMoneyPerHour,
                    salaryUnit, computedByCode, activePath, auditTrail);
        }

        SalaryCalculateMethod calculateMethod = detail.getSalary() == null ? SalaryCalculateMethod.FIXED
                : detail.getSalary().getCalculateMethod();
        BigDecimal result = applyCalculateMethod(calculateMethod, hourlyBase, configuredAmount, dependencyAmount);
        computedByCode.put(salaryCode, result);
        activePath.remove(salaryCode);

        auditTrail.add(new MonthlySalaryDetailAuditResponse(
                salaryCode,
                dayType,
                calculateMethod,
                dependenceCode,
                salaryUnit,
                hourlyBase,
                configuredAmount,
                dependencyAmount,
                result,
                null));
        LOGGER.info(
                "Payroll detail computed salaryCode={} dayType={} method={} dependenceCode={} base={} amount={} dependency={} result={}",
                salaryCode, dayType, calculateMethod, dependenceCode, hourlyBase, configuredAmount, dependencyAmount, result);

        return result;
    }

    private BigDecimal applyCalculateMethod(SalaryCalculateMethod method,
            BigDecimal hourlyBase,
            BigDecimal amount,
            BigDecimal dependencyAmount) {
        if (method == null) {
            return amount;
        }
        return switch (method) {
            case PLUS -> hourlyBase.add(amount);
            case MINUS -> hourlyBase.subtract(amount);
            case FIXED, FORMULA -> amount;
            case PERCENT -> {
                BigDecimal percentBase = dependencyAmount == null ? hourlyBase : dependencyAmount;
                yield percentBase.multiply(amount).divide(ONE_HUNDRED, 12, RoundingMode.HALF_UP);
            }
            case DIVIDE -> {
                BigDecimal divideBase = dependencyAmount == null ? hourlyBase : dependencyAmount;
                if (amount.compareTo(BigDecimal.ZERO) == 0) {
                    throw new BadRequestException(Messages.ERROR_PAYROLL_EXPECTED_WORKING_HOURS_INVALID);
                }
                yield divideBase.divide(amount, 12, RoundingMode.HALF_UP);
            }
        };
    }

    private BigDecimal resolveExpectedWorkingHours(String companyCode, PayrollPolicy payrollPolicy, YearMonth month) {
        Integer standardHoursPerDay = payrollPolicy.getStandardQuantityPerDay();
        if (standardHoursPerDay == null || standardHoursPerDay <= 0) {
            return BigDecimal.ZERO;
        }

        Map<DayType, Integer> dayTypeCounts = calendarDateService.getCalendarDateTotalsByCompanyCodeAndMonth(companyCode, month);
        int normalWorkingDays = dayTypeCounts.getOrDefault(DayType.NORMAL, 0);
        return BigDecimal.valueOf((long) standardHoursPerDay * normalWorkingDays);
    }

    private Map<DayType, BigDecimal> aggregateHoursByDayType(String companyCode, String employeeCode, YearMonth month) {
        Map<DayType, BigDecimal> result = new EnumMap<>(DayType.class);
        List<DailyWork> monthlyWorks = dailyWorkRepository.findAllByEmployeeCodeAndCompanyCodeAndDateRange(employeeCode,
                companyCode, month.atDay(1), month.atEndOfMonth());
        for (DailyWork dailyWork : monthlyWorks) {
            if (dailyWork.getWorkType() == null) {
                continue;
            }
            result.merge(dailyWork.getWorkType(),
                    dailyWork.getHoursWorked() == null ? BigDecimal.ZERO : dailyWork.getHoursWorked(),
                    BigDecimal::add);
        }
        return result;
    }

    private BigDecimal toAmount(String value) {
        return CustomStringUtils.parsePositiveBigDecimal(value, Messages.ERROR_EMPLOYEE_SALARY_DETAIL_AMOUNT_INVALID);
    }
}
