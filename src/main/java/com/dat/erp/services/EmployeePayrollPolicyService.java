package com.dat.erp.services;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import com.dat.erp.dto.request.EmployeePayrollPolicyRequest;
import com.dat.erp.dto.response.EmployeePayrollPolicyResponse;
import com.dat.erp.entities.PayrollPolicy;

public interface EmployeePayrollPolicyService {
    EmployeePayrollPolicyResponse createEmployeePayrollPolicy(EmployeePayrollPolicyRequest request);

    EmployeePayrollPolicyResponse deactivateEmployeePayrollPolicy(String code);

    List<EmployeePayrollPolicyResponse> getEmployeePayrollPolicies(String userProfileCode);

    Map<String, PayrollPolicy> getCompanyPoliciesByEmployeeCodesAndDate(List<String> employeeCodes, LocalDate date);
}
