package com.dat.erp.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dat.erp.dto.request.EmployeeDailyWorkRequest;
import com.dat.erp.services.EmployeeDailyWorkService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Employee Daily Work", description = "APIs for employee daily work creation")
@RestController
@RequestMapping("/api/employee-daily-works")
public class EmployeeDailyWorkController {

    private final EmployeeDailyWorkService employeeDailyWorkService;

    public EmployeeDailyWorkController(EmployeeDailyWorkService employeeDailyWorkService) {
        this.employeeDailyWorkService = employeeDailyWorkService;
    }

    @Operation(summary = "Create employee daily works", description = "Creates employee daily work records for an employee in specific date(s).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Employee daily work created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Not found"),
            @ApiResponse(responseCode = "409", description = "Conflict")
    })
    @PostMapping("")
    @PreAuthorize("hasRoles({'HUMAN_RESOURCES'})")
    public ResponseEntity<String> createEmployeeDailyWorks(
            @Valid @RequestBody List<@Valid EmployeeDailyWorkRequest> requests) {
        String message = employeeDailyWorkService.createEmployeeDailyWorks(requests);
        return ResponseEntity.status(201).body(message);
    }
}

