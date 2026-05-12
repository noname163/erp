package com.dat.erp.services.salary.calculation.hour;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.dat.erp.constants.DayType;

@Component
public class NormalExpectedWorkingHourStrategy implements ExpectedWorkingHourStrategy {

    @Override
    public DayType supports() {
        return DayType.NORMAL;
    }

    @Override
    public BigDecimal calculate(ExpectedWorkingHourContext context) {
        Integer standardHoursPerDay = context.payrollPolicy().getStandardQuantityPerDay();
        if (standardHoursPerDay == null || standardHoursPerDay <= 0) {
            return BigDecimal.ZERO;
        }

        int normalWorkingDays = context.dayTypeCounts().getOrDefault(DayType.NORMAL, 0);
        return BigDecimal.valueOf((long) standardHoursPerDay * normalWorkingDays);
    }
}
