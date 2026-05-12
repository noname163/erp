package com.dat.erp.services.salary.calculation.detail;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.dat.erp.constants.DayType;
import com.dat.erp.constants.Messages;
import com.dat.erp.constants.SalaryCalculateMethod;
import com.dat.erp.entities.EmployeeSalaryDetail;
import com.dat.erp.entities.Salary;
import com.dat.erp.exceptions.BadRequestException;
import com.dat.erp.services.salary.calculation.MonthlySalaryCalculationContext;
import com.dat.erp.services.salary.calculation.SalaryCalculationAmounts;
import com.dat.erp.services.salary.calculation.amount.SalaryAmountCalculationContext;
import com.dat.erp.services.salary.calculation.amount.SalaryAmountCalculationStrategyFactory;

@Component
public class SalaryDetailDependencyEvaluator {

    private static final Logger LOGGER = LoggerFactory.getLogger(SalaryDetailDependencyEvaluator.class);

    private final SalaryAmountCalculationStrategyFactory amountCalculationStrategyFactory;

    public SalaryDetailDependencyEvaluator(SalaryAmountCalculationStrategyFactory amountCalculationStrategyFactory) {
        this.amountCalculationStrategyFactory = amountCalculationStrategyFactory;
    }

    public List<SalaryDetailCalculationResult> evaluate(MonthlySalaryCalculationContext context) {
        Map<String, EmployeeSalaryDetail> detailsByCode = new LinkedHashMap<>();
        for (EmployeeSalaryDetail detail : context.getDetails()) {
            Salary salary = detail.getSalary();
            if (salary != null && salary.getCode() != null) {
                detailsByCode.put(salary.getCode(), detail);
            }
        }

        Map<String, SalaryDetailCalculationResult> computedByCode = new LinkedHashMap<>();
        Set<String> activePath = new HashSet<>();
        for (String salaryCode : detailsByCode.keySet()) {
            evaluateDetail(salaryCode, detailsByCode, context, computedByCode, activePath);
        }
        return new ArrayList<>(computedByCode.values());
    }

    private BigDecimal evaluateDetail(String salaryCode,
            Map<String, EmployeeSalaryDetail> detailsByCode,
            MonthlySalaryCalculationContext context,
            Map<String, SalaryDetailCalculationResult> computedByCode,
            Set<String> activePath) {
        SalaryDetailCalculationResult cachedResult = computedByCode.get(salaryCode);
        if (cachedResult != null) {
            return cachedResult.result();
        }
        if (!activePath.add(salaryCode)) {
            throw new BadRequestException(String.format(Messages.ERROR_PAYROLL_CIRCULAR_DEPENDENCY, activePath));
        }

        EmployeeSalaryDetail detail = detailsByCode.get(salaryCode);
        if (detail == null) {
            throw new BadRequestException(String.format(Messages.ERROR_PAYROLL_DEPENDENCE_CODE_MISSING, salaryCode));
        }

        DayType dayType = detail.getDayType();
        BigDecimal actualHours = context.getActualHoursByDayType().get(dayType);
        if (dayType == null || actualHours == null) {
            throw new BadRequestException(String.format(Messages.ERROR_PAYROLL_DAY_TYPE_HOURS_MISSING, dayType));
        }

        BigDecimal baseAmount = context.getStandardMoneyPerHour().multiply(actualHours);
        BigDecimal configuredAmount = SalaryCalculationAmounts.toAmount(detail.getAmount());

        String dependenceCode = detail.getDependenceCode() == null ? null : detail.getDependenceCode().getCode();
        BigDecimal dependencyAmount = null;
        if (dependenceCode != null) {
            if (!detailsByCode.containsKey(dependenceCode)) {
                throw new BadRequestException(
                        String.format(Messages.ERROR_PAYROLL_DEPENDENCE_CODE_MISSING, dependenceCode));
            }
            dependencyAmount = evaluateDetail(dependenceCode, detailsByCode, context, computedByCode, activePath);
        }

        SalaryCalculateMethod calculateMethod = detail.getSalary() == null ? SalaryCalculateMethod.FIXED
                : detail.getSalary().getCalculateMethod();
        BigDecimal result = amountCalculationStrategyFactory.calculate(calculateMethod,
                new SalaryAmountCalculationContext(baseAmount, configuredAmount, dependencyAmount));
        SalaryDetailCalculationResult calculationResult = new SalaryDetailCalculationResult(salaryCode, dayType,
                calculateMethod, dependenceCode, baseAmount, configuredAmount, dependencyAmount, result);
        computedByCode.put(salaryCode, calculationResult);
        activePath.remove(salaryCode);

        LOGGER.debug("Calculated salary detail. salaryCode={} dayType={} method={} dependenceCode={} base={} amount={} "
                + "dependency={} result={}", salaryCode, dayType, calculateMethod, dependenceCode, baseAmount,
                configuredAmount, dependencyAmount, result);

        return result;
    }
}
