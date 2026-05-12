package com.dat.erp.services.salary.calculation.hour;

import java.math.BigDecimal;
import java.util.List;

import com.dat.erp.entities.DailyWork;

abstract class SummingActualWorkingHourStrategySupport implements ActualWorkingHourStrategy {

    @Override
    public BigDecimal calculate(List<DailyWork> worksForDayType) {
        return worksForDayType.stream()
                .map(DailyWork::getHoursWorked)
                .map(hours -> hours == null ? BigDecimal.ZERO : hours)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
