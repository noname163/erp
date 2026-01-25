package com.dat.erp.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dat.erp.constants.Messages;
import com.dat.erp.dto.request.LoginRequest;
import com.dat.erp.dto.response.LoginResponse;
import com.dat.erp.exceptions.UnauthorizedException;
import com.dat.erp.entities.Account;
import com.dat.erp.entities.UserProfile;
import com.dat.erp.repositories.customrepositories.AccountRepository;
import com.dat.erp.services.AuthenticationService;
import com.dat.erp.utils.CookieUtils;
import com.dat.erp.utils.CryptoUtils;
import com.dat.erp.utils.JwtUtils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public LoginResponse login(LoginRequest request, HttpServletResponse response) {
        Account account = accountRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException(Messages.ERROR_INVALID_CREDENTIALS));
        if (!CryptoUtils.verifyHash(request.getPassword(), account.getPasswordHash())) {
            throw new UnauthorizedException(Messages.ERROR_INVALID_CREDENTIALS);
        }
        String token = jwtUtils.generateToken(account.getEmail(), account.getCode());
        CookieUtils.addTokenCookie(response, token);
        LoginResponse loginResponse = LoginResponse.builder()
                .email(account.getEmail())
                .role(account.getRole().getType())
                .build();
        if (account.getUserProfile() != null) {
            UserProfile userProfile = account.getUserProfile();
            loginResponse.setFullName(userProfile.getLastName() + userProfile.getFirstName());
        }
        return loginResponse;
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
