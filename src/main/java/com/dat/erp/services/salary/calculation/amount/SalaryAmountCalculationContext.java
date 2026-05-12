package com.dat.erp.services.salary.calculation.amount;

import java.math.BigDecimal;

public record SalaryAmountCalculationContext(BigDecimal baseAmount, BigDecimal configuredAmount,
        BigDecimal dependencyAmount) {

    public BigDecimal amountOrZero() {
        return configuredAmount == null ? BigDecimal.ZERO : configuredAmount;
    }

    public BigDecimal baseOrZero() {
        return baseAmount == null ? BigDecimal.ZERO : baseAmount;
    }

    public BigDecimal dependencyOrBase() {
        return dependencyAmount == null ? baseOrZero() : dependencyAmount;
    }
}
