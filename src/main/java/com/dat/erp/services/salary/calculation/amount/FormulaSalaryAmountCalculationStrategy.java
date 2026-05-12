package com.dat.erp.services.salary.calculation.amount;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.dat.erp.constants.SalaryCalculateMethod;

@Component
public class FormulaSalaryAmountCalculationStrategy implements SalaryAmountCalculationStrategy {

    @Override
    public SalaryCalculateMethod supports() {
        return SalaryCalculateMethod.FORMULA;
    }

    @Override
    public BigDecimal calculate(SalaryAmountCalculationContext context) {
        return context.amountOrZero();
    }
}
