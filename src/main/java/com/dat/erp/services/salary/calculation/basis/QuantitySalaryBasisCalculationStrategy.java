package com.dat.erp.services.salary.calculation.basis;

import java.math.BigDecimal;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.dat.erp.constants.SalaryBasisType;

@Component
public class QuantitySalaryBasisCalculationStrategy implements SalaryBasisCalculationStrategy {

    @Override
    public SalaryBasisType supports() {
        return SalaryBasisType.QUANTITY;
    }

    @Override
    public SalaryBasisCalculationResult calculate(SalaryBasisCalculationContext context) {
        BigDecimal quantity = context.productionResults().stream()
                .map(result -> result.getQuantity() == null ? BigDecimal.ZERO : result.getQuantity())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        Map<String, BigDecimal> quantityByProduct = context.productionResults().stream()
                .collect(Collectors.groupingBy(result -> result.getProductCode() == null ? "UNKNOWN"
                        : result.getProductCode(), Collectors.mapping(
                                result -> result.getQuantity() == null ? BigDecimal.ZERO : result.getQuantity(),
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))));
        return new SalaryBasisCalculationResult(supports(), "UNIT",
                BigDecimal.ZERO, quantity, context.standardMoneyPerHour(), Map.of(), quantityByProduct);
    }
}
