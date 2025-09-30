package com.dat.erp.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dat.erp.builders.ResponseBuilder;
import com.dat.erp.dto.request.RoleRequest;
import com.dat.erp.dto.response.CustomApiResponse;
import com.dat.erp.dto.response.PagedResponse;
import com.dat.erp.dto.response.RoleResponse;
import com.dat.erp.services.RoleService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

// Swagger/OpenAPI annotations
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

/**
 * Controller for managing roles in the ERP system.
 */
@Tag(name = "Role Management", description = "APIs for managing roles")
@RestController
@RequestMapping("/api/roles")
public class RoleController {
    @Autowired
    private RoleService roleService;

    /**
     * Create a new role.
     *
     * @param roleRequest
     *            the request body containing role details
     * @return ResponseEntity with creation result
     */
    @Operation(summary = "Create a new role", description = "Creates a new role in the system.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Role created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PostMapping("")
    public ResponseEntity<CustomApiResponse<String>> createRole(@Valid @RequestBody RoleRequest roleRequest) {
        return ResponseBuilder.created(roleService.createRole(roleRequest));
    }

    /**
     * Get a paginated list of roles with optional search and sorting.
     *
     * @param searchValue
     *            the search keyword for filtering roles
     * @param page
     *            the page number to retrieve
     * @param size
     *            the number of items per page
     * @param sortBy
     *            the field to sort by
     * @param sortDir
     *            the sort direction (ASC or DESC)
     * @return paginated list of roles
     */
    @Operation(summary = "Get list of roles", description = "Returns a paginated list of roles, optionally filtered by search value and sorted.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of roles retrieved successfully")
    })
    @GetMapping("")
    public ResponseEntity<PagedResponse<RoleResponse>> getListRole(
            @Parameter(description = "Search keyword for roles") @RequestParam(required = false) String searchValue,
            @Parameter(description = "Page number") @RequestParam(required = false) Integer page,
            @Parameter(description = "Page size") @RequestParam(required = false) Integer size,
            @Parameter(description = "Field to sort by") @RequestParam(required = false) String sortBy,
            @Parameter(description = "Sort direction (ASC or DESC)", example = "DESC") @RequestParam(defaultValue = "DESC") String sortDir) {
        return ResponseEntity.ok(roleService.getRoleResponses(searchValue, page, size, sortBy, sortDir));
    }

}
