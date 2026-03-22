package com.dat.erp.services;

import java.util.List;

import com.dat.erp.dto.request.payroll.PayrollAdjustmentRequest;
import com.dat.erp.dto.response.payroll.PayrollAdjustmentResponse;

public interface PayrollAdjustmentService {
    PayrollAdjustmentResponse create(PayrollAdjustmentRequest request);

    PayrollAdjustmentResponse update(String code, PayrollAdjustmentRequest request);

    PayrollAdjustmentResponse get(String code);

    List<PayrollAdjustmentResponse> list();

    void delete(String code);
}
