package com.dat.erp.dto.response.payroll;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.dat.erp.constants.PayrollLineType;
import com.dat.erp.constants.PayrollSourceType;

public record PayrollLineResponse(
        String salaryCode,
        String salaryName,
        PayrollLineType lineType,
        PayrollSourceType sourceType,
        String amount,
        BigDecimal quantity,
        String currency,
        BigDecimal rate,
        BigDecimal multiplier,
        LocalDate segmentFrom,
        LocalDate segmentTo,
        Boolean isRetro,
        String retroReason,
        String formulaNote) {
}
