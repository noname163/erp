package com.dat.erp.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dat.erp.dto.response.PagedResponse;
import com.dat.erp.dto.response.SystemApiResponse;
import com.dat.erp.services.SystemApiService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controller for managing system APIs.
 */
@Tag(name = "System API Management", description = "APIs for retrieving system API information")
@RestController
@RequestMapping("/api/system-apis")
public class SystemApiController {
    @Autowired
    private SystemApiService systemApiService;

    /**
     * Get a paginated list of system APIs filtered by API code.
     *
     * @param apiCode
     *            the code to filter APIs
     * @param page
     *            the page number to retrieve
     * @param size
     *            the number of items per page
     * @param sortBy
     *            the field to sort by
     * @param sortDir
     *            the sort direction (ASC or DESC)
     * @return paginated list of system APIs
     */
    @Operation(summary = "Get list of system APIs", description = "Returns a paginated list of system APIs filtered by API code, with sorting options.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of system APIs retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SystemApiResponse.class)))
    })
    @GetMapping("")
    public ResponseEntity<PagedResponse<SystemApiResponse>> getMethodName(
            @Parameter(description = "API code to filter APIs", required = false) @RequestParam(required = false) String apiCode,
            @Parameter(description = "Page number") @RequestParam(required = false) Integer page,
            @Parameter(description = "Page size") @RequestParam(required = false) Integer size,
            @Parameter(description = "Field to sort by") @RequestParam(required = false) String sortBy,
            @Parameter(description = "Sort direction (ASC or DESC)", example = "DESC") @RequestParam(defaultValue = "DESC", required = false) String sortDir) {
        return ResponseEntity.ok(systemApiService.getListSystemApi(apiCode, page, size, sortBy, sortDir));
    }

}