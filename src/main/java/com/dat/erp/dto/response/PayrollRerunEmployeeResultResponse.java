package com.dat.erp.dto.response;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PayrollRerunEmployeeResultResponse {
    private String employeeCode;
    private String status;
    private BigDecimal oldActualAmount;
    private BigDecimal newActualAmount;
    private BigDecimal differenceAmount;
    private BigDecimal oldExpectedAmount;
    private BigDecimal newExpectedAmount;
    private String oldPayrollResultCode;
    private String newPayrollResultCode;
}
