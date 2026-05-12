package com.dat.erp.services.salary.calculation.hour;

import java.math.BigDecimal;
import java.util.List;

import com.dat.erp.constants.DayType;
import com.dat.erp.entities.DailyWork;

public interface ActualWorkingHourStrategy {
    DayType supports();

    BigDecimal calculate(List<DailyWork> worksForDayType);
}
