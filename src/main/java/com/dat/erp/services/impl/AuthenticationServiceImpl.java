package com.dat.erp.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dat.erp.dtos.requests.LoginRequest;
import com.dat.erp.entities.EmployeeInformation;
import com.dat.erp.repositories.customrepositories.EmployeeInformationRepository;
import com.dat.erp.services.AuthenticationService;
import com.dat.erp.utils.CookieUtils;
import com.dat.erp.utils.CryptoUtils;
import com.dat.erp.utils.JwtUtils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {
    @Autowired
    private EmployeeInformationRepository employeeInformationRepository;
    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public String login(LoginRequest request, HttpServletResponse response) {
        EmployeeInformation employee = employeeInformationRepository
                .findBasicByEmployeeEmail(request
                        .getEmployeeEmail())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));
        if (!CryptoUtils.verifyHash(request.getPassword(), employee.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }
        String token = jwtUtils.generateToken(employee.getUser().getFirstName(), employee.getCode());
        CookieUtils.addTokenCookie(response, token);
        return "Login successful";
    }

    @Override
    public String logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("AUTH_TOKEN", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0); // delete immediately
        response.addCookie(cookie);
        return "Logout successful";
    }

}
