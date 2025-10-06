package com.dat.erp.dto.request;

import java.util.List;

import com.dat.erp.constants.WorkScheduleEnum;

import lombok.Data;

@Data
public class EmployeeHasWorkScheduleRequest {
    private String code;
    private List<String> codes;
    private WorkScheduleEnum type;
}
