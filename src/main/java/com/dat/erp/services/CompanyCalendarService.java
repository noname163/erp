package com.dat.erp.services;

import java.util.List;

import com.dat.erp.dto.request.payroll.CompanyCalendarRequest;
import com.dat.erp.dto.response.payroll.CompanyCalendarResponse;

public interface CompanyCalendarService {
    CompanyCalendarResponse create(CompanyCalendarRequest request);

    CompanyCalendarResponse update(String code, CompanyCalendarRequest request);

    CompanyCalendarResponse get(String code);

    List<CompanyCalendarResponse> list();

    void delete(String code);
}
