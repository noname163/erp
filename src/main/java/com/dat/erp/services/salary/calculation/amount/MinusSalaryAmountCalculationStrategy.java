package com.dat.erp.services.salary.calculation.amount;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.dat.erp.constants.SalaryCalculateMethod;

@Component
public class MinusSalaryAmountCalculationStrategy implements SalaryAmountCalculationStrategy {

    @Override
    public SalaryCalculateMethod supports() {
        return SalaryCalculateMethod.MINUS;
    }

    @Override
    public BigDecimal calculate(SalaryAmountCalculationContext context) {
        return context.baseOrZero().subtract(context.amountOrZero());
    }
}
