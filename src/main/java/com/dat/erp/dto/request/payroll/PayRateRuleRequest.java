package com.dat.erp.dto.request.payroll;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import com.dat.erp.constants.EmploymentType;
import com.dat.erp.constants.PayRateAppliesTo;
import com.dat.erp.constants.PayRateDayType;
import com.dat.erp.constants.PayrollRateRuleType;

import jakarta.validation.constraints.NotNull;

public record PayRateRuleRequest(
        String rateName,
        @NotNull PayrollRateRuleType rateType,
        @NotNull PayRateDayType dayType,
        @NotNull BigDecimal multiplier,
        @NotNull PayRateAppliesTo appliesTo,
        Integer priority,
        LocalTime startTime,
        LocalTime endTime,
        Integer minimumMinutes,
        Integer roundingMinutes,
        Boolean requiresApproval,
        String departmentCode,
        String locationCode,
        EmploymentType employmentType,
        String salaryCode,
        Boolean isStackable,
        LocalDate effectiveFrom,
        LocalDate effectiveTo) {
}
