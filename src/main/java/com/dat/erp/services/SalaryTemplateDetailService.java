package com.dat.erp.services;

import java.util.List;

import com.dat.erp.dto.request.SalaryTemplateDetailRequest;
import com.dat.erp.dto.response.SalaryTemplateDetailListResponse;
import com.dat.erp.entities.SalaryTemplate;
import com.dat.erp.entities.SalaryTemplateDetail;

public interface SalaryTemplateDetailService {
    List<SalaryTemplateDetail> createSalaryTemplateDetails(List<SalaryTemplateDetailRequest> requests,
            SalaryTemplate salaryTemplate);

    List<SalaryTemplateDetailListResponse> getSalaryTemplateDetails(String salaryTemplateCode);
}

