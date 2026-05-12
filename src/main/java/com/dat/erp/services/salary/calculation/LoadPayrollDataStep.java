package com.dat.erp.services.salary.calculation;

import java.time.LocalDate;
import java.util.List;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.dat.erp.constants.Messages;
import com.dat.erp.entities.EmployeeSalary;
import com.dat.erp.entities.PayrollPolicy;
import com.dat.erp.exceptions.ResourceNotFoundException;
import com.dat.erp.repositories.customrepositories.EmployeeSalaryDetailRepository;
import com.dat.erp.repositories.customrepositories.EmployeeSalaryRepository;
import com.dat.erp.services.EmployeePayrollPolicyService;

@Component
@Order(10)
public class LoadPayrollDataStep implements MonthlySalaryCalculationStep {

    private final EmployeeSalaryRepository employeeSalaryRepository;
    private final EmployeeSalaryDetailRepository employeeSalaryDetailRepository;
    private final EmployeePayrollPolicyService employeePayrollPolicyService;

    public LoadPayrollDataStep(EmployeeSalaryRepository employeeSalaryRepository,
            EmployeeSalaryDetailRepository employeeSalaryDetailRepository,
            EmployeePayrollPolicyService employeePayrollPolicyService) {
        this.employeeSalaryRepository = employeeSalaryRepository;
        this.employeeSalaryDetailRepository = employeeSalaryDetailRepository;
        this.employeePayrollPolicyService = employeePayrollPolicyService;
    }

    @Override
    public void execute(MonthlySalaryCalculationContext context) {
        LocalDate asOfDate = context.getMonth().atEndOfMonth();
        EmployeeSalary employeeSalary = employeeSalaryRepository
                .findFirstActiveByEmployeeCodeAndCompanyCodeAndDate(context.getEmployeeCode(), context.getCompanyCode(),
                        asOfDate)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(
                        Messages.ERROR_PAYROLL_EMPLOYEE_SALARY_NOT_FOUND, context.getEmployeeCode(),
                        context.getMonth())));

        PayrollPolicy payrollPolicy = employeePayrollPolicyService
                .getCompanyPoliciesByEmployeeCodesAndDate(List.of(context.getEmployeeCode()), asOfDate)
                .get(context.getEmployeeCode());
        if (payrollPolicy == null) {
            throw new ResourceNotFoundException(
                    String.format(Messages.ERROR_PAYROLL_POLICY_NOT_FOUND, context.getEmployeeCode(),
                            context.getMonth()));
        }

        context.setEmployeeSalary(employeeSalary);
        context.setPayrollPolicy(payrollPolicy);
        context.setDetails(employeeSalaryDetailRepository
                .findForPayrollByEmployeeSalaryCodeAndCompanyCode(employeeSalary.getCode(), context.getCompanyCode()));
    }
}
