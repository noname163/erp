package com.dat.erp.dto.response.payroll;

import java.time.LocalDate;
import java.util.List;

import com.dat.erp.dto.request.payroll.CalendarDateRequest;

public record CompanyCalendarResponse(
        String code,
        String name,
        LocalDate effectiveFrom,
        LocalDate effectiveTo,
        List<CalendarDateRequest> dates) {
}
