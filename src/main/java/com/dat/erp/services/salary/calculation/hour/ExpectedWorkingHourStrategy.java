package com.dat.erp.services.salary.calculation.hour;

import java.math.BigDecimal;

import com.dat.erp.constants.DayType;

public interface ExpectedWorkingHourStrategy {
    DayType supports();

    BigDecimal calculate(ExpectedWorkingHourContext context);
}
