package com.dat.erp.services.salary.calculation.amount;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.dat.erp.constants.SalaryCalculateMethod;

@Component
public class SalaryAmountCalculationStrategyFactory {

    private final Map<SalaryCalculateMethod, SalaryAmountCalculationStrategy> strategiesByMethod = new EnumMap<>(
            SalaryCalculateMethod.class);

    public SalaryAmountCalculationStrategyFactory(List<SalaryAmountCalculationStrategy> strategies) {
        strategies.forEach(strategy -> strategiesByMethod.put(strategy.supports(), strategy));
    }

    public BigDecimal calculate(SalaryCalculateMethod method, SalaryAmountCalculationContext context) {
        if (method == null) {
            return context.amountOrZero();
        }
        SalaryAmountCalculationStrategy strategy = strategiesByMethod.get(method);
        if (strategy == null) {
            return context.amountOrZero();
        }
        return strategy.calculate(context);
    }
}
