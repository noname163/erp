package com.dat.erp.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.dat.erp.constants.Messages;
import com.dat.erp.dto.request.LoginRequest;
import com.dat.erp.dto.request.ResetPasswordRequest;
import com.dat.erp.dto.response.LoginResponse;
import com.dat.erp.entities.Account;
import com.dat.erp.entities.Role;
import com.dat.erp.exceptions.BadRequestException;
import com.dat.erp.exceptions.UnauthorizedException;
import com.dat.erp.repositories.customrepositories.AccountRepository;
import com.dat.erp.services.SecurityContextService;
import com.dat.erp.systemconfigs.CustomUserDetails;
import com.dat.erp.utils.CookieUtils;
import com.dat.erp.utils.CryptoUtils;
import com.dat.erp.utils.JwtUtils;

import jakarta.servlet.http.HttpServletResponse;

class AuthenticationServiceImplTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private SecurityContextService securityContextService;

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    private LoginRequest request;
    private Account account;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        request = new LoginRequest();
        request.setEmail("user@example.com");
        request.setPassword("password");

        account = new Account();
        account.setEmail("user@example.com");
        com.dat.erp.testutils.EntityTestData.setCode(account, "ACC-1");
        account.setPasswordHash("$2a$10$hash");
        account.setRole(Role.builder().type("ADMIN").name("ADMIN").build());
    }

    @Test
    void login_success_setsCookieAndReturnsMessage() {
        when(accountRepository.findByEmailWithRoleAndUserProfile("user@example.com")).thenReturn(Optional.of(account));
        when(jwtUtils.generateToken("user@example.com", "ACC-1")).thenReturn("token");

        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        try (MockedStatic<CryptoUtils> crypto = Mockito.mockStatic(CryptoUtils.class);
                MockedStatic<CookieUtils> cookies = Mockito.mockStatic(CookieUtils.class)) {
            crypto.when(() -> CryptoUtils.verifyHash("password", "$2a$10$hash")).thenReturn(true);

            LoginResponse result = authenticationService.login(request, response);

            assertEquals("user@example.com", result.getEmail());
            assertNull(result.getCode());
            assertNull(result.getFullName());
            assertTrue(result.isFirstLogin());
            cookies.verify(() -> CookieUtils.addTokenCookie(eq(response), eq("token")));
        }
    }

    @Test
    void login_accountNotFound_throwsUnauthorized() {
        when(accountRepository.findByEmailWithRoleAndUserProfile("user@example.com")).thenReturn(Optional.empty());

        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        assertThrows(UnauthorizedException.class, () -> authenticationService.login(request, response));

        verify(jwtUtils, never()).generateToken(any(), any());
    }

    @Test
    void login_passwordMismatch_throwsUnauthorized() {
        when(accountRepository.findByEmailWithRoleAndUserProfile("user@example.com")).thenReturn(Optional.of(account));

        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        try (MockedStatic<CryptoUtils> crypto = Mockito.mockStatic(CryptoUtils.class)) {
            crypto.when(() -> CryptoUtils.verifyHash("password", "$2a$10$hash")).thenReturn(false);

            assertThrows(UnauthorizedException.class, () -> authenticationService.login(request, response));
        }

        verify(jwtUtils, never()).generateToken(any(), any());
    }

    @Test
    void logout_addsExpiredCookieAndReturnsMessage() {
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);

        try (MockedStatic<CookieUtils> cookies = Mockito.mockStatic(CookieUtils.class)) {
            String result = authenticationService.logout(response);

            assertEquals(Messages.LOGOUT_SUCCESS, result);
            cookies.verify(() -> CookieUtils.clearTokenCookie(eq(response)));
        }
    }

    @Test
    void resetPassword_success_updatesHash() {
        ResetPasswordRequest resetRequest = new ResetPasswordRequest();
        resetRequest.setOldPassword("oldPass");
        resetRequest.setNewPassword("newPass");
        resetRequest.setConfirmPassword("newPass");

        CustomUserDetails currentUser = Mockito.mock(CustomUserDetails.class);
        when(currentUser.getCode()).thenReturn("ACC-1");
        when(securityContextService.getCurrentUser()).thenReturn(currentUser);
        when(accountRepository.findByCode("ACC-1")).thenReturn(Optional.of(account));

        try (MockedStatic<CryptoUtils> crypto = Mockito.mockStatic(CryptoUtils.class)) {
            crypto.when(() -> CryptoUtils.verifyHash("oldPass", "$2a$10$hash")).thenReturn(true);
            crypto.when(() -> CryptoUtils.hash("newPass")).thenReturn("$2a$10$newHash");

            String result = authenticationService.resetPassword(resetRequest);

            assertEquals(Messages.PASSWORD_RESET_SUCCESS, result);

            ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
            verify(accountRepository).save(accountCaptor.capture());
            assertEquals("$2a$10$newHash", accountCaptor.getValue().getPasswordHash());
        }
    }

    @Test
    void resetPassword_confirmMismatch_throwsBadRequest() {
        ResetPasswordRequest resetRequest = new ResetPasswordRequest();
        resetRequest.setOldPassword("oldPass");
        resetRequest.setNewPassword("newPass");
        resetRequest.setConfirmPassword("different");

        assertThrows(BadRequestException.class, () -> authenticationService.resetPassword(resetRequest));
        verify(securityContextService, never()).getCurrentUser();
    }

    @Test
    void resetPassword_oldPasswordIncorrect_throwsBadRequest() {
        ResetPasswordRequest resetRequest = new ResetPasswordRequest();
        resetRequest.setOldPassword("oldPass");
        resetRequest.setNewPassword("newPass");
        resetRequest.setConfirmPassword("newPass");

        CustomUserDetails currentUser = Mockito.mock(CustomUserDetails.class);
        when(currentUser.getCode()).thenReturn("ACC-1");
        when(securityContextService.getCurrentUser()).thenReturn(currentUser);
        when(accountRepository.findByCode("ACC-1")).thenReturn(Optional.of(account));

        try (MockedStatic<CryptoUtils> crypto = Mockito.mockStatic(CryptoUtils.class)) {
            crypto.when(() -> CryptoUtils.verifyHash("oldPass", "$2a$10$hash")).thenReturn(false);

            assertThrows(BadRequestException.class, () -> authenticationService.resetPassword(resetRequest));
            verify(accountRepository, never()).save(any());
        }
    }
}
