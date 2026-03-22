package com.dat.erp.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dat.erp.dto.response.payroll.EmployeePayslipResponse;
import com.dat.erp.services.PayrollRunService;

import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Employee Payroll", description = "Self-service payroll payslip APIs")
@RestController
@RequestMapping("/api/my/payroll-runs")
public class EmployeePayrollController {

    private final PayrollRunService payrollRunService;

    public EmployeePayrollController(PayrollRunService payrollRunService) {
        this.payrollRunService = payrollRunService;
    }

    @GetMapping("/{runCode}/payslip")
    @PreAuthorize("hasRoles({'EMPLOYEE', 'ADMIN', 'HUMAN_RESOURCES'})")
    public ResponseEntity<EmployeePayslipResponse> getMyPayslip(@PathVariable String runCode) {
        return ResponseEntity.ok(payrollRunService.getPayslipForCurrentUser(runCode));
    }
}
