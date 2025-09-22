package com.dat.erp.services;

import java.util.List;

import com.dat.erp.dto.request.CompanyRequest;
import com.dat.erp.dto.response.CompanyResponse;

public interface CompanyService {
    public String createCompany(CompanyRequest companyRequest);

    public List<CompanyResponse> getCompanies(Integer page, Integer size);
}
