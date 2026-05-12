package com.dat.erp.services.salary.calculation;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(40)
public class CalculateStandardMoneyPerHourStep implements MonthlySalaryCalculationStep {

    @Override
    public void execute(MonthlySalaryCalculationContext context) {
        BigDecimal fixedDeductBase = context.getDetails().stream()
                .filter(detail -> detail.getSalary() != null && Boolean.TRUE.equals(detail.getSalary().getIsDeduct()))
                .filter(detail -> Boolean.TRUE.equals(detail.getIsFixed()))
                .map(detail -> SalaryCalculationAmounts.toAmount(detail.getAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        context.setStandardMoneyPerHour(
                fixedDeductBase.divide(context.getExpectedWorkingHourPerMonth(), 12, RoundingMode.HALF_UP));
    }
}
