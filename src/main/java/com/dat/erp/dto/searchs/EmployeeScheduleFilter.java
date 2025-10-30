package com.dat.erp.dto.searchs;

import java.time.LocalDate;

public record EmployeeScheduleFilter(
        LocalDate startDate,
        LocalDate endDate,
        String companyCode,
        String shiftType) {
}
