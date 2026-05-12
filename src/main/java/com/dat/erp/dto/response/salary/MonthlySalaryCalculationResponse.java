package com.dat.erp.dto.response.salary;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

import com.dat.erp.constants.DayType;
import com.dat.erp.constants.SalaryBasisType;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MonthlySalaryCalculationResponse {
    private String employeeCode;
    private YearMonth month;
    private BigDecimal expectedWorkingHourPerMonth;
    private BigDecimal actualWorkingHourPerMonth;
    private BigDecimal standardMoneyPerHour;
    private BigDecimal finalSalary;
    private Map<DayType, BigDecimal> actualHoursByDayType;
    private List<MonthlySalaryDetailAuditResponse> auditTrail;
    private SalaryBasisType salaryBasisType;
    private String basisUnit;
    private BigDecimal expectedBasisValue;
    private BigDecimal actualBasisValue;
    private BigDecimal standardMoneyPerUnit;
    private Map<String, BigDecimal> actualBasisValuesByType;

    public MonthlySalaryCalculationResponse(String employeeCode, YearMonth month,
            BigDecimal expectedWorkingHourPerMonth, BigDecimal actualWorkingHourPerMonth,
            BigDecimal standardMoneyPerHour, BigDecimal finalSalary,
            Map<DayType, BigDecimal> actualHoursByDayType, List<MonthlySalaryDetailAuditResponse> auditTrail) {
        this(employeeCode, month, expectedWorkingHourPerMonth, actualWorkingHourPerMonth, standardMoneyPerHour,
                finalSalary, actualHoursByDayType, auditTrail, SalaryBasisType.WORKING_HOUR, "HOUR",
                expectedWorkingHourPerMonth, actualWorkingHourPerMonth, standardMoneyPerHour,
                actualHoursByDayType == null ? null : actualHoursByDayType.entrySet().stream()
                        .collect(java.util.stream.Collectors.toMap(entry -> entry.getKey().name(), Map.Entry::getValue)));
    }

    public MonthlySalaryCalculationResponse(String employeeCode, YearMonth month,
            BigDecimal expectedWorkingHourPerMonth, BigDecimal actualWorkingHourPerMonth,
            BigDecimal standardMoneyPerHour, BigDecimal finalSalary,
            Map<DayType, BigDecimal> actualHoursByDayType, List<MonthlySalaryDetailAuditResponse> auditTrail,
            SalaryBasisType salaryBasisType, String basisUnit, BigDecimal expectedBasisValue,
            BigDecimal actualBasisValue, BigDecimal standardMoneyPerUnit,
            Map<String, BigDecimal> actualBasisValuesByType) {
        this.employeeCode = employeeCode;
        this.month = month;
        this.expectedWorkingHourPerMonth = expectedWorkingHourPerMonth;
        this.actualWorkingHourPerMonth = actualWorkingHourPerMonth;
        this.standardMoneyPerHour = standardMoneyPerHour;
        this.finalSalary = finalSalary;
        this.actualHoursByDayType = actualHoursByDayType;
        this.auditTrail = auditTrail;
        this.salaryBasisType = salaryBasisType;
        this.basisUnit = basisUnit;
        this.expectedBasisValue = expectedBasisValue;
        this.actualBasisValue = actualBasisValue;
        this.standardMoneyPerUnit = standardMoneyPerUnit;
        this.actualBasisValuesByType = actualBasisValuesByType;
    }
}
