package com.dat.erp.repositories.projections;

import java.time.LocalDateTime;
import java.time.YearMonth;

import com.dat.erp.constants.PayrollStatus;

public interface PayrollResultListProjection {
    String getPayrollResultCode();
    String getPayrollRunCode();

    String getSalaryName();

    String getExpectedAmount();

    String getEmployeeName();

    String getActualAmount();

    String getCurrency();

    Integer getExpectedQuantity();

    Integer getActualQuantity();

    String getUnitName();

    PayrollStatus getSourceType();

    Boolean getIsRetro();

    String getRetroReason();

    YearMonth getPeriod();

    LocalDateTime getCreatedAt();

    String getEmployeeCode();
}
