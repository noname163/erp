package com.dat.erp.data;

import com.dat.erp.entities.SystemUnit;
import com.dat.erp.exceptions.BadRequestException;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MeasuredQuantityData {

    private final Integer quantity;
    private final Integer expectedQuantity;
    private final Integer actualQuantity;
    private final SystemUnit systemUnit;

    public MeasuredQuantityData(
            Integer quantity,
            Integer expectedQuantity,
            Integer actualQuantity,
            SystemUnit systemUnit) {

        this.quantity = requireNonNegativeIfPresent(quantity, "Quantity must not be negative");
        this.expectedQuantity = requireNonNegativeIfPresent(expectedQuantity, "Expected quantity must not be negative");
        this.actualQuantity = requireNonNegativeIfPresent(actualQuantity, "Actual quantity must not be negative");
        this.systemUnit = systemUnit;

        if (this.quantity == null
                && this.expectedQuantity == null
                && this.actualQuantity == null
                && this.systemUnit == null) {
            throw new BadRequestException("At least one measured quantity value is required");
        }
    }

    private static Integer requireNonNegativeIfPresent(
            Integer value,
            String message) {

        if (value != null && value < 0) {
            throw new BadRequestException(message);
        }
        return value;
    }
}
