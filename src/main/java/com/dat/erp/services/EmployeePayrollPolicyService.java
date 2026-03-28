package com.dat.erp.services;

import java.util.List;

import com.dat.erp.dto.request.EmployeePayrollPolicyRequest;
import com.dat.erp.dto.response.EmployeePayrollPolicyResponse;

public interface EmployeePayrollPolicyService {
    EmployeePayrollPolicyResponse createEmployeePayrollPolicy(EmployeePayrollPolicyRequest request);

    EmployeePayrollPolicyResponse deactivateEmployeePayrollPolicy(String code);

    List<EmployeePayrollPolicyResponse> getEmployeePayrollPolicies(String userProfileCode);
}
