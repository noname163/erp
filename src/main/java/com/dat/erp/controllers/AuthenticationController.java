package com.dat.erp.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.dat.erp.builders.ResponseBuilder;
import com.dat.erp.dtos.requests.LoginRequest;
import com.dat.erp.dtos.responses.ApiResponse;
import com.dat.erp.services.AuthenticationService;

import jakarta.servlet.http.HttpServletResponse;

@RestController
public class AuthenticationController {
    @Autowired
    private AuthenticationService authenticationService;

    public ResponseEntity<ApiResponse<Object>> login(LoginRequest request, HttpServletResponse response) {
        String result = authenticationService.login(request, response);
        return ResponseBuilder.ok(result);
    }
}
