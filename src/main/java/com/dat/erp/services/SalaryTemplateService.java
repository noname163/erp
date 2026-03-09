package com.dat.erp.services;

import java.time.LocalDate;

import com.dat.erp.dto.request.SalaryTemplateRequest;
import com.dat.erp.dto.response.PagedResponse;
import com.dat.erp.dto.response.SalaryTemplateListResponse;
import com.dat.erp.dto.response.SalaryTemplateResponse;

public interface SalaryTemplateService {
    SalaryTemplateResponse createSalaryTemplate(SalaryTemplateRequest request);

    PagedResponse<SalaryTemplateListResponse> getSalaryTemplates(String name, String currency, LocalDate effectiveFrom,
            LocalDate effectiveTo, Integer page, Integer size, String sortBy, String sortDir);
}

