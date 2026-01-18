package com.dat.erp.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dat.erp.builders.ResponseBuilder;
import com.dat.erp.dto.request.CreateEmployeeRequest;
import com.dat.erp.dto.response.CustomApiResponse;
import com.dat.erp.dto.response.EmployeeResponse;
import com.dat.erp.services.EmployeeAccountService;
import com.dat.erp.services.SecurityContextService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Employee Management", description = "APIs for creating employee accounts")
@RestController
@RequestMapping("/api/employees")
public class EmployeeController {
    private final EmployeeAccountService employeeAccountService;

    public EmployeeController(EmployeeAccountService employeeAccountService,
            SecurityContextService securityContextService) {
        this.employeeAccountService = employeeAccountService;
    }

    @Operation(summary = "Create employee", description = "Creates a new employee account under the current company.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Employee created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "409", description = "Conflict")
    })
    @PostMapping("")
    @PreAuthorize("hasRoles({'ADMIN', 'HR', 'HUMAN_RESOURCES'})")
    public ResponseEntity<CustomApiResponse<EmployeeResponse>> createEmployee(
            @Valid @RequestBody CreateEmployeeRequest request) {
        return ResponseBuilder.created(employeeAccountService.createEmployee(request));
    }
}
