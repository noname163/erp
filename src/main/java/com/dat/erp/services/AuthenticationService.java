package com.dat.erp.services;

import com.dat.erp.dto.request.LoginRequest;
import com.dat.erp.dto.response.LoginResponse;

import jakarta.servlet.http.HttpServletResponse;

public interface AuthenticationService {
    public LoginResponse login(LoginRequest request, HttpServletResponse response);

    public String logout(HttpServletResponse response);
}
