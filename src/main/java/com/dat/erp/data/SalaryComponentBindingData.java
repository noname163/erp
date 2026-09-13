package com.dat.erp.data;

import com.dat.erp.entities.EmployeeSalary;
import com.dat.erp.entities.Salary;
import com.dat.erp.entities.SalaryTemplate;
import com.dat.erp.utils.ErrorUtils;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SalaryComponentBindingData {

    private final Salary salary;
    private final SalaryTemplate salaryTemplate;
    private final EmployeeSalary employeeSalary;
    private final Salary dependenceCode;
    private final Boolean fixed;

    public SalaryComponentBindingData(
            Salary salary,
            SalaryTemplate salaryTemplate,
            EmployeeSalary employeeSalary,
            Salary dependenceCode,
            Boolean fixed) {

        this.salary = salary;
        this.salaryTemplate = salaryTemplate;
        this.employeeSalary = employeeSalary;
        this.dependenceCode = dependenceCode;
        this.fixed = fixed == null ? Boolean.FALSE : fixed;

        if (salary == null && salaryTemplate == null && employeeSalary == null) {
            ErrorUtils.requireNonNull(salary, "Salary component binding is required");
        }
    }
}
