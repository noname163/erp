package com.dat.erp.services;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import com.dat.erp.dto.request.EmployeeDailyWorkRequest;
import com.dat.erp.dto.response.EmployeeDailyWorkListResponse;
import com.dat.erp.dto.response.PagedResponse;
import com.dat.erp.dto.response.salary.DailyWorkForSalaryResponse;

public interface EmployeeDailyWorkService {
    String createEmployeeDailyWorks(List<EmployeeDailyWorkRequest> requests);

    PagedResponse<EmployeeDailyWorkListResponse> getEmployeeDailyWorks(
            String employeeCode,
            LocalDate startDate,
            LocalDate endDate,
            Boolean isPto,
            Integer page,
            Integer size,
            String sortBy,
            String sortDir);

    Map<String, List<DailyWorkForSalaryResponse>> getEmployeeDailyWorksByEmployeeCodes(List<String> employeeCodes);
}
