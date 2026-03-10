package com.dat.erp.controllers;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dat.erp.dto.request.EmployeeSalaryRequest;
import com.dat.erp.dto.response.EmployeeSalaryListResponse;
import com.dat.erp.dto.response.EmployeeSalaryResponse;
import com.dat.erp.dto.response.PagedResponse;
import com.dat.erp.services.EmployeeSalaryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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

    @Operation(summary = "Get employee salary list", description = "Returns a paginated employee salary list for the current company.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Employee salary list retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid filter values"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("")
    @PreAuthorize("hasRoles({'HUMAN_RESOURCES'})")
    public ResponseEntity<PagedResponse<EmployeeSalaryListResponse>> getEmployeeSalaries(
            @Parameter(description = "Employee name (contains)") @RequestParam(required = false) String employeeName,
            @Parameter(description = "Minimum total amount") @RequestParam(required = false) BigDecimal minAmount,
            @Parameter(description = "Maximum total amount") @RequestParam(required = false) BigDecimal maxAmount,
            @Parameter(description = "Effective range from (yyyy-MM-dd)") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam(required = false) LocalDate effectiveFrom,
            @Parameter(description = "Effective range to (yyyy-MM-dd)") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam(required = false) LocalDate effectiveTo,
            @Parameter(description = "Page number") @RequestParam(required = false) Integer page,
            @Parameter(description = "Page size") @RequestParam(required = false) Integer size,
            @Parameter(description = "Field to sort by") @RequestParam(required = false) String sortBy,
            @Parameter(description = "Sort direction (ASC or DESC)", example = "DESC") @RequestParam(defaultValue = "DESC") String sortDir) {
        return ResponseEntity.ok(employeeSalaryService.getEmployeeSalaries(employeeName, minAmount, maxAmount, effectiveFrom,
                effectiveTo, page, size, sortBy, sortDir));
    }
}
