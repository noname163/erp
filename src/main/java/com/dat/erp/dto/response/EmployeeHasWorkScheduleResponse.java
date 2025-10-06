package com.dat.erp.dto.response;

import java.time.LocalDate;

import lombok.Data;

@Data
public class EmployeeHasWorkScheduleResponse {
    private Long id;
    private String code;
    private String employeeCode;
    private String employeeName;
    private String scheduleCode;
    private String shiftType;
    private LocalDate date;
}
