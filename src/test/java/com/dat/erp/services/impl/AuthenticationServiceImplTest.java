package com.dat.erp.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.dat.erp.constants.Messages;
import com.dat.erp.dto.request.LoginRequest;
import com.dat.erp.dto.response.LoginResponse;
import com.dat.erp.entities.Account;
import com.dat.erp.exceptions.UnauthorizedException;
import com.dat.erp.repositories.customrepositories.AccountRepository;
import com.dat.erp.utils.CookieUtils;
import com.dat.erp.utils.CryptoUtils;
import com.dat.erp.utils.JwtUtils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

class AuthenticationServiceImplTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private JwtUtils jwtUtils;

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
        account.setCode("ACC-1");
        account.setPasswordHash("$2a$10$hash");
    }

    @Test
    void login_success_setsCookieAndReturnsMessage() {
        when(accountRepository.findByEmail("user@example.com")).thenReturn(Optional.of(account));
        when(jwtUtils.generateToken("user@example.com", "ACC-1")).thenReturn("token");

        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        try (MockedStatic<CryptoUtils> crypto = Mockito.mockStatic(CryptoUtils.class);
                MockedStatic<CookieUtils> cookies = Mockito.mockStatic(CookieUtils.class)) {
            crypto.when(() -> CryptoUtils.verifyHash("password", "$2a$10$hash")).thenReturn(true);

            LoginResponse result = authenticationService.login(request, response);

            assertEquals(Messages.LOGIN_SUCCESS, result);
            cookies.verify(() -> CookieUtils.addTokenCookie(eq(response), eq("token")));
        }
    }

    @Test
    void login_accountNotFound_throwsUnauthorized() {
        when(accountRepository.findByEmail("user@example.com")).thenReturn(Optional.empty());

        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        assertThrows(UnauthorizedException.class, () -> authenticationService.login(request, response));

        verify(jwtUtils, never()).generateToken(any(), any());
    }

    @Test
    void login_passwordMismatch_throwsUnauthorized() {
        when(accountRepository.findByEmail("user@example.com")).thenReturn(Optional.of(account));

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

        String result = authenticationService.logout(response);

        assertEquals(Messages.LOGOUT_SUCCESS, result);
        verify(response).addCookie(Mockito.argThat(cookie -> matchesLogoutCookie(cookie)));
    }

    private static boolean matchesLogoutCookie(Cookie cookie) {
        return cookie != null
                && "AUTH_TOKEN".equals(cookie.getName())
                && cookie.getValue() == null
                && cookie.isHttpOnly()
                && cookie.getSecure()
                && "/".equals(cookie.getPath())
                && cookie.getMaxAge() == 0;
    }
}
