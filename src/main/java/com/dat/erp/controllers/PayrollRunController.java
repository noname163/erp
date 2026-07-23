package com.dat.erp.controllers;

import java.time.LocalDateTime;
import java.time.YearMonth;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dat.erp.constants.PayrollRunStatus;
import com.dat.erp.dto.request.PayrollRerunRequest;
import com.dat.erp.dto.response.PagedResponse;
import com.dat.erp.dto.response.PayrollRerunResponse;
import com.dat.erp.dto.response.PayrollRunResponse;
import com.dat.erp.services.payroll.PayrollRunService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Payroll Run", description = "APIs for payroll run management")
@RestController
@RequestMapping("/api/payroll-runs")
public class PayrollRunController {

    private final PayrollRunService payrollRunService;

    public PayrollRunController(PayrollRunService payrollRunService) {
        this.payrollRunService = payrollRunService;
    }

    @Operation(summary = "Get payroll run list", description = "Returns a paginated list of payroll runs for the current company filtered by status, runAt, and closeAt.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payroll run list retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("")
    @PreAuthorize("hasRoles({'HUMAN_RESOURCES'})")
    public ResponseEntity<PagedResponse<PayrollRunResponse>> getPayrollRuns(
            @Parameter(description = "Payroll run status", example = "CALCULATED") @RequestParam(required = false) PayrollRunStatus status,
            @Parameter(description = "Run timestamp from (yyyy-MM-dd'T'HH:mm:ss)") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @RequestParam(required = false) LocalDateTime runAtFrom,
            @Parameter(description = "Run timestamp to (yyyy-MM-dd'T'HH:mm:ss)") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @RequestParam(required = false) LocalDateTime runAtTo,
            @Parameter(description = "Closed timestamp from (yyyy-MM-dd'T'HH:mm:ss)") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @RequestParam(required = false) LocalDateTime closeAtFrom,
            @Parameter(description = "Closed timestamp to (yyyy-MM-dd'T'HH:mm:ss)") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @RequestParam(required = false) LocalDateTime closeAtTo,
            @Parameter(description = "Page number") @RequestParam(required = false) Integer page,
            @Parameter(description = "Page size") @RequestParam(required = false) Integer size,
            @Parameter(description = "Field to sort by") @RequestParam(required = false) String sortBy,
            @Parameter(description = "Sort direction (ASC or DESC)", example = "DESC") @RequestParam(defaultValue = "DESC") String sortDir) {
        return ResponseEntity.ok(payrollRunService.getPayrollRuns(
                status,
                runAtFrom,
                runAtTo,
                closeAtFrom,
                closeAtTo,
                page,
                size,
                sortBy,
                sortDir));
    }

    @Operation(summary = "Run payroll", description = "Creates a payroll run for the requested period and starts payroll calculation for the current company.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Payroll run created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "409", description = "Conflict")
    })
    @PostMapping("")
    @PreAuthorize("hasRoles({'HUMAN_RESOURCES'})")
    public ResponseEntity<PayrollRunResponse> runPayroll(
            @Parameter(description = "Payroll run month (yyyy-MM)", example = "2026-04") @DateTimeFormat(pattern = "yyyy-MM") @RequestParam YearMonth runDate) {
        return ResponseEntity.status(201).body(payrollRunService.runPayroll(runDate));
    }

    @Operation(summary = "Re-run payroll", description = "Recalculates payroll for an existing payroll run with audit logging and optional dry-run preview.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payroll re-run completed or preview calculated"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Payroll run not found"),
            @ApiResponse(responseCode = "409", description = "Payroll run cannot be re-run now")
    })
    @PostMapping("/{payrollRunCode}/rerun")
    @PreAuthorize("hasRoles({'HUMAN_RESOURCES'})")
    public ResponseEntity<PayrollRerunResponse> rerunPayroll(
            @PathVariable String payrollRunCode,
            @RequestBody PayrollRerunRequest request) {
        return ResponseEntity.ok(payrollRunService.rerunPayroll(payrollRunCode, request));
    }
}
