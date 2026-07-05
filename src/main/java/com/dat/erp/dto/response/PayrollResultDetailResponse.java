package com.dat.erp.dto.response;

import java.math.BigDecimal;

import com.dat.erp.constants.PayrollResultCalcBasis;

import lombok.Data;

@Data
public class PayrollResultDetailResponse {
    private String code;
    private PayrollResultCalcBasis calcBasis;
    private BigDecimal basisDays;
    private BigDecimal paidDays;
    private BigDecimal unpaidDays;
    private BigDecimal basisHours;
    private BigDecimal ratePerDay;
    private BigDecimal amount;
    private BigDecimal multiplierApplied;
    private String formulaNote;
}
