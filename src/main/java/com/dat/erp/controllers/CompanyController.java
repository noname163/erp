package com.dat.erp.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dat.erp.builders.ResponseBuilder;
import com.dat.erp.dto.request.CompanyRequest;
import com.dat.erp.dto.response.CompanyResponse;
import com.dat.erp.dto.response.CustomApiResponse;
import com.dat.erp.dto.response.PagedResponse;
import com.dat.erp.services.CompanyService;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller for managing companies.
 */
@Tag(name = "Company Management", description = "APIs for creating and retrieving companies")
@RestController
@RequestMapping("/api/companies")
public class CompanyController {
    @Autowired
    private CompanyService companyService;

    /**
     * Create a new company.
     *
     * @param companyRequest
     *            the request body containing company details
     * @return ResponseEntity with creation result
     */
    @Operation(summary = "Create company", description = "Creates a new company in the system.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Company created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PostMapping("")
    public ResponseEntity<CustomApiResponse<String>> createCompany(@RequestBody CompanyRequest companyRequest) {
        return ResponseBuilder.created(companyService.createCompany(companyRequest));
    }

    /**
     * Get a paginated list of companies with optional search and sorting.
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
     * @return paginated list of companies
     */
    @Operation(summary = "Get list of companies", description = "Returns a paginated list of companies, optionally filtered and sorted.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of companies retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CompanyResponse.class)))
    })
    @GetMapping("")
    public ResponseEntity<PagedResponse<CompanyResponse>> getListCompany(
            @Parameter(description = "Search key") @RequestParam(required = false) String searchKey,
            @Parameter(description = "Search value") @RequestParam(required = false) String searchValue,
            @Parameter(description = "Page number") @RequestParam(required = false) Integer page,
            @Parameter(description = "Page size") @RequestParam(required = false) Integer size,
            @Parameter(description = "Field to sort by") @RequestParam(required = false) String sortBy,
            @Parameter(description = "Sort direction (ASC or DESC)", example = "DESC") @RequestParam(defaultValue = "DESC") String sortDir) {

        return ResponseEntity.ok(companyService.getCompanies(searchKey, searchValue, page, size, sortBy, sortDir));
    }

}