package com.dat.erp.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDailyWorkListResponse {
    private String employeeCode;
    private String employeeName;
    private LocalDate logDay;
    private LocalTime startTime;
    private LocalTime endTime;
    private String createdBy;
    private String editedBy;
    private Integer otTime;
    private Boolean usedPto;
    private String workType;
}
