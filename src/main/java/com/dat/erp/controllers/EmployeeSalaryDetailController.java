package com.dat.erp.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dat.erp.services.EmployeeSalaryDetailService;

import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Employee Salary Detail", description = "APIs for employee salary detail creation")
@RestController
@RequestMapping("/api/employee-salary-details")
public class EmployeeSalaryDetailController {

    private final EmployeeSalaryDetailService employeeSalaryDetailService;

    public EmployeeSalaryDetailController(EmployeeSalaryDetailService employeeSalaryDetailService) {
        this.employeeSalaryDetailService = employeeSalaryDetailService;
    }

}
