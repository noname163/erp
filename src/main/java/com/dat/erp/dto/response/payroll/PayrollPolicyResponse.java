package com.dat.erp.dto.response.payroll;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import com.dat.erp.constants.PayrollProrationBasis;
import com.dat.erp.dto.request.payroll.PayRateRuleRequest;

public record PayrollPolicyResponse(
        String code,
        String name,
        PayrollProrationBasis prorationBasis,
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
        LocalDate effectiveFrom,
        LocalDate effectiveTo,
        List<PayRateRuleRequest> rateRules) {
}
