package com.dat.erp.repositories.projections;

import java.time.LocalDate;
import java.time.LocalDateTime;

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

    String getWorkType();
}
