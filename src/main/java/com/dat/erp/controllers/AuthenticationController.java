package com.dat.erp.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dat.erp.builders.ResponseBuilder;
import com.dat.erp.dto.request.LoginRequest;
import com.dat.erp.dto.request.ResetPasswordRequest;
import com.dat.erp.dto.response.CustomApiResponse;
import com.dat.erp.dto.response.LoginResponse;
import com.dat.erp.services.AuthenticationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

/**
 * Controller for handling user authentication.
 */
@Tag(name = "Authentication", description = "APIs for user authentication and login")
@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    /**
     * Authenticate user with credentials and return JWT token in cookie and
     * response body.
     *
     * @param request
     *            the login request containing user credentials
     * @param response
     *            the HTTP response to set cookies
     * @return ResponseEntity with JWT token and authentication result
     */
    @Operation(summary = "User login", description = "Authenticate user with credentials and return JWT token in cookie + response body.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CustomApiResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid credentials"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/login")
    public ResponseEntity<CustomApiResponse<Object>> login(@Valid @RequestBody LoginRequest request,
            HttpServletResponse response) {
        LoginResponse result = authenticationService.login(request, response);
        return ResponseBuilder.ok(result);
    }

    @Operation(summary = "Reset password", description = "Reset password for the currently authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password reset successful"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/reset-password")
    public ResponseEntity<CustomApiResponse<Object>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        return ResponseBuilder.ok(authenticationService.resetPassword(request));
    }
}
