package com.dat.erp.services;

import com.dat.erp.dto.request.SalaryTemplateRequest;
import com.dat.erp.dto.response.SalaryTemplateResponse;

public interface SalaryTemplateService {
    SalaryTemplateResponse createSalaryTemplate(SalaryTemplateRequest request);
}

