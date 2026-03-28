package com.dat.erp.services;

import com.dat.erp.dto.request.PayrollPolicyRequest;
import com.dat.erp.dto.response.PayrollPolicyResponse;

public interface PayrollPolicyService {
    PayrollPolicyResponse createPayrollPolicy(PayrollPolicyRequest request);
}
