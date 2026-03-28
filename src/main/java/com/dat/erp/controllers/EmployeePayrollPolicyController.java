package com.dat.erp.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dat.erp.dto.request.EmployeePayrollPolicyRequest;
import com.dat.erp.dto.response.EmployeePayrollPolicyResponse;
import com.dat.erp.services.EmployeePayrollPolicyService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Employee Payroll Policy", description = "APIs for assigning payroll policies to employees without active overlap")
@RestController
@RequestMapping("/api/v1/employee-payroll-policies")
public class EmployeePayrollPolicyController {

    private final EmployeePayrollPolicyService employeePayrollPolicyService;

    public EmployeePayrollPolicyController(EmployeePayrollPolicyService employeePayrollPolicyService) {
        this.employeePayrollPolicyService = employeePayrollPolicyService;
    }

    @Operation(summary = "Assign payroll policy to employee", description = "Creates an active employee payroll policy assignment and rejects overlapping active periods.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Employee payroll policy created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "409", description = "Active payroll policy already overlaps")
    })
    @PostMapping("")
    @PreAuthorize("hasRoles({'HUMAN_RESOURCES'})")
    public ResponseEntity<EmployeePayrollPolicyResponse> createEmployeePayrollPolicy(
            @Valid @RequestBody EmployeePayrollPolicyRequest request) {
        return ResponseEntity.status(201).body(employeePayrollPolicyService.createEmployeePayrollPolicy(request));
    }

    @Operation(summary = "Deactivate employee payroll policy", description = "Marks the employee payroll policy inactive so a new policy can be assigned.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Employee payroll policy deactivated successfully"),
            @ApiResponse(responseCode = "404", description = "Employee payroll policy not found")
    })
    @PatchMapping("/{code}/inactive")
    @PreAuthorize("hasRoles({'HUMAN_RESOURCES'})")
    public ResponseEntity<EmployeePayrollPolicyResponse> deactivateEmployeePayrollPolicy(@PathVariable String code) {
        return ResponseEntity.ok(employeePayrollPolicyService.deactivateEmployeePayrollPolicy(code));
    }

    @Operation(summary = "Get employee payroll policies", description = "Returns payroll policy assignments for one employee ordered by effectiveFrom descending.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Employee payroll policies retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Employee not found")
    })
    @GetMapping("")
    @PreAuthorize("hasRoles({'HUMAN_RESOURCES'})")
    public ResponseEntity<List<EmployeePayrollPolicyResponse>> getEmployeePayrollPolicies(
            @Parameter(description = "User profile code") @RequestParam String userProfileCode) {
        return ResponseEntity.ok(employeePayrollPolicyService.getEmployeePayrollPolicies(userProfileCode));
    }
}
