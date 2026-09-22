package com.dat.erp.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PayrollPolicyResponse {
    private String code;
    private String name;
    @jakarta.validation.Valid
    private com.dat.erp.data.PayrollStatutorySettings statutorySettings;
    private Integer standardQuantityPerDay;
    private String unitCode;
    private LocalTime standardStartTime;
    private LocalTime standardEndTime;
    private String roundingRule;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
}
