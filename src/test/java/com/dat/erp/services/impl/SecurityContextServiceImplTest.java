package com.dat.erp.services.impl;

import com.dat.erp.entities.EmployeeInformation;
import com.dat.erp.entities.Role;
import com.dat.erp.repositories.customrepositories.EmployeeInformationRepository;
import com.dat.erp.systemconfigs.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SecurityContextServiceImplTest {

    @Mock
    private EmployeeInformationRepository employeeInformationRepository;

    @InjectMocks
    private SecurityContextServiceImpl securityContextService;

    private EmployeeInformation employee;

    private Role role;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        SecurityContextHolder.clearContext();
        role = new Role();
        role.setId(1L);
        role.setName("Admin");
        role.setDescription("Administrator role");
        role.setCode("CODE123");
        employee = new EmployeeInformation();
        employee.setCode("E123");
        employee.setNickname("John Doe");
        employee.setEmail("JohnDoe@test.com");
        employee.setPassword("hashedpassword");
        employee.setRole(role);
    }

    @Test
    void testSetCurrentUser_Success() {
        // given
        when(employeeInformationRepository.findByCode("E123"))
                .thenReturn(Optional.of(employee));

        // when
        securityContextService.setCurrentUser("E123");

        // then
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        assertTrue(principal instanceof CustomUserDetails);
        assertEquals("E123", ((CustomUserDetails) principal).getEmployee().getCode());
    }

    @Test
    void testSetCurrentUser_EmployeeNotFound() {
        // given
        when(employeeInformationRepository.findByCode("X999"))
                .thenReturn(Optional.empty());

        // when + then
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> securityContextService.setCurrentUser("X999"));
        assertEquals("Employee not found", ex.getMessage());
    }

    @Test
    void testGetCurrentUser_Success() {
        // given
        when(employeeInformationRepository.findByCode("E123"))
                .thenReturn(Optional.of(employee));
        securityContextService.setCurrentUser("E123");

        // when
        EmployeeInformation currentUser = securityContextService.getCurrentUser();

        // then
        assertNotNull(currentUser);
        assertEquals("E123", currentUser.getCode());
        assertEquals("JohnDoe@test.com", currentUser.getEmail());
    }
}
