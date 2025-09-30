package com.dat.erp.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dat.erp.builders.ResponseBuilder;
import com.dat.erp.dto.request.EmployeeInformationRequest;
import com.dat.erp.dto.response.CustomApiResponse;
import com.dat.erp.dto.response.EmployeeInformationResponse;
import com.dat.erp.dto.response.PagedResponse;
import com.dat.erp.services.EmployeeInformationService;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller for managing employee information.
 */
@Tag(name = "Employee Information Management", description = "APIs for creating and retrieving employee information")
@RestController
@RequestMapping("/api/employees-information")
public class EmployeeInformationController {
    @Autowired
    private EmployeeInformationService employeeInformationService;

    /**
     * Create a new employee information record.
     *
     * @param employeeRequest
     *            the request body containing employee details
     * @return ResponseEntity with creation result
     */
    @Operation(summary = "Create employee information", description = "Creates a new employee information record in the system.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Employee information created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PostMapping("")
    public ResponseEntity<CustomApiResponse<String>> createEmployeeInformation(
            @Valid @RequestBody EmployeeInformationRequest employeeRequest) {
        return ResponseBuilder.created(employeeInformationService.createEmployeeInformation(employeeRequest));
    }

    /**
     * Get a paginated list of employee information records with optional search and
     * sorting.
     *
     * @param searchKey
     *            the key to search by
     * @param searchValue
     *            the value to search for
     * @param page
     *            the page number to retrieve
     * @param size
     *            the number of items per page
     * @param sortBy
     *            the field to sort by
     * @param sortDir
     *            the sort direction (ASC or DESC)
     * @return paginated list of employee information records
     */
    @Operation(summary = "Get list of employee information", description = "Returns a paginated list of employee information records, optionally filtered and sorted.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of employee information retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = EmployeeInformationResponse.class)))
    })
    @GetMapping("")
    public ResponseEntity<PagedResponse<EmployeeInformationResponse>> getListEmployee(
            @Parameter(description = "Search key") @RequestParam(required = false) String searchKey,
            @Parameter(description = "Search value") @RequestParam(required = false) String searchValue,
            @Parameter(description = "Page number") @RequestParam(required = false) Integer page,
            @Parameter(description = "Page size") @RequestParam(required = false) Integer size,
            @Parameter(description = "Field to sort by") @RequestParam(required = false) String sortBy,
            @Parameter(description = "Sort direction (ASC or DESC)", example = "DESC") @RequestParam(defaultValue = "DESC") String sortDir) {
        return ResponseEntity
                .ok(employeeInformationService.getListEmployeeInformationResponse(searchKey, searchValue, page,
                        size, sortBy, sortDir));
    }

}