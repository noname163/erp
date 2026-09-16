package com.dat.erp.data;

import java.math.BigDecimal;

import com.dat.erp.exceptions.BadRequestException;
import com.dat.erp.utils.ErrorUtils;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MonetaryAmountData {

    private final String amount;
    private final String expectedAmount;
    private final String actualAmount;
    private final String totalAmount;
    private final BigDecimal decimalAmount;
    private final BigDecimal decimalTotalAmount;
    private final String currency;

    public MonetaryAmountData(
            String amount,
            String expectedAmount,
            String actualAmount,
            String totalAmount,
            BigDecimal decimalAmount,
            BigDecimal decimalTotalAmount,
            String currency) {

        this.amount = requireValueIfPresent(amount, "Amount must not be blank");
        this.expectedAmount = requireValueIfPresent(expectedAmount, "Expected amount must not be blank");
        this.actualAmount = requireValueIfPresent(actualAmount, "Actual amount must not be blank");
        this.totalAmount = requireValueIfPresent(totalAmount, "Total amount must not be blank");
        this.decimalAmount = requireNonNegativeIfPresent(decimalAmount, "Amount must not be negative");
        this.decimalTotalAmount = requireNonNegativeIfPresent(decimalTotalAmount, "Total amount must not be negative");
        this.currency = currency == null ? null : ErrorUtils.requireNotBlank(currency, "Currency must not be blank");

        if (this.amount == null
                && this.expectedAmount == null
                && this.actualAmount == null
                && this.totalAmount == null
                && this.decimalAmount == null
                && this.decimalTotalAmount == null
                && this.currency == null) {
            throw new BadRequestException("At least one monetary value is required");
        }
    }

    private static String requireValueIfPresent(
            String value,
            String message) {

        if (value == null) {
            return null;
        }

        return ErrorUtils.requireNotBlank(value, message);
    }

    private static BigDecimal requireNonNegativeIfPresent(
            BigDecimal value,
            String message) {

        if (value != null && value.signum() < 0) {
            throw new BadRequestException(message);
        }
        return value;
    }
}
