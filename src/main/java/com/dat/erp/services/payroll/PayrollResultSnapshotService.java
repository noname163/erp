package com.dat.erp.services.payroll;

import com.dat.erp.entities.PayrollResult;
import com.dat.erp.entities.PayrollRun;

public interface PayrollResultSnapshotService {
    public void createSnapshot(PayrollRun payrollRun, PayrollResult oldResult, String rerunBatchCode,
            String employeeCode);
}
