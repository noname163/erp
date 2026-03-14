package com.dat.erp.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dat.erp.dto.response.PagedResponse;
import com.dat.erp.dto.response.SelectionOptionResponse;
import com.dat.erp.services.UserProfileService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "User Profile", description = "APIs for retrieving user profile data")
@RestController
@RequestMapping("/api/v1/user-profiles")
public class UserProfileController {

    private final UserProfileService userProfileService;

    public UserProfileController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @Operation(summary = "Get user profile options", description = "Returns paginated user profile options filtered by first name.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User profile options retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("/options")
    @PreAuthorize("hasRoles({'HUMAN_RESOURCES'})")
    public ResponseEntity<PagedResponse<SelectionOptionResponse>> getUserProfileOptions(
            @Parameter(description = "First name (contains)") @RequestParam(required = false) String firstName,
            @Parameter(description = "Page number") @RequestParam(required = false) Integer page,
            @Parameter(description = "Page size") @RequestParam(required = false) Integer size,
            @Parameter(description = "Field to sort by") @RequestParam(required = false) String sortBy,
            @Parameter(description = "Sort direction (ASC or DESC)", example = "ASC") @RequestParam(defaultValue = "ASC") String sortDir) {
        return ResponseEntity.ok(userProfileService.getUserProfileOptionsByFirstName(firstName, page, size, sortBy, sortDir));
    }
}
