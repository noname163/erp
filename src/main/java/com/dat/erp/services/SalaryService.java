package com.dat.erp.services;

import java.util.List;

import com.dat.erp.dto.request.SalaryRequest;
import com.dat.erp.dto.response.SalaryResponse;

public interface SalaryService {
    List<SalaryResponse> createSalaries(List<SalaryRequest> requests);
}

