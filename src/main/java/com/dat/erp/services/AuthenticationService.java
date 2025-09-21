package com.dat.erp.services;

import com.dat.erp.dto.request.LoginRequest;

import jakarta.servlet.http.HttpServletResponse;

public interface AuthenticationService {
    public String login(LoginRequest request, HttpServletResponse response);

    public String logout(HttpServletResponse response);
}
