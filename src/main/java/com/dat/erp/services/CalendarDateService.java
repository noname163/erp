package com.dat.erp.services;

import java.time.YearMonth;
import java.util.List;
import java.util.Map;

import com.dat.erp.constants.DayType;
import com.dat.erp.dto.request.CompanyCalendarDateRequest;
import com.dat.erp.dto.response.CompanyCalendarDateResponse;
import com.dat.erp.entities.CalendarDate;
import com.dat.erp.entities.CompanyCalendar;

public interface CalendarDateService {
    List<CalendarDate> createCalendarDates(List<CompanyCalendarDateRequest> requests, CompanyCalendar calendar);

    List<CalendarDate> replaceCalendarDates(List<CompanyCalendarDateRequest> requests, CompanyCalendar calendar);

    List<CompanyCalendarDateResponse> getCompanyCalendarDates(CompanyCalendar calendar);

    Map<DayType, Integer> getCalendarDateTotalsByCompanyCodeAndMonth(String companyCode, YearMonth month);
}
