package com.dat.erp.dto.response.salary;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

import com.dat.erp.constants.DayType;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MonthlySalaryCalculationResponse {
    private String employeeCode;
    private YearMonth month;
    private BigDecimal expectedWorkingHourPerMonth;
    private BigDecimal actualWorkingHourPerMonth;
    private BigDecimal standardMoneyPerHour;
    private BigDecimal finalSalary;
    private Map<DayType, BigDecimal> actualHoursByDayType;
    private List<MonthlySalaryDetailAuditResponse> auditTrail;
}
