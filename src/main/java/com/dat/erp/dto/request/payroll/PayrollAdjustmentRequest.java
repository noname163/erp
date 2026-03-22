package com.dat.erp.dto.request.payroll;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.dat.erp.constants.ApprovalStatus;
import com.dat.erp.constants.PayrollAdjustmentType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PayrollAdjustmentRequest(
        @NotBlank String userProfileCode,
        String salaryCode,
        @NotNull PayrollAdjustmentType adjustmentType,
        @NotNull ApprovalStatus approvalStatus,
        @NotBlank String amount,
        BigDecimal quantity,
        String unitCode,
        @NotBlank String reason,
        String sourceMonth,
        @NotBlank String effectivePayrollMonth,
        Boolean isRetro,
        String referenceCode,
        LocalDate effectiveDate) {
}
