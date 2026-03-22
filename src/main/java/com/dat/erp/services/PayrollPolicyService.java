package com.dat.erp.services;

import java.util.List;

import com.dat.erp.dto.request.payroll.PayrollPolicyRequest;
import com.dat.erp.dto.response.payroll.PayrollPolicyResponse;

public interface PayrollPolicyService {
    PayrollPolicyResponse create(PayrollPolicyRequest request);

    PayrollPolicyResponse update(String code, PayrollPolicyRequest request);

    PayrollPolicyResponse get(String code);

    List<PayrollPolicyResponse> list();

    void delete(String code);
}
