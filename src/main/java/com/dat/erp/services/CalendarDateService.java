package com.dat.erp.services;

import java.util.List;

import com.dat.erp.dto.request.CompanyCalendarDateRequest;
import com.dat.erp.dto.response.CompanyCalendarDateResponse;
import com.dat.erp.entities.CalendarDate;
import com.dat.erp.entities.CompanyCalendar;

public interface CalendarDateService {
    List<CalendarDate> createCalendarDates(List<CompanyCalendarDateRequest> requests, CompanyCalendar calendar);

    List<CompanyCalendarDateResponse> getCompanyCalendarDates(CompanyCalendar calendar, Integer year);
}
