package com.dat.erp.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dat.erp.dto.request.EmployeeSalaryDetailRequest;
import com.dat.erp.services.EmployeeSalaryDetailService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Employee Salary Detail", description = "APIs for employee salary detail creation")
@RestController
@RequestMapping("/api/employee-salary-details")
public class EmployeeSalaryDetailController {

    private final EmployeeSalaryDetailService employeeSalaryDetailService;

    public EmployeeSalaryDetailController(EmployeeSalaryDetailService employeeSalaryDetailService) {
        this.employeeSalaryDetailService = employeeSalaryDetailService;
    }

    @Operation(summary = "Create employee salary details", description = "Creates employee salary detail records for a specific employee salary.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Employee salary details created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Not found"),
            @ApiResponse(responseCode = "409", description = "Conflict")
    })
    @PostMapping("")
    @PreAuthorize("hasRoles({'HUMAN_RESOURCES'})")
    public ResponseEntity<String> createEmployeeSalaryDetails(
            @Valid @RequestBody List<@Valid EmployeeSalaryDetailRequest> requests) {
        String message = employeeSalaryDetailService.createEmployeeSalaryDetails(requests);
        return ResponseEntity.status(201).body(message);
    }
}

