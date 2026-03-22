package com.dat.erp.services;

import java.math.BigDecimal;
import java.util.List;

import com.dat.erp.constants.PayrollSummaryStatus;
import com.dat.erp.entities.UserProfile;

public record PayrollEmployeeDraft(
        UserProfile userProfile,
        String currency,
        BigDecimal grossAmount,
        BigDecimal deductionAmount,
        BigDecimal netAmount,
        PayrollSummaryStatus status,
        boolean hasBlockingIssue,
        String issueMessage,
        List<PayrollLineDraft> lines) {
}
