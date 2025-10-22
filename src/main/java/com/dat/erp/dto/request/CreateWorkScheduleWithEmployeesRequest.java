package com.dat.erp.dto.request;

import java.util.List;

import lombok.Data;

/**
 * Request payload to create a work schedule and assign employees to it.
 */
@Data
public class CreateWorkScheduleWithEmployeesRequest {
    private WorkScheduleRequest workSchedule;
    private List<String> employeeCodes;
}

