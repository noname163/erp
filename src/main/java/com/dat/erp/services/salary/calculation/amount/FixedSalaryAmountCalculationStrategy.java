package com.dat.erp.services.salary.calculation.amount;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.dat.erp.constants.SalaryCalculateMethod;

@Component
public class FixedSalaryAmountCalculationStrategy implements SalaryAmountCalculationStrategy {

    @Override
    public SalaryCalculateMethod supports() {
        return SalaryCalculateMethod.FIXED;
    }

    @Override
    public BigDecimal calculate(SalaryAmountCalculationContext context) {
        return context.amountOrZero();
    }
}
