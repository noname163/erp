package com.dat.erp.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dat.erp.constants.SystemUnitType;
import com.dat.erp.dto.response.PagedResponse;
import com.dat.erp.dto.response.SelectionOptionResponse;
import com.dat.erp.services.SystemUnitService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "System Unit", description = "APIs for retrieving system unit data")
@RestController
@RequestMapping("/api/system-units")
public class SystemUnitController {

    private final SystemUnitService systemUnitService;

    public SystemUnitController(SystemUnitService systemUnitService) {
        this.systemUnitService = systemUnitService;
    }

    @Operation(summary = "Get system unit options", description = "Returns paginated system unit options for the current company.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "System unit options retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("/options")
    @PreAuthorize("hasRoles({'HUMAN_RESOURCES'})")
    public ResponseEntity<PagedResponse<SelectionOptionResponse>> getSystemUnitOptionsByCompanyCode(
            @Parameter(description = "System unit name (contains)") @RequestParam(required = false) String name,
            @Parameter(description = "System unit type", example = "DURATION") @RequestParam(required = false) SystemUnitType type,
            @Parameter(description = "Page number") @RequestParam(required = false) Integer page,
            @Parameter(description = "Page size") @RequestParam(required = false) Integer size,
            @Parameter(description = "Field to sort by") @RequestParam(required = false) String sortBy,
            @Parameter(description = "Sort direction (ASC or DESC)", example = "ASC") @RequestParam(defaultValue = "ASC") String sortDir) {
        return ResponseEntity.ok(systemUnitService.getSystemUnitOptionsByCompanyCode(name, type, page, size, sortBy, sortDir));
    }
}
