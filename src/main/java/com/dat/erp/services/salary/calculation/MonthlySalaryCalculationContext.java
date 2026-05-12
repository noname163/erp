package com.dat.erp.services.salary.calculation;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

import com.dat.erp.constants.DayType;
import com.dat.erp.constants.SalaryBasisType;
import com.dat.erp.dto.response.salary.MonthlySalaryDetailAuditResponse;
import com.dat.erp.services.salary.calculation.basis.SalaryBasisCalculationResult;
import com.dat.erp.entities.EmployeeSalary;
import com.dat.erp.entities.EmployeeSalaryDetail;
import com.dat.erp.entities.PayrollPolicy;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MonthlySalaryCalculationContext {
    private String employeeCode;
    private String companyCode;
    private YearMonth month;
    private EmployeeSalary employeeSalary;
    private PayrollPolicy payrollPolicy;
    private List<EmployeeSalaryDetail> details;
    private Map<DayType, BigDecimal> actualHoursByDayType;
    private BigDecimal expectedWorkingHourPerMonth;
    private BigDecimal actualWorkingHourPerMonth;
    private BigDecimal standardMoneyPerHour;
    private BigDecimal finalSalary;
    private List<MonthlySalaryDetailAuditResponse> auditTrail;
    private SalaryBasisCalculationResult salaryBasisCalculationResult;

    public MonthlySalaryCalculationContext(String employeeCode, String companyCode, YearMonth month) {
        this.employeeCode = employeeCode;
        this.companyCode = companyCode;
        this.month = month;
    }

    public SalaryBasisType getSalaryBasisType() {
        if (employeeSalary == null || employeeSalary.getSalaryBasisType() == null) {
            return SalaryBasisType.WORKING_HOUR;
        }
        return employeeSalary.getSalaryBasisType();
    }
}
