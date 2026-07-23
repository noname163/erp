package com.dat.erp.services.salary.calculation.hour;

import java.time.YearMonth;
import java.util.Map;

import com.dat.erp.constants.DayType;
import com.dat.erp.entities.PayrollPolicy;

public record ExpectedWorkingHourContext(PayrollPolicy payrollPolicy, YearMonth month,
        Map<DayType, Integer> dayTypeCounts) {
}
