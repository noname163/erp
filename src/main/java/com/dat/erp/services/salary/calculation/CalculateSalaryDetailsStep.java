package com.dat.erp.services.salary.calculation;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.dat.erp.constants.DayType;
import com.dat.erp.constants.PayRateDayType;
import com.dat.erp.constants.SalaryBasisType;
import com.dat.erp.dto.response.salary.MonthlySalaryDetailAuditResponse;
import com.dat.erp.entities.PayRateRule;
import com.dat.erp.services.salary.calculation.detail.SalaryDetailCalculationResult;
import com.dat.erp.services.salary.calculation.detail.SalaryDetailDependencyEvaluator;

@Component
@Order(50)
public class CalculateSalaryDetailsStep implements MonthlySalaryCalculationStep {

    private final SalaryDetailDependencyEvaluator salaryDetailDependencyEvaluator;

    public CalculateSalaryDetailsStep(SalaryDetailDependencyEvaluator salaryDetailDependencyEvaluator) {
        this.salaryDetailDependencyEvaluator = salaryDetailDependencyEvaluator;
    }

    @Override
    public void execute(MonthlySalaryCalculationContext context) {
        if (context.getPayslip() != null) return;
        List<MonthlySalaryDetailAuditResponse> auditTrail = new ArrayList<>();
        BigDecimal finalSalary;
        SalaryBasisType salaryBasisType = context.getSalaryBasisType();
        if (salaryBasisType == SalaryBasisType.HOURLY) {
            finalSalary = calculateHourly(context, auditTrail);
        } else if (salaryBasisType == SalaryBasisType.PRODUCT || salaryBasisType == SalaryBasisType.QUANTITY) {
            finalSalary = calculateDirectBasis(context, auditTrail, DayType.NORMAL);
        } else if (salaryBasisType == SalaryBasisType.KPI) {
            finalSalary = calculateDirectBasis(context, auditTrail, DayType.NORMAL);
        } else if (context.getActualWorkingHourPerMonth().compareTo(context.getExpectedWorkingHourPerMonth()) == 0) {
            finalSalary = calculateWithFullAmountModel(context, auditTrail);
        } else {
            finalSalary = calculateWithHourlyModel(context, auditTrail);
        }

        finalSalary = finalSalary.add(calculateOvertimePay(context, auditTrail));
        context.setFinalSalary(finalSalary);
        context.setAuditTrail(auditTrail);
    }

    private BigDecimal calculateHourly(MonthlySalaryCalculationContext context,
            List<MonthlySalaryDetailAuditResponse> auditTrail) {
        BigDecimal total = BigDecimal.ZERO;
        for (DayType dayType : List.of(DayType.NORMAL, DayType.WEEKEND_WORK, DayType.HOLIDAY_WORK)) {
            BigDecimal hours = context.getActualHoursByDayType().getOrDefault(dayType, BigDecimal.ZERO);
            if (hours.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            BigDecimal multiplier = resolveMultiplier(context, dayType);
            BigDecimal result = context.getStandardMoneyPerHour().multiply(hours).multiply(multiplier);
            auditTrail.add(new MonthlySalaryDetailAuditResponse(null, dayType, null, null,
                    context.getEmployeeSalary().getCurrency(), hours, context.getStandardMoneyPerHour(), multiplier,
                    result, null));
            total = total.add(result);
        }
        return total;
    }

    private BigDecimal calculateDirectBasis(MonthlySalaryCalculationContext context,
            List<MonthlySalaryDetailAuditResponse> auditTrail, DayType dayType) {
        BigDecimal result = context.getSalaryBasisCalculationResult().actualBasisValue()
                .multiply(context.getSalaryBasisCalculationResult().standardMoneyPerUnit());
        auditTrail.add(new MonthlySalaryDetailAuditResponse(null, dayType, null, null,
                context.getEmployeeSalary().getCurrency(),
                context.getSalaryBasisCalculationResult().actualBasisValue(),
                context.getSalaryBasisCalculationResult().standardMoneyPerUnit(), null, result, null));
        return result;
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

        BigDecimal detailTotal = context.getDetails().stream()
                .map(detail -> SalaryCalculationAmounts.toAmount(detail.getAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return detailTotal.add(calculateSpecialDayPremiums(context, auditTrail, false));
    }

    private BigDecimal calculateWithHourlyModel(MonthlySalaryCalculationContext context,
            List<MonthlySalaryDetailAuditResponse> auditTrail) {
        List<SalaryDetailCalculationResult> detailResults = salaryDetailDependencyEvaluator.evaluate(context);
        detailResults.forEach(result -> auditTrail.add(new MonthlySalaryDetailAuditResponse(
                result.salaryCode(),
                result.dayType(),
                result.calculateMethod(),
                result.dependenceCode(),
                context.getEmployeeSalary().getCurrency(),
                result.baseAmount(),
                result.configuredAmount(),
                result.dependencyAmount(),
                result.result(),
                null)));

        BigDecimal detailTotal = detailResults.stream().map(SalaryDetailCalculationResult::result).reduce(BigDecimal.ZERO,
                BigDecimal::add);
        return detailTotal.add(calculateSpecialDayPremiums(context, auditTrail, false));
    }

    private BigDecimal calculateSpecialDayPremiums(MonthlySalaryCalculationContext context,
            List<MonthlySalaryDetailAuditResponse> auditTrail, boolean includeBase) {
        BigDecimal total = BigDecimal.ZERO;
        for (DayType dayType : List.of(DayType.WEEKEND_WORK, DayType.HOLIDAY_WORK)) {
            BigDecimal hours = context.getActualHoursByDayType().getOrDefault(dayType, BigDecimal.ZERO);
            if (hours.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            BigDecimal multiplier = resolveMultiplier(context, dayType);
            BigDecimal payableMultiplier = includeBase ? multiplier : multiplier.subtract(BigDecimal.ONE).max(BigDecimal.ZERO);
            BigDecimal result = context.getStandardMoneyPerHour().multiply(hours).multiply(payableMultiplier);
            if (result.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            auditTrail.add(new MonthlySalaryDetailAuditResponse(null, dayType, null, null,
                    context.getEmployeeSalary().getCurrency(), hours, context.getStandardMoneyPerHour(),
                    payableMultiplier, result, null));
            total = total.add(result);
        }
        return total;
    }

    private BigDecimal calculateOvertimePay(MonthlySalaryCalculationContext context,
            List<MonthlySalaryDetailAuditResponse> auditTrail) {
        BigDecimal overtimeHours = context.getOvertimeHours() == null ? BigDecimal.ZERO : context.getOvertimeHours();
        if (overtimeHours.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal multiplier = resolveMultiplier(context, DayType.NORMAL);
        BigDecimal result = context.getStandardMoneyPerHour().multiply(overtimeHours).multiply(multiplier);
        auditTrail.add(new MonthlySalaryDetailAuditResponse(null, DayType.NORMAL, null, null,
                context.getEmployeeSalary().getCurrency(), overtimeHours, context.getStandardMoneyPerHour(),
                multiplier, result, null));
        return result;
    }

    private BigDecimal resolveMultiplier(MonthlySalaryCalculationContext context, DayType dayType) {
        if (context.getPayRateRules() == null) {
            return BigDecimal.ONE;
        }
        PayRateDayType payRateDayType = switch (dayType) {
            case HOLIDAY_WORK, HOLIDAY -> PayRateDayType.HOLIDAY;
            case WEEKEND_WORK, WEEKEND -> PayRateDayType.WEEKEND;
            case COMPANY_DAY_OFF -> PayRateDayType.DAY_OFF;
            default -> PayRateDayType.NORMAL;
        };
        return context.getPayRateRules().stream()
                .filter(rule -> rule.getDayType() == payRateDayType)
                .map(PayRateRule::getMultiplier)
                .filter(multiplier -> multiplier != null && multiplier.compareTo(BigDecimal.ZERO) > 0)
                .findFirst()
                .orElse(BigDecimal.ONE);
    }
}
