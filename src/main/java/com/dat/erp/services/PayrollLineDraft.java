package com.dat.erp.services;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.dat.erp.constants.PayrollLineType;
import com.dat.erp.constants.PayrollResultCalcBasis;
import com.dat.erp.constants.PayrollSourceType;
import com.dat.erp.entities.Salary;

public record PayrollLineDraft(
        Salary salary,
        PayrollLineType lineType,
        PayrollSourceType sourceType,
        BigDecimal amount,
        BigDecimal quantity,
        String currency,
        BigDecimal rate,
        BigDecimal multiplier,
        LocalDate segmentFrom,
        LocalDate segmentTo,
        boolean retro,
        boolean manual,
        String retroReason,
        String sourceRefCode,
        String formulaNote,
        PayrollResultCalcBasis calcBasis,
        String policyRuleCode,
        LocalDate sourceDate) {
}
