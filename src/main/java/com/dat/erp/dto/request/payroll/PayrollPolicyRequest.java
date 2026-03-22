package com.dat.erp.dto.request.payroll;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import com.dat.erp.constants.PayrollProrationBasis;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PayrollPolicyRequest(
        @NotBlank String name,
        @NotNull PayrollProrationBasis prorationBasis,
        Integer standardDaysPerWeek,
        Boolean payHolidayIfOff,
        String roundingRule,
        Integer standardHoursPerDay,
        Integer standardMinutesPerDay,
        String roundingMode,
        String roundAt,
        String zeroDenominatorAction,
        String holidayWeekendOverlapRule,
        String approvalMode,
        Boolean freezeSnapshotRequired,
        LocalTime nightPremiumStart,
        LocalTime nightPremiumEnd,
        Boolean otRequiresApproval,
        Integer otMinimumMinutes,
        Integer otRoundingMinutes,
        @NotNull LocalDate effectiveFrom,
        @NotNull LocalDate effectiveTo,
        @Valid List<PayRateRuleRequest> rateRules) {
}
