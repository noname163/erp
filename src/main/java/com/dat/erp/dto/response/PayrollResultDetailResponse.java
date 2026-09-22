package com.dat.erp.dto.response;

import java.math.BigDecimal;

import com.dat.erp.constants.PayrollResultCalcBasis;

import lombok.Data;

@Data
public class PayrollResultDetailResponse {
    private String code;
    private String category;
    private String label;
    private BigDecimal quantity;
    private BigDecimal rate;
    private String currency;
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
