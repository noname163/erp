package com.dat.erp.services.salary.calculation.amount;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.dat.erp.constants.SalaryCalculateMethod;

@Component
public class PlusSalaryAmountCalculationStrategy implements SalaryAmountCalculationStrategy {

    @Override
    public SalaryCalculateMethod supports() {
        return SalaryCalculateMethod.PLUS;
    }

    @Override
    public BigDecimal calculate(SalaryAmountCalculationContext context) {
        return context.baseOrZero().add(context.amountOrZero());
    }
}
