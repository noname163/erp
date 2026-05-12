package com.dat.erp.services.salary.calculation.basis;

import com.dat.erp.constants.SalaryBasisType;

public interface SalaryBasisCalculationStrategy {
    SalaryBasisType supports();

    SalaryBasisCalculationResult calculate(SalaryBasisCalculationContext context);
}
