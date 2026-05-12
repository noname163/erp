package com.dat.erp.services.salary.calculation;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.dat.erp.services.salary.calculation.basis.SalaryBasisCalculationContext;
import com.dat.erp.services.salary.calculation.basis.SalaryBasisCalculationStrategyFactory;

@Component
@Order(45)
public class ResolveSalaryBasisStep implements MonthlySalaryCalculationStep {

    private final SalaryBasisCalculationStrategyFactory salaryBasisCalculationStrategyFactory;

    public ResolveSalaryBasisStep(SalaryBasisCalculationStrategyFactory salaryBasisCalculationStrategyFactory) {
        this.salaryBasisCalculationStrategyFactory = salaryBasisCalculationStrategyFactory;
    }

    @Override
    public void execute(MonthlySalaryCalculationContext context) {
        context.setSalaryBasisCalculationResult(salaryBasisCalculationStrategyFactory.getStrategy(context.getSalaryBasisType())
                .calculate(new SalaryBasisCalculationContext(context)));
    }
}
