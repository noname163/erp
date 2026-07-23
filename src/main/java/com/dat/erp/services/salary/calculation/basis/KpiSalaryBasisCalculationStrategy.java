package com.dat.erp.services.salary.calculation.basis;

import java.math.BigDecimal;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.dat.erp.constants.SalaryBasisType;

@Component
public class KpiSalaryBasisCalculationStrategy implements SalaryBasisCalculationStrategy {

    @Override
    public SalaryBasisType supports() {
        return SalaryBasisType.KPI;
    }

    @Override
    public SalaryBasisCalculationResult calculate(SalaryBasisCalculationContext context) {
        BigDecimal score = context.kpiResults().stream()
                .map(result -> result.getScore() == null ? BigDecimal.ZERO : result.getScore())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal configuredAmount = context.kpiResults().stream()
                .map(result -> result.getAmount() == null ? BigDecimal.ZERO : result.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        Map<String, BigDecimal> scoreByKpi = context.kpiResults().stream()
                .collect(Collectors.groupingBy(result -> result.getKpiCode() == null ? "UNKNOWN" : result.getKpiCode(),
                        Collectors.mapping(result -> result.getScore() == null ? BigDecimal.ZERO : result.getScore(),
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))));
        BigDecimal rate = configuredAmount.compareTo(BigDecimal.ZERO) > 0 ? BigDecimal.ONE
                : context.standardMoneyPerHour();
        BigDecimal actualValue = configuredAmount.compareTo(BigDecimal.ZERO) > 0 ? configuredAmount : score;
        return new SalaryBasisCalculationResult(SalaryBasisType.KPI, "KPI",
                BigDecimal.ZERO, actualValue, rate, Map.of(), scoreByKpi);
    }
}
