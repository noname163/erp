package com.dat.erp.controllers;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dat.erp.constants.PayrollStatus;
import com.dat.erp.dto.response.PagedResponse;
import com.dat.erp.dto.response.PayrollResultListResponse;
import com.dat.erp.services.payroll.PayrollResultService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Payroll Result", description = "APIs for payroll result management")
@RestController
@RequestMapping("/api/payroll-results")
public class PayrollResultController {

    private final PayrollResultService payrollResultService;

    public PayrollResultController(PayrollResultService payrollResultService) {
        this.payrollResultService = payrollResultService;
    }

    @Operation(summary = "Get payroll result list", description = "Returns a paginated list of payroll results filtered by payrollRunCode, createdDate, sourceType, and employeeCode. payrollRunCode is required. If createdDate is omitted, the current request date is used.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payroll result list retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("")
    @PreAuthorize("hasRoles({'HUMAN_RESOURCES'})")
    public ResponseEntity<PagedResponse<PayrollResultListResponse>> getPayrollResults(
            @Parameter(description = "Payroll run code", example = "PRN-1", required = true) @RequestParam String payrollRunCode,
            @Parameter(description = "Created date", example = "2026-04-01") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam(required = false) LocalDate createdDate,
            @Parameter(description = "Payroll source type", example = "RUNNING") @RequestParam(required = false) PayrollStatus sourceType,
            @Parameter(description = "Employee code") @RequestParam(required = false) String employeeCode,
            @Parameter(description = "Page number") @RequestParam(required = false) Integer page,
            @Parameter(description = "Page size") @RequestParam(required = false) Integer size,
            @Parameter(description = "Field to sort by") @RequestParam(required = false) String sortBy,
            @Parameter(description = "Sort direction (ASC or DESC)", example = "DESC") @RequestParam(defaultValue = "DESC") String sortDir) {
        return ResponseEntity.ok(payrollResultService.getPayrollResults(
                payrollRunCode,
                createdDate,
                sourceType,
                employeeCode,
                page,
                size,
                sortBy,
                sortDir));
    }
}
