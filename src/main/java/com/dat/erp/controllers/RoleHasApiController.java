package com.dat.erp.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dat.erp.builders.ResponseBuilder;
import com.dat.erp.dto.request.RoleHasApiRequest;
import com.dat.erp.dto.response.CustomApiResponse;
import com.dat.erp.dto.response.PagedResponse;
import com.dat.erp.dto.response.RoleHasApiResponse;
import com.dat.erp.services.RoleHasApiService;

// Swagger/OpenAPI annotations
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

/**
 * Controller for managing the assignment of APIs to roles.
 */
@Tag(name = "Role-API Assignment", description = "APIs for assigning and retrieving APIs for roles")
@RestController
@RequestMapping("/api/role-has-api")
public class RoleHasApiController {
    @Autowired
    private RoleHasApiService roleHasApiService;

    /**
     * Assign APIs to a role.
     *
     * @param roleHasApiRequest
     *            the request body containing role and API assignment
     *            details
     * @return ResponseEntity with assignment result
     */
    @Operation(summary = "Assign APIs to a role", description = "Assigns one or more APIs to a specified role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "APIs assigned to role successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PostMapping("")
    public ResponseEntity<CustomApiResponse<String>> createRoleHasApis(
            @Valid @RequestBody RoleHasApiRequest roleHasApiRequest) {
        return ResponseBuilder.created(roleHasApiService.assignApisToRole(roleHasApiRequest));
    }

    /**
     * Get a paginated list of APIs assigned to a role.
     *
     * @param roleCode
     *            the code of the role to filter APIs
     * @param page
     *            the page number to retrieve
     * @param size
     *            the number of items per page
     * @param sortBy
     *            the field to sort by
     * @param sortDir
     *            the sort direction (ASC or DESC)
     * @return paginated list of APIs assigned to the role
     */
    @Operation(summary = "Get APIs assigned to a role", description = "Returns a paginated list of APIs assigned to the specified role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of APIs for role retrieved successfully")
    })
    @GetMapping("")
    public ResponseEntity<PagedResponse<RoleHasApiResponse>> getListRoleHasApiByRoleCode(
            @Parameter(description = "Role code to filter APIs", required = false) @RequestParam(required = false) String roleCode,
            @Parameter(description = "Page number") @RequestParam(required = false, defaultValue = "0") Integer page,
            @Parameter(description = "Page size") @RequestParam(required = false, defaultValue = "20") Integer size,
            @Parameter(description = "Field to sort by") @RequestParam(required = false) String sortBy,
            @Parameter(description = "Sort direction (ASC or DESC)", example = "DESC") @RequestParam(defaultValue = "DESC") String sortDir) {
        return ResponseEntity.ok().body(roleHasApiService.getApisByRoleId(roleCode, page, size, sortBy, sortDir));
    }

}
