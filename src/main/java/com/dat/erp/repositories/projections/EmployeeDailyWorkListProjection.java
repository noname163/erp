package com.dat.erp.repositories.projections;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.dat.erp.constants.DayType;

public interface EmployeeDailyWorkListProjection {
    String getEmployeeCode();

    String getEmployeeName();

    LocalDate getLogDay();

    LocalDateTime getStartTime();

    LocalDateTime getEndTime();

    String getCreatedByName();

    String getEditedByName();

    Integer getOtTime();

    Boolean getUsedPto();

    DayType getWorkType();
}
