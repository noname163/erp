package com.dat.erp.dto.response;

import java.math.BigDecimal;

import com.dat.erp.entities.PayrollResult;
import com.dat.erp.utils.CompanySecretKeyCryptoUtils;

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

    public static PayrollRerunEmployeeResultResponse buildEmployeeResponse(
            String employeeCode,
            PayrollResult oldResult,
            PayrollResult newResult,
            String companySecretKey) {
        BigDecimal oldActualAmount = CompanySecretKeyCryptoUtils.decryptAmount(oldResult.getActualAmount(),
                companySecretKey);
        BigDecimal newActualAmount = CompanySecretKeyCryptoUtils.decryptAmount(newResult.getActualAmount(),
                companySecretKey);
        BigDecimal oldExpectedAmount = CompanySecretKeyCryptoUtils.decryptAmount(oldResult.getExpectedAmount(),
                companySecretKey);
        BigDecimal newExpectedAmount = CompanySecretKeyCryptoUtils.decryptAmount(newResult.getExpectedAmount(),
                companySecretKey);
        return new PayrollRerunEmployeeResultResponse(
                employeeCode,
                "SUCCESS",
                oldActualAmount,
                newActualAmount,
                newActualAmount.subtract(oldActualAmount),
                oldExpectedAmount,
                newExpectedAmount,
                oldResult.getCode(),
                newResult.getCode());
    }
}
