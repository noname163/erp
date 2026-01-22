package com.dat.erp.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dat.erp.dto.request.SalaryRequest;
import com.dat.erp.dto.response.SalaryResponse;
import com.dat.erp.services.SalaryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Salary", description = "APIs for salary component creation")
@RestController
@RequestMapping("/api/salaries")
public class SalaryController {

    private final SalaryService salaryService;

    public SalaryController(SalaryService salaryService) {
        this.salaryService = salaryService;
    }

    @Operation(summary = "Create salary components", description = "Creates reusable salary components for the current company.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Salary created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "409", description = "Conflict")
    })
    @PostMapping("")
    @PreAuthorize("hasRoles({'HUMAN_RESOURCES'})")
    public ResponseEntity<List<SalaryResponse>> createSalaries(
            @Valid @RequestBody List<@Valid SalaryRequest> requests) {
        return ResponseEntity.status(201).body(salaryService.createSalaries(requests));
    }
}

