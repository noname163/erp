package com.dat.erp.services.payroll;

import java.time.LocalDate;
import java.util.List;

import com.dat.erp.dto.request.PayrollPolicyRequest;
import com.dat.erp.dto.response.PayrollPolicyResponse;

public interface PayrollPolicyService {
    List<PayrollPolicyResponse> getPayrollPolicies(String name, LocalDate effectiveFrom, LocalDate effectiveTo, String unitCode);

    PayrollPolicyResponse createPayrollPolicy(PayrollPolicyRequest request);
}
