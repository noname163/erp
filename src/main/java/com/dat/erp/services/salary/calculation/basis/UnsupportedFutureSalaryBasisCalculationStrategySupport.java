package com.dat.erp.services.salary.calculation.basis;

import com.dat.erp.constants.SalaryBasisType;
import com.dat.erp.exceptions.BadRequestException;

abstract class UnsupportedFutureSalaryBasisCalculationStrategySupport implements SalaryBasisCalculationStrategy {

    @Override
    public SalaryBasisCalculationResult calculate(SalaryBasisCalculationContext context) {
        throw new BadRequestException("Salary basis type " + supports() + " is not implemented yet");
    }
}
