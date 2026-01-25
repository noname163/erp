package com.dat.erp.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dat.erp.dto.request.EmployeeSalaryRequest;
import com.dat.erp.dto.response.EmployeeSalaryResponse;
import com.dat.erp.services.EmployeeSalaryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Employee Salary", description = "APIs for employee salary master creation")
@RestController
@RequestMapping("/api/employee-salaries")
public class EmployeeSalaryController {

    private final EmployeeSalaryService employeeSalaryService;

    public EmployeeSalaryController(EmployeeSalaryService employeeSalaryService) {
        this.employeeSalaryService = employeeSalaryService;
    }

    @Operation(summary = "Create employee salary", description = "Creates employee salary master data for a specific employee.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Employee salary created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Not found"),
            @ApiResponse(responseCode = "409", description = "Conflict")
    })
    @PostMapping("")
    @PreAuthorize("hasRoles({'HUMAN_RESOURCES'})")
    public ResponseEntity<EmployeeSalaryResponse> createEmployeeSalary(@Valid @RequestBody EmployeeSalaryRequest request) {
        EmployeeSalaryResponse response = employeeSalaryService.createEmployeeSalary(request);
        return ResponseEntity.status(201).body(response);
    }
}

