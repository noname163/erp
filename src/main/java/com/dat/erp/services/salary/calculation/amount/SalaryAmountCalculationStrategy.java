package com.dat.erp.services.salary.calculation.amount;

import java.math.BigDecimal;

import com.dat.erp.constants.SalaryCalculateMethod;

public interface SalaryAmountCalculationStrategy {
    SalaryCalculateMethod supports();

    BigDecimal calculate(SalaryAmountCalculationContext context);
}
