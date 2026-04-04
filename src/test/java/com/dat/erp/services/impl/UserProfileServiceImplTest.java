package com.dat.erp.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import com.dat.erp.constants.Messages;
import com.dat.erp.dto.response.PagedResponse;
import com.dat.erp.dto.response.SelectionOptionResponse;
import com.dat.erp.entities.Account;
import com.dat.erp.entities.UserProfile;
import com.dat.erp.exceptions.BadRequestException;
import com.dat.erp.mapper.interfaces.UserProfileMapper;
import com.dat.erp.repositories.customrepositories.AccountRepository;
import com.dat.erp.repositories.customrepositories.DepartmentRepository;
import com.dat.erp.repositories.customrepositories.UserProfileRepository;
import com.dat.erp.services.SecurityContextService;
import com.dat.erp.systemconfigs.CustomUserDetails;

class UserProfileServiceImplTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private UserProfileMapper userProfileMapper;

    @Mock
    private SecurityContextService securityContextService;

    @InjectMocks
    private UserProfileServiceImpl userProfileService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(userProfileService, "securityContextService", securityContextService);
    }

    @Test
    void getUserProfileOptionsByFirstName_success() {
        Account account = new Account();
        account.setCompanyCode("CMP-1");
        when(securityContextService.getCurrentUser()).thenReturn(new CustomUserDetails(account, null));

        UserProfile profile = new UserProfile();
        profile.setCode("USR-1");
        profile.setFirstName("John");
        profile.setLastName("Smith");

        when(userProfileRepository.findOptionsByFilters(eq("CMP-1"), isNull(), any()))
                .thenReturn(new PageImpl<>(List.of(profile), PageRequest.of(0, 20), 1));

        PagedResponse<SelectionOptionResponse> result = userProfileService.getUserProfileOptionsByFirstName(null, 0, 20,
                null, "ASC");

        assertNotNull(result);
        assertEquals(1, result.getData().size());
        assertEquals("USR-1", result.getData().get(0).getCode());
        assertEquals("John Smith", result.getData().get(0).getName());
        assertEquals(Messages.SUCCESS, result.getMessage());
        verify(userProfileRepository).findOptionsByFilters(eq("CMP-1"), isNull(), any());
    }

    @Test
    void getUserProfileOptionsByFirstName_withFilter() {
        Account account = new Account();
        account.setCompanyCode("CMP-1");
        when(securityContextService.getCurrentUser()).thenReturn(new CustomUserDetails(account, null));

        when(userProfileRepository.findOptionsByFilters(eq("CMP-1"), eq("Ann"), any()))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 20), 0));

        PagedResponse<SelectionOptionResponse> result = userProfileService.getUserProfileOptionsByFirstName("  Ann  ", 0,
                20, null, "ASC");

        assertNotNull(result);
        assertEquals(0, result.getData().size());
        verify(userProfileRepository).findOptionsByFilters(eq("CMP-1"), eq("Ann"), any());
    }

    @Test
    void getActiveUserProfileCodesOfCurrentCompany_success() {
        Account account = new Account();
        account.setCompanyCode("CMP-1");
        when(securityContextService.getCurrentUser()).thenReturn(new CustomUserDetails(account, null));
        when(userProfileRepository.findActiveCodesByCompanyCode("CMP-1")).thenReturn(List.of("USR-1", "USR-2"));

        List<String> result = userProfileService.getActiveUserProfileCodesOfCurrentCompany();

        assertEquals(List.of("USR-1", "USR-2"), result);
        verify(userProfileRepository).findActiveCodesByCompanyCode("CMP-1");
    }

    @Test
    void getActiveUserProfileCodesOfCurrentCompany_missingCompany_throwsBadRequest() {
        Account account = new Account();
        account.setCompanyCode(" ");
        when(securityContextService.getCurrentUser()).thenReturn(new CustomUserDetails(account, null));

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> userProfileService.getActiveUserProfileCodesOfCurrentCompany());

        assertEquals(Messages.ERROR_CURRENT_USER_COMPANY_MISSING, exception.getMessage());
        verifyNoInteractions(userProfileRepository);
    }
}
