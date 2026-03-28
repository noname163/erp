package com.dat.erp.repositories.projections;

import com.dat.erp.constants.DayType;

public interface CalendarDateDayTypeCountProjection {
    DayType getDayType();

    long getTotalDates();
}
