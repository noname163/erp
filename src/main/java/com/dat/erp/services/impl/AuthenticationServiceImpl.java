package com.dat.erp.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dat.erp.constants.Messages;
import com.dat.erp.dto.request.LoginRequest;
import com.dat.erp.exceptions.UnauthorizedException;
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
                .findByEmail(request.getEmployeeEmail())
                .orElseThrow(() -> new UnauthorizedException(Messages.ERROR_INVALID_CREDENTIALS));
        if (!CryptoUtils.verifyHash(request.getPassword(), employee.getPassword())) {
            throw new UnauthorizedException(Messages.ERROR_INVALID_CREDENTIALS);
        }
        String token = jwtUtils.generateToken(employee.getUser().getFirstName(), employee.getCode());
        CookieUtils.addTokenCookie(response, token);
        return Messages.LOGIN_SUCCESS;
    }

    @Override
    public String logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("AUTH_TOKEN", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0); // delete immediately
        response.addCookie(cookie);
        return Messages.LOGOUT_SUCCESS;
    }

}
