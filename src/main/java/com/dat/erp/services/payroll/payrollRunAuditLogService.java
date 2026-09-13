package com.dat.erp.services.payroll;

import com.dat.erp.dto.request.PayrollRunAuditLogRequest;
import com.dat.erp.entities.PayrollRun;

public interface PayrollRunAuditLogService {
    public void writeAudit(PayrollRun payrollRun, PayrollRunAuditLogRequest request);
}
