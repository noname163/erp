package com.dat.erp.dto.response;

import java.time.LocalDateTime;

import com.dat.erp.constants.PayrollStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PayrollResultListResponse {
    private String payrollRunCode;
    private String salaryName;
    private String expectedAmount;
    private String employeeName;
    private String actualAmount;
    private String currency;
    private Integer expectedQuantity;
    private Integer actualQuantity;
    private String unitName;
    private PayrollStatus sourceType;
    private Boolean isRetro;
    private String retroReason;
    private String period;
    private LocalDateTime createdAt;
    private String employeeCode;
}
