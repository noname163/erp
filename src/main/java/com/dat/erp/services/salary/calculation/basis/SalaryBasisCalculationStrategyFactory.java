package com.dat.erp.services.salary.calculation.basis;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.dat.erp.constants.SalaryBasisType;

@Component
public class SalaryBasisCalculationStrategyFactory {

    private final Map<SalaryBasisType, SalaryBasisCalculationStrategy> strategiesByType = new EnumMap<>(
            SalaryBasisType.class);

    public SalaryBasisCalculationStrategyFactory(List<SalaryBasisCalculationStrategy> strategies) {
        strategies.forEach(strategy -> strategiesByType.put(strategy.supports(), strategy));
    }

    public SalaryBasisCalculationStrategy getStrategy(SalaryBasisType salaryBasisType) {
        return strategiesByType.getOrDefault(salaryBasisType, strategiesByType.get(SalaryBasisType.WORKING_HOUR));
    }
}
