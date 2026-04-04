package com.dat.erp.repositories.projections;

import java.time.LocalDateTime;

import com.dat.erp.constants.PayrollStatus;

public interface PayrollResultListProjection {
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

    String getPeriod();

    LocalDateTime getCreatedAt();

    String getEmployeeCode();
}
