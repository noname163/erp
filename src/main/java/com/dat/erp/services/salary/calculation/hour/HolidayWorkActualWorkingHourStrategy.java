package com.dat.erp.services.salary.calculation.hour;

import org.springframework.stereotype.Component;

import com.dat.erp.constants.DayType;

@Component
public class HolidayWorkActualWorkingHourStrategy extends SummingActualWorkingHourStrategySupport {

    @Override
    public DayType supports() {
        return DayType.HOLIDAY_WORK;
    }
}
