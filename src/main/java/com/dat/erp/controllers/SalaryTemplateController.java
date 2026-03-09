package com.dat.erp.controllers;

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

import com.dat.erp.dto.request.SalaryTemplateRequest;
import com.dat.erp.dto.response.PagedResponse;
import com.dat.erp.dto.response.SalaryTemplateListResponse;
import com.dat.erp.dto.response.SalaryTemplateResponse;
import com.dat.erp.services.SalaryTemplateService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Salary Template", description = "APIs for salary template management")
@RestController
@RequestMapping("/api/salary-templates")
public class SalaryTemplateController {

    private final SalaryTemplateService salaryTemplateService;

    public SalaryTemplateController(SalaryTemplateService salaryTemplateService) {
        this.salaryTemplateService = salaryTemplateService;
    }

    @Operation(summary = "Create salary template", description = "Creates a reusable salary template for the current company.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Salary template created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "409", description = "Conflict")
    })
    @PostMapping("")
    @PreAuthorize("hasRoles({'HUMAN_RESOURCES'})")
    public ResponseEntity<SalaryTemplateResponse> createSalaryTemplate(@Valid @RequestBody SalaryTemplateRequest request) {
        SalaryTemplateResponse response = salaryTemplateService.createSalaryTemplate(request);
        return ResponseEntity.status(201).body(response);
    }

    @Operation(summary = "Get list of salary templates", description = "Returns a paginated list of salary templates for the current company.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Salary template list retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("")
    @PreAuthorize("hasRoles({'HUMAN_RESOURCES'})")
    public ResponseEntity<PagedResponse<SalaryTemplateListResponse>> getSalaryTemplates(
            @Parameter(description = "Template name (contains)") @RequestParam(required = false) String name,
            @Parameter(description = "Currency (exact, case-insensitive)") @RequestParam(required = false) String currency,
            @Parameter(description = "Effective range from (yyyy-MM-dd)") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam(required = false) LocalDate effectiveFrom,
            @Parameter(description = "Effective range to (yyyy-MM-dd)") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam(required = false) LocalDate effectiveTo,
            @Parameter(description = "Page number") @RequestParam(required = false) Integer page,
            @Parameter(description = "Page size") @RequestParam(required = false) Integer size,
            @Parameter(description = "Field to sort by") @RequestParam(required = false) String sortBy,
            @Parameter(description = "Sort direction (ASC or DESC)", example = "DESC") @RequestParam(defaultValue = "DESC") String sortDir) {
        return ResponseEntity
                .ok(salaryTemplateService.getSalaryTemplates(name, currency, effectiveFrom, effectiveTo, page, size, sortBy,
                        sortDir));
    }
}

