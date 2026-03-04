package com.dat.erp.services;

import java.util.List;

import com.dat.erp.dto.request.SalaryRequest;
import com.dat.erp.dto.response.PagedResponse;
import com.dat.erp.dto.response.SalaryResponse;
import com.dat.erp.dto.response.SelectionOptionResponse;

public interface SalaryService {
    List<SalaryResponse> createSalaries(List<SalaryRequest> requests);

    PagedResponse<SelectionOptionResponse> getSalaryOptionsByCompanyCode(String name, Integer page, Integer size,
            String sortBy, String sortDir);
}
