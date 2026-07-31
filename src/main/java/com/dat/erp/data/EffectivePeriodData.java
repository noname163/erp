package com.dat.erp.data;

import java.time.LocalDate;

import com.dat.erp.exceptions.BadRequestException;
import com.dat.erp.utils.ErrorUtils;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EffectivePeriodData {

    private final LocalDate effectiveFrom;
    private final LocalDate effectiveTo;

    public EffectivePeriodData(
            LocalDate effectiveFrom,
            LocalDate effectiveTo) {

        this.effectiveFrom = ErrorUtils.requireNonNull(
                effectiveFrom,
                "Effective from date is required");
        this.effectiveTo = ErrorUtils.requireNonNull(
                effectiveTo,
                "Effective to date is required");

        if (effectiveTo.isBefore(effectiveFrom)) {
            throw new BadRequestException(
                    "Effective to date must not be before effective from date");
        }
    }
}
