package com.dat.erp.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.context.SecurityContextHolder;

import com.dat.erp.entities.Account;
import com.dat.erp.entities.UserProfile;
import com.dat.erp.exceptions.ResourceNotFoundException;
import com.dat.erp.exceptions.UnauthorizedException;
import com.dat.erp.repositories.customrepositories.AccountRepository;
import com.dat.erp.repositories.customrepositories.UserProfileRepository;
import com.dat.erp.systemconfigs.CustomUserDetails;

class SecurityContextServiceImplTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private UserProfileRepository userProfileRepository;

    private SecurityContextServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new SecurityContextServiceImpl(accountRepository, userProfileRepository);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void setCurrentUser_setsAuthenticationAndReturnsDetails() {
        Account account = new Account();
        account.setCode("ACC-1");

        UserProfile profile = new UserProfile();
        profile.setCode("USR-1");

        when(accountRepository.findByCode("ACC-1")).thenReturn(Optional.of(account));
        when(userProfileRepository.findByAccount_Code("ACC-1")).thenReturn(Optional.of(profile));

        CustomUserDetails details = service.setCurrentUser("ACC-1");

        assertNotNull(details);
        assertEquals("ACC-1", details.getCode());
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(details, service.getCurrentUser());
    }

    @Test
    void setCurrentUser_accountNotFound_throwsResourceNotFound() {
        when(accountRepository.findByCode("ACC-404")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.setCurrentUser("ACC-404"));
    }

    @Test
    void getCurrentUser_whenNoAuthentication_throwsUnauthorized() {
        assertThrows(UnauthorizedException.class, () -> service.getCurrentUser());
    }
}

