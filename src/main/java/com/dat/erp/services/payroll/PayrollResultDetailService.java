package com.dat.erp.services.payroll;

import java.util.List;

import com.dat.erp.entities.PayrollResult;
import com.dat.erp.entities.PayrollResultDetail;

public interface PayrollResultDetailService {
    public void calculatePayrollDetails();

    void replacePayrollResultDetailsBestEffort(
            String operation,
            PayrollResult payrollResult,
            List<PayrollResultDetail> newDetails);
}
