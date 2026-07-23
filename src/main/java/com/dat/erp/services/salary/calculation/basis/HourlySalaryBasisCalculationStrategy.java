package com.dat.erp.services.salary.calculation.basis;

import java.math.BigDecimal;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.dat.erp.constants.SalaryBasisType;

@Component
public class HourlySalaryBasisCalculationStrategy implements SalaryBasisCalculationStrategy {

    @Override
    public SalaryBasisType supports() {
        return SalaryBasisType.HOURLY;
    }

    @Override
    public SalaryBasisCalculationResult calculate(SalaryBasisCalculationContext context) {
        Map<String, BigDecimal> actualBasisValuesByType = context.actualHoursByDayType().entrySet().stream()
                .collect(Collectors.toMap(entry -> entry.getKey().name(), Map.Entry::getValue));
        return new SalaryBasisCalculationResult(SalaryBasisType.HOURLY, "HOUR",
                context.expectedWorkingHourPerMonth(), context.actualWorkingHourPerMonth(),
                context.standardMoneyPerHour(), context.actualHoursByDayType(), actualBasisValuesByType);
    }
}
