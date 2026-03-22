package com.dat.erp.dto.response.payroll;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.dat.erp.constants.ApprovalStatus;
import com.dat.erp.constants.PayrollAdjustmentType;

public record PayrollAdjustmentResponse(
        String code,
        String userProfileCode,
        String salaryCode,
        PayrollAdjustmentType adjustmentType,
        ApprovalStatus approvalStatus,
        String amount,
        BigDecimal quantity,
        String unitCode,
        String reason,
        String sourceMonth,
        String effectivePayrollMonth,
        Boolean isRetro,
        String referenceCode,
        LocalDate effectiveDate) {
}
