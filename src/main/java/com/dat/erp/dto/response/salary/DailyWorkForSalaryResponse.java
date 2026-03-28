package com.dat.erp.dto.response.salary;

import com.dat.erp.constants.DayType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DailyWorkForSalaryResponse {
    private DayType dayType;
    private int totalWorkHours;
}
