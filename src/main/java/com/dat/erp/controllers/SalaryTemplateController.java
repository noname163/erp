package com.dat.erp.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dat.erp.dto.request.SalaryTemplateRequest;
import com.dat.erp.dto.response.SalaryTemplateResponse;
import com.dat.erp.services.SalaryTemplateService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Salary Template", description = "APIs for salary template creation")
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
}

