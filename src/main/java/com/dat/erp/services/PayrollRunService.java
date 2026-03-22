package com.dat.erp.services;

import com.dat.erp.dto.request.payroll.PayrollRunPreviewRequest;
import com.dat.erp.dto.response.payroll.EmployeePayslipResponse;
import com.dat.erp.dto.response.payroll.PayrollRunDetailResponse;
import com.dat.erp.dto.response.payroll.PayrollRunResponse;

public interface PayrollRunService {
    PayrollRunResponse createPreview(PayrollRunPreviewRequest request);

    PayrollRunResponse finalizeRun(String runCode);

    PayrollRunResponse replaySnapshot(String runCode);

    PayrollRunDetailResponse getRun(String runCode);

    EmployeePayslipResponse getPayslip(String runCode, String userProfileCode);

    EmployeePayslipResponse getPayslipForCurrentUser(String runCode);
}
