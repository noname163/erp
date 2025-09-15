package com.dat.erp.services;

import org.springframework.stereotype.Service;

import com.dat.erp.dtos.requests.LoginRequest;

import jakarta.servlet.http.HttpServletResponse;

@Service
public interface AuthenticationService {
    public String login(LoginRequest request, HttpServletResponse response);

    public String logout(HttpServletResponse response);
}
