package com.dat.erp.controllers;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dat.erp.dto.request.EmployeeDailyWorkRequest;
import com.dat.erp.dto.response.EmployeeDailyWorkListResponse;
import com.dat.erp.dto.response.PagedResponse;
import com.dat.erp.services.EmployeeDailyWorkService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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

    @Operation(summary = "Get working logs", description = "Returns a paginated list of working logs with optional filters by employee code, date range, and PTO usage.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Working logs retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("")
    @PreAuthorize("hasRoles({'HUMAN_RESOURCES'})")
    public ResponseEntity<PagedResponse<EmployeeDailyWorkListResponse>> getEmployeeDailyWorks(
            @Parameter(description = "Employee code") @RequestParam(required = false) String employeeCode,
            @Parameter(description = "Start date", example = "2026-03-01") @RequestParam(required = false) LocalDate startDate,
            @Parameter(description = "End date", example = "2026-03-31") @RequestParam(required = false) LocalDate endDate,
            @Parameter(description = "Filter PTO logs only") @RequestParam(required = false) Boolean isPto,
            @Parameter(description = "Page number") @RequestParam(required = false) Integer page,
            @Parameter(description = "Page size") @RequestParam(required = false) Integer size,
            @Parameter(description = "Field to sort by") @RequestParam(required = false) String sortBy,
            @Parameter(description = "Sort direction (ASC or DESC)", example = "DESC") @RequestParam(defaultValue = "DESC") String sortDir) {
        return ResponseEntity.ok(
                employeeDailyWorkService.getEmployeeDailyWorks(employeeCode, startDate, endDate, isPto, page, size, sortBy, sortDir));
    }
}
