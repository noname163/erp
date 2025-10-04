package com.dat.erp.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.dat.erp.dto.request.EmployeeInformationRequest;
import com.dat.erp.dto.response.EmployeeInformationDetailResponse;
import com.dat.erp.dto.response.EmployeeInformationResponse;
import com.dat.erp.dto.response.PagedResponse;

@Service
public interface EmployeeInformationService {
    public String createEmployeeInformation(EmployeeInformationRequest request);

    public PagedResponse<EmployeeInformationResponse> getListEmployeeInformationResponse(String companyCode,
            String deparmentCode, String managerCode, String keyword, Integer page, Integer pageSize, String sortBy,
            String sortDir);

    public EmployeeInformationDetailResponse employeeInformationDetailResponse(String code);

    public String updateEmployeeInformation(EmployeeInformationRequest request, String code);

    public String deleteEmployeeByCode(String code);

    public List<String> deleteEmployeeByCodes(List<String> codes);
}
