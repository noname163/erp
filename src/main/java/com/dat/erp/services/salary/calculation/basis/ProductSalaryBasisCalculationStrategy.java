package com.dat.erp.services.salary.calculation.basis;

import com.dat.erp.constants.SalaryBasisType;

import org.springframework.stereotype.Component;

@Component
public class ProductSalaryBasisCalculationStrategy extends QuantitySalaryBasisCalculationStrategy {

    @Override
    public SalaryBasisType supports() {
        return SalaryBasisType.PRODUCT;
    }
}
