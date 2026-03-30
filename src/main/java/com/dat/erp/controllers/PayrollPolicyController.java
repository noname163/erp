package com.dat.erp.controllers;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dat.erp.dto.request.PayrollPolicyRequest;
import com.dat.erp.dto.response.PayrollPolicyResponse;
import com.dat.erp.services.PayrollPolicyService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Payroll Policy", description = "APIs for payroll policy management")
@RestController
@RequestMapping("/api/payroll-policies")
public class PayrollPolicyController {

    private final PayrollPolicyService payrollPolicyService;

    public PayrollPolicyController(PayrollPolicyService payrollPolicyService) {
        this.payrollPolicyService = payrollPolicyService;
    }

    @Operation(summary = "Get payroll policy list", description = "Returns payroll policies for the current company filtered by name, effective date range, and unit code.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payroll policy list retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("")
    @PreAuthorize("hasRoles({'HUMAN_RESOURCES'})")
    public ResponseEntity<List<PayrollPolicyResponse>> getPayrollPolicies(
            @RequestParam(required = false) String name,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam(required = false) LocalDate effectiveFrom,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam(required = false) LocalDate effectiveTo,
            @RequestParam(required = false) String unitCode) {
        return ResponseEntity.ok(payrollPolicyService.getPayrollPolicies(name, effectiveFrom, effectiveTo, unitCode));
    }

    @Operation(summary = "Create payroll policy", description = "Creates a payroll policy for the current company.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Payroll policy created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "409", description = "Conflict")
    })
    @PostMapping("")
    @PreAuthorize("hasRoles({'HUMAN_RESOURCES'})")
    public ResponseEntity<PayrollPolicyResponse> createPayrollPolicy(@Valid @RequestBody PayrollPolicyRequest request) {
        return ResponseEntity.status(201).body(payrollPolicyService.createPayrollPolicy(request));
    }
}
