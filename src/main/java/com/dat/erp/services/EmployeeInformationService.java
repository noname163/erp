package com.dat.erp.services;

import org.springframework.stereotype.Service;

import com.dat.erp.dto.request.EmployeeInformationRequest;
import com.dat.erp.dto.response.EmployeeInformationResponse;
import com.dat.erp.dto.response.PagedResponse;

@Service
public interface EmployeeInformationService {
    public String createEmployeeInformation(EmployeeInformationRequest request);

    public PagedResponse<EmployeeInformationResponse> getListEmployeeInformationResponse(String searchKey,
            String searchValue, Integer page, Integer pageSize, String sortBy, String sortDir);
}
