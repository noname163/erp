package com.dat.erp.services.impl;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dat.erp.constants.Messages;
import com.dat.erp.dto.request.LoginRequest;
import com.dat.erp.dto.request.ResetPasswordRequest;
import com.dat.erp.dto.response.LoginResponse;
import com.dat.erp.entities.Account;
import com.dat.erp.entities.UserProfile;
import com.dat.erp.exceptions.BadRequestException;
import com.dat.erp.exceptions.UnauthorizedException;
import com.dat.erp.repositories.customrepositories.AccountRepository;
import com.dat.erp.services.AuthenticationService;
import com.dat.erp.services.SecurityContextService;
import com.dat.erp.systemconfigs.CustomUserDetails;
import com.dat.erp.utils.CookieUtils;
import com.dat.erp.utils.CryptoUtils;
import com.dat.erp.utils.JwtUtils;

import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {
    private final AccountRepository accountRepository;
    private final JwtUtils jwtUtils;
    private final SecurityContextService securityContextService;

    @Override
    public LoginResponse login(LoginRequest request, HttpServletResponse response) {
        Account account = accountRepository
                .findByEmailWithRoleAndUserProfile(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException(Messages.ERROR_INVALID_CREDENTIALS));
        if (!CryptoUtils.verifyHash(request.getPassword(), account.getPasswordHash())) {
            throw new UnauthorizedException(Messages.ERROR_INVALID_CREDENTIALS);
        }
        UserProfile userProfile = account.getUserProfile();
        String token = jwtUtils.generateToken(account.getEmail(), account.getCode());
        CookieUtils.addTokenCookie(response, token);
        LoginResponse loginResponse = LoginResponse.builder()
                .email(account.getEmail())
                .role(account.getRole().getType())
                .code(userProfile == null ? null : userProfile.getCode())
                .build();
        if (account.getLastLogin() == null) {
            loginResponse.setFirstLogin(true);
        } else {
            loginResponse.setFirstLogin(false);
        }
        if (userProfile != null) {
            loginResponse.setFullName(userProfile.getLastName() + userProfile.getFirstName());
        }
        account.setLastLogin(LocalDateTime.now());
        accountRepository.save(account);
        return loginResponse;
    }

    @Override
    public String logout(HttpServletResponse response) {
        CookieUtils.clearTokenCookie(response);
        return Messages.LOGOUT_SUCCESS;
    }

    @Override
    @Transactional
    public String resetPassword(ResetPasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException(Messages.ERROR_PASSWORD_CONFIRM_MISMATCH);
        }

        CustomUserDetails currentUser = securityContextService.getCurrentUser();
        Account account = accountRepository.findByCode(currentUser.getCode())
                .orElseThrow(() -> new UnauthorizedException("User account not found"));

        if (!CryptoUtils.verifyHash(request.getOldPassword(), account.getPasswordHash())) {
            throw new BadRequestException(Messages.ERROR_OLD_PASSWORD_INCORRECT);
        }
        account.setLastLogin(LocalDateTime.now());
        account.setPasswordHash(CryptoUtils.hash(request.getNewPassword()));
        accountRepository.save(account);
        return Messages.PASSWORD_RESET_SUCCESS;
    }

}
