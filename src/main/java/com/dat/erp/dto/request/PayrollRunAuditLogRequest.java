package com.dat.erp.dto.request;

import com.dat.erp.constants.PayrollRunAuditActionType;
import com.dat.erp.constants.PayrollRunAuditStatus;
import com.dat.erp.entities.PayrollResult;

public record PayrollRunAuditLogRequest(
        String rerunBatchCode,
        PayrollRunAuditActionType actionType,
        String reason,
        String employeeCode,
        PayrollResult oldResult,
        PayrollResult newResult,
        PayrollRunAuditStatus status,
        String errorMessage) {
}
