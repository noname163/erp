package com.dat.erp.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import com.dat.erp.dtos.requests.LoginRequest;
import com.dat.erp.entities.EmployeeInformation;
import com.dat.erp.entities.UserInformation;
import com.dat.erp.repositories.customrepositories.EmployeeInformationRepository;
import com.dat.erp.utils.CookieUtils;
import com.dat.erp.utils.CryptoUtils;
import com.dat.erp.utils.JwtUtils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

class AuthenticationServiceImplTest {

    @Mock
    private EmployeeInformationRepository employeeInformationRepository;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    private EmployeeInformation employee;
    private LoginRequest loginRequest;

    private MockedStatic<CryptoUtils> cryptoUtilsMock;
    private MockedStatic<CookieUtils> cookieUtilsMock;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Mock static utilities
        cryptoUtilsMock = mockStatic(CryptoUtils.class);
        cookieUtilsMock = mockStatic(CookieUtils.class);

        // Prepare test data
        UserInformation user = new UserInformation();
        user.setFirstName("John");

        employee = new EmployeeInformation();
        employee.setCode("EMP123");
        employee.setEmail("john@example.com");
        employee.setPassword("hashedPassword");
        employee.setUser(user);

        loginRequest = new LoginRequest();
        loginRequest.setEmployeeEmail("john@example.com");
        loginRequest.setPassword("plainPassword");
    }

    @AfterEach
    void tearDown() {
        cryptoUtilsMock.close();
        cookieUtilsMock.close();
    }

    @Test
    void testLogin_Success() {
        // given
        when(employeeInformationRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(employee));
        cryptoUtilsMock.when(() -> CryptoUtils.verifyHash("plainPassword", "hashedPassword"))
                .thenReturn(true);
        when(jwtUtils.generateToken("John", "EMP123")).thenReturn("jwt-token");

        // when
        String result = authenticationService.login(loginRequest, response);

        // then
        assertEquals("Login successful", result);
        cookieUtilsMock.verify(() -> CookieUtils.addTokenCookie(response, "jwt-token"));
    }

    @Test
    void testLogin_EmployeeNotFound() {
        // given
        when(employeeInformationRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.empty());

        // when + then
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authenticationService.login(loginRequest, response));
        assertEquals("Invalid credentials", ex.getMessage());
    }

    @Test
    void testLogin_InvalidPassword() {
        // given
        when(employeeInformationRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(employee));
        cryptoUtilsMock.when(() -> CryptoUtils.verifyHash("plainPassword", "hashedPassword"))
                .thenReturn(false);

        // when + then
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authenticationService.login(loginRequest, response));
        assertEquals("Invalid credentials", ex.getMessage());
    }

    @Test
    void testLogout() {
        // when
        String result = authenticationService.logout(response);

        // then
        assertEquals("Logout successful", result);
        ArgumentCaptor<Cookie> captor = ArgumentCaptor.forClass(Cookie.class);
        verify(response).addCookie(captor.capture());

        Cookie cookie = captor.getValue();
        assertEquals("AUTH_TOKEN", cookie.getName());
        assertNull(cookie.getValue());
        assertEquals(0, cookie.getMaxAge());
        assertTrue(cookie.isHttpOnly());
        assertTrue(cookie.getSecure());
    }
}
