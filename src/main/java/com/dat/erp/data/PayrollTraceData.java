package com.dat.erp.data;

import com.dat.erp.constants.PayrollStatus;
import com.dat.erp.utils.ErrorUtils;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PayrollTraceData {

    private final String payrollRunCode;
    private final String payrollResultCode;
    private final String rerunBatchCode;
    private final PayrollStatus sourceType;

    public PayrollTraceData(
            String payrollRunCode,
            String payrollResultCode,
            String rerunBatchCode,
            PayrollStatus sourceType) {

        this.payrollRunCode = payrollRunCode == null ? null
                : ErrorUtils.requireNotBlank(payrollRunCode, "Payroll run code must not be blank");
        this.payrollResultCode = payrollResultCode == null ? null
                : ErrorUtils.requireNotBlank(payrollResultCode, "Payroll result code must not be blank");
        this.rerunBatchCode = rerunBatchCode == null ? null
                : ErrorUtils.requireNotBlank(rerunBatchCode, "Rerun batch code must not be blank");
        this.sourceType = sourceType;
    }
}
