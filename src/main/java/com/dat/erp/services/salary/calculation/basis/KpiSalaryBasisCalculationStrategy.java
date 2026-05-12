package com.dat.erp.services.salary.calculation.basis;

import org.springframework.stereotype.Component;

import com.dat.erp.constants.SalaryBasisType;

@Component
public class KpiSalaryBasisCalculationStrategy extends UnsupportedFutureSalaryBasisCalculationStrategySupport {

    @Override
    public SalaryBasisType supports() {
        return SalaryBasisType.KPI;
    }
}
