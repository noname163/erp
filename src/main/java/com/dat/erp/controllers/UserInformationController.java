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
import com.dat.erp.dto.request.UserInformationRequest;
import com.dat.erp.dto.response.CustomApiResponse;
import com.dat.erp.dto.response.PagedResponse;
import com.dat.erp.dto.response.UserInformationResponse;
import com.dat.erp.services.UserInformationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Controller for managing user information.
 */
@Tag(name = "User Information Management", description = "APIs for creating and retrieving user information")
@RestController
@RequestMapping("/api/user-informations")
public class UserInformationController {
    @Autowired
    private UserInformationService userInformationService;

    /**
     * Create a new user information record.
     *
     * @param userInformationRequest
     *            the request body containing user information details
     * @return ResponseEntity with creation result
     */
    @Operation(summary = "Create user information", description = "Creates a new user information record in the system.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User information created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PostMapping("")
    public ResponseEntity<CustomApiResponse<String>> createUserInformation(
            @Valid @RequestBody UserInformationRequest userInformationRequest) {
        return ResponseBuilder.created(userInformationService.createUserInformation(userInformationRequest));
    }

    /**
     * Get a paginated list of user information records with optional search and
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
     * @return paginated list of user information records
     */
    @Operation(summary = "Get list of user information", description = "Returns a paginated list of user information records, optionally filtered and sorted.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of user information retrieved successfully")
    })
    @GetMapping("")
    public ResponseEntity<PagedResponse<UserInformationResponse>> getUserInformations(
            @Parameter(description = "Name of field to filter user information", required = false) @RequestParam(required = false) String searchKey,
            @Parameter(description = "Value of field to filter user information", required = false) @RequestParam(required = false) String searchValue,
            @Parameter(description = "Page number") @RequestParam(required = false) Integer page,
            @Parameter(description = "Page size") @RequestParam(required = false) Integer size,
            @Parameter(description = "Field to sort by") @RequestParam(required = false) String sortBy,
            @Parameter(description = "Sort direction (ASC or DESC)", example = "DESC") @RequestParam(defaultValue = "DESC", required = false) String sortDir) {
        return ResponseEntity.ok().body(
                userInformationService.getListUserInformation(searchKey, searchValue, size, page, sortBy, sortDir));
    }

}