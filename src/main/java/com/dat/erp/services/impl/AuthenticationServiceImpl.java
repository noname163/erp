package com.dat.erp.services.impl;

import org.springframework.beans.factory.annotation.Autowired;

import com.dat.erp.dtos.requests.LoginRequest;
import com.dat.erp.entities.EmployeeInformation;
import com.dat.erp.repositories.customrepositories.EmployeeInformationRepository;
import com.dat.erp.services.AuthenticationService;
import com.dat.erp.utils.CookieUtils;
import com.dat.erp.utils.CryptoUtils;
import com.dat.erp.utils.JwtUtils;

import jakarta.servlet.http.HttpServletResponse;

public class AuthenticationServiceImpl implements AuthenticationService {
    @Autowired
    private EmployeeInformationRepository employeeInformationRepository;
    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public String login(LoginRequest request, HttpServletResponse response) {
        EmployeeInformation employee = employeeInformationRepository
                .findFullByEmployeeEmail(request.getEmployeeEmail())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));
        if (CryptoUtils.verifyHash(request.getPassword(), employee.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }
        String token = jwtUtils.generateToken(employee.getUser().getFirstName(), employee.getCode());
        CookieUtils.addTokenCookie(response, token);
        return "Login successful";
    }

    @Override
    public String logout(HttpServletResponse response) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'logout'");
    }

}
