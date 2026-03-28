package com.dat.erp.services;

import com.dat.erp.dto.request.CompanyCalendarRequest;
import com.dat.erp.dto.response.CompanyCalendarDateResponse;
import com.dat.erp.dto.response.CompanyCalendarListResponse;
import com.dat.erp.dto.response.PagedResponse;
import com.dat.erp.dto.response.CompanyCalendarResponse;

import java.util.List;

public interface CompanyCalendarService {
    CompanyCalendarResponse createCompanyCalendar(CompanyCalendarRequest request);

    PagedResponse<CompanyCalendarListResponse> getCompanyCalendars(
            String name,
            String region,
            String timeZone,
            Integer page,
            Integer size,
            String sortBy,
            String sortDir);

    List<CompanyCalendarDateResponse> getCompanyCalendarDates(String code, Integer year);
}
