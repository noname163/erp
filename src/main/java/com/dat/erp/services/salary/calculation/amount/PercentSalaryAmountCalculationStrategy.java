package com.dat.erp.services.salary.calculation.amount;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Component;

import com.dat.erp.constants.SalaryCalculateMethod;

@Component
public class PercentSalaryAmountCalculationStrategy implements SalaryAmountCalculationStrategy {

    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

    @Override
    public SalaryCalculateMethod supports() {
        return SalaryCalculateMethod.PERCENT;
    }

    @Override
    public BigDecimal calculate(SalaryAmountCalculationContext context) {
        return context.dependencyOrBase().multiply(context.amountOrZero()).divide(ONE_HUNDRED, 12,
                RoundingMode.HALF_UP);
    }
}
