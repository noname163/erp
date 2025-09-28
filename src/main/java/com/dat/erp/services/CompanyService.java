package com.dat.erp.services;

import com.dat.erp.dto.request.CompanyRequest;
import com.dat.erp.dto.response.CompanyResponse;
import com.dat.erp.dto.response.PagedResponse;

public interface CompanyService {
    public String createCompany(CompanyRequest companyRequest);

    public PagedResponse<CompanyResponse> getCompanies(String searchKey, String searchValue, Integer page, Integer size,
            String sortBy, String sortDir);
}
