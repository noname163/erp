package com.dat.erp.services.salary.calculation;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.dat.erp.constants.DayType;
import com.dat.erp.constants.Messages;
import com.dat.erp.constants.SalaryCalculateMethod;
import com.dat.erp.dto.response.salary.MonthlySalaryDetailAuditResponse;
import com.dat.erp.entities.EmployeeSalaryDetail;
import com.dat.erp.entities.Salary;
import com.dat.erp.exceptions.BadRequestException;

@Component
@Order(50)
public class CalculateSalaryDetailsStep implements MonthlySalaryCalculationStep {

    private static final Logger LOGGER = LoggerFactory.getLogger(CalculateSalaryDetailsStep.class);
    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

    @Override
    public void execute(MonthlySalaryCalculationContext context) {
        List<MonthlySalaryDetailAuditResponse> auditTrail = new ArrayList<>();
        BigDecimal finalSalary;
        if (context.getActualWorkingHourPerMonth().compareTo(context.getExpectedWorkingHourPerMonth()) == 0) {
            finalSalary = calculateWithFullAmountModel(context, auditTrail);
        } else {
            finalSalary = calculateWithHourlyModel(context, auditTrail);
        }

        context.setFinalSalary(finalSalary);
        context.setAuditTrail(auditTrail);
    }

    private BigDecimal calculateWithFullAmountModel(MonthlySalaryCalculationContext context,
            List<MonthlySalaryDetailAuditResponse> auditTrail) {
        String salaryUnit = context.getEmployeeSalary().getCurrency();
        context.getDetails().forEach(detail -> {
            BigDecimal amount = SalaryCalculationAmounts.toAmount(detail.getAmount());
            auditTrail.add(new MonthlySalaryDetailAuditResponse(
                    detail.getSalary() == null ? null : detail.getSalary().getCode(),
                    detail.getDayType(),
                    detail.getSalary() == null ? null : detail.getSalary().getCalculateMethod(),
                    detail.getDependenceCode() == null ? null : detail.getDependenceCode().getCode(),
                    salaryUnit,
                    null,
                    amount,
                    null,
                    amount,
                    null));
        });

        return context.getDetails().stream()
                .map(detail -> SalaryCalculationAmounts.toAmount(detail.getAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateWithHourlyModel(MonthlySalaryCalculationContext context,
            List<MonthlySalaryDetailAuditResponse> auditTrail) {
        Map<String, EmployeeSalaryDetail> detailsByCode = new LinkedHashMap<>();
        for (EmployeeSalaryDetail detail : context.getDetails()) {
            Salary salary = detail.getSalary();
            if (salary != null && salary.getCode() != null) {
                detailsByCode.put(salary.getCode(), detail);
            }
        }

        Map<String, BigDecimal> computedByCode = new LinkedHashMap<>();
        Set<String> activePath = new HashSet<>();
        for (String salaryCode : detailsByCode.keySet()) {
            evaluateDetail(salaryCode, detailsByCode, context, computedByCode, activePath, auditTrail);
        }

        return computedByCode.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal evaluateDetail(String salaryCode,
            Map<String, EmployeeSalaryDetail> detailsByCode,
            MonthlySalaryCalculationContext context,
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
        BigDecimal actualHours = context.getActualHoursByDayType().get(dayType);
        if (dayType == null || actualHours == null) {
            throw new BadRequestException(String.format(Messages.ERROR_PAYROLL_DAY_TYPE_HOURS_MISSING, dayType));
        }

        BigDecimal hourlyBase = context.getStandardMoneyPerHour().multiply(actualHours);
        BigDecimal configuredAmount = SalaryCalculationAmounts.toAmount(detail.getAmount());

        String dependenceCode = detail.getDependenceCode() == null ? null : detail.getDependenceCode().getCode();
        BigDecimal dependencyAmount = null;
        if (dependenceCode != null) {
            if (!detailsByCode.containsKey(dependenceCode)) {
                throw new BadRequestException(
                        String.format(Messages.ERROR_PAYROLL_DEPENDENCE_CODE_MISSING, dependenceCode));
            }
            dependencyAmount = evaluateDetail(dependenceCode, detailsByCode, context, computedByCode, activePath,
                    auditTrail);
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
                context.getEmployeeSalary().getCurrency(),
                hourlyBase,
                configuredAmount,
                dependencyAmount,
                result,
                null));
        LOGGER.info(
                "Payroll detail computed salaryCode={} dayType={} method={} dependenceCode={} base={} amount={} "
                        + "dependency={} result={}",
                salaryCode, dayType, calculateMethod, dependenceCode, hourlyBase, configuredAmount, dependencyAmount,
                result);

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
}
