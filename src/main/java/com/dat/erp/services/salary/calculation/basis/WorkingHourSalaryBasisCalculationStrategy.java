package com.dat.erp.services.salary.calculation.basis;

import java.math.BigDecimal;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.dat.erp.constants.SalaryBasisType;

@Component
public class WorkingHourSalaryBasisCalculationStrategy implements SalaryBasisCalculationStrategy {

    @Override
    public SalaryBasisType supports() {
        return SalaryBasisType.WORKING_HOUR;
    }

    @Override
    public SalaryBasisCalculationResult calculate(SalaryBasisCalculationContext context) {
        Map<String, BigDecimal> actualBasisValuesByType = context.actualHoursByDayType().entrySet().stream()
                .collect(Collectors.toMap(entry -> entry.getKey().name(), Map.Entry::getValue));
        return new SalaryBasisCalculationResult(SalaryBasisType.WORKING_HOUR, "HOUR",
                context.expectedWorkingHourPerMonth(), context.actualWorkingHourPerMonth(),
                context.standardMoneyPerHour(), context.actualHoursByDayType(), actualBasisValuesByType);
    }
}
