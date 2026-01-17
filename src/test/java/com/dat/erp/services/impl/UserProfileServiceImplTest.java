package com.dat.erp.services.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.dat.erp.constants.CodePrefixes;
import com.dat.erp.constants.Messages;
import com.dat.erp.dto.request.UserProfileCreateRequest;
import com.dat.erp.entities.Account;
import com.dat.erp.entities.Department;
import com.dat.erp.entities.Role;
import com.dat.erp.entities.UserProfile;
import com.dat.erp.exceptions.BadRequestException;
import com.dat.erp.mapper.interfaces.UserProfileMapper;
import com.dat.erp.repositories.customrepositories.AccountRepository;
import com.dat.erp.repositories.customrepositories.DepartmentRepository;
import com.dat.erp.repositories.customrepositories.UserProfileRepository;
import com.dat.erp.services.CodeGenerator;
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
    private CodeGenerator codeGenerator;
    @Mock
    private SecurityContextService securityContextService;

    private UserProfileServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        UserProfileMapper mapper = Mappers.getMapper(UserProfileMapper.class);
        service = new UserProfileServiceImpl(accountRepository, departmentRepository, userProfileRepository, mapper);
        ReflectionTestUtils.setField(service, "codeGenerator", codeGenerator);
        ReflectionTestUtils.setField(service, "securityContextService", securityContextService);
    }

    @Test
    void createUserProfile_success_setsRelationsAndAuditAndPersists() {
        UserProfileCreateRequest request = new UserProfileCreateRequest();
        request.setAccountCode("ACC-000001");
        request.setFirstName("Nguyen");
        request.setLastName("Van A");
        request.setDepartmentCode("DPM-IT");

        Account account = new Account();
        account.setCode("ACC-000001");
        account.setCompanyCode("CMP-1");
        Role role = new Role();
        role.setName("EMPLOYEE");
        account.setRole(role);

        Department department = new Department();
        department.setCode("DPM-IT");
        department.setName("IT");
        department.setCompanyCode("CMP-1");

        when(accountRepository.findByCode("ACC-000001")).thenReturn(Optional.of(account));
        when(departmentRepository.findByCodeAndCompanyCode("DPM-IT", "CMP-1")).thenReturn(Optional.of(department));
        when(codeGenerator.nextCode(CodePrefixes.USER)).thenReturn("USR-000123");
        when(userProfileRepository.save(any(UserProfile.class))).thenAnswer(inv -> inv.getArgument(0));

        Account actor = new Account();
        actor.setCode("ACC-ACTOR");
        actor.setCompanyCode("CMP-1");
        when(securityContextService.getCurrentUser()).thenReturn(new CustomUserDetails(actor, null));

        UserProfile profile = service.createUserProfile(request);

        assertNotNull(profile);
        assertEquals("USR-000123", profile.getCode());
        assertEquals("Nguyen", profile.getFirstName());
        assertEquals("Van A", profile.getLastName());
        assertEquals(account, profile.getAccount());
        assertEquals(department, profile.getDepartment());
        assertEquals("CMP-1", profile.getCompanyCode());
        assertThat(profile.getHireDate()).isNotNull();
        assertThat(profile.getIsActive()).isTrue();
        assertThat(profile.getCreatedAt()).isNotNull();
        assertThat(profile.getUpdatedAt()).isNotNull();
        assertThat(profile.getCreatedBy()).isEqualTo("ACC-ACTOR");
        assertThat(profile.getUpdatedBy()).isEqualTo("ACC-ACTOR");

        verify(accountRepository).findByCode(eq("ACC-000001"));
        verify(departmentRepository).findByCodeAndCompanyCode(eq("DPM-IT"), eq("CMP-1"));
        verify(userProfileRepository).save(any(UserProfile.class));
    }

    @Test
    void createUserProfile_accountNotFound_throwsBadRequest() {
        UserProfileCreateRequest request = new UserProfileCreateRequest();
        request.setAccountCode("ACC-MISSING");
        request.setFirstName("Nguyen");
        request.setLastName("Van A");
        request.setDepartmentCode("DPM-IT");

        when(accountRepository.findByCode("ACC-MISSING")).thenReturn(Optional.empty());

        BadRequestException ex = assertThrows(BadRequestException.class, () -> service.createUserProfile(request));

        assertEquals(String.format(Messages.ERROR_ACCOUNT_NOT_FOUND_WITH_CODE, "ACC-MISSING"), ex.getMessage());
        verify(userProfileRepository, never()).save(any());
    }

    @Test
    void createUserProfile_accountCompanyMissing_throwsBadRequest() {
        UserProfileCreateRequest request = new UserProfileCreateRequest();
        request.setAccountCode("ACC-000001");
        request.setFirstName("Nguyen");
        request.setLastName("Van A");
        request.setDepartmentCode("DPM-IT");

        Account account = new Account();
        account.setCode("ACC-000001");
        account.setCompanyCode("  ");
        when(accountRepository.findByCode("ACC-000001")).thenReturn(Optional.of(account));

        BadRequestException ex = assertThrows(BadRequestException.class, () -> service.createUserProfile(request));

        assertEquals(Messages.ERROR_CURRENT_USER_COMPANY_MISSING, ex.getMessage());
        verify(departmentRepository, never()).findByCodeAndCompanyCode(any(), any());
        verify(userProfileRepository, never()).save(any());
    }

    @Test
    void createUserProfile_departmentNotFound_throwsBadRequest() {
        UserProfileCreateRequest request = new UserProfileCreateRequest();
        request.setAccountCode("ACC-000001");
        request.setFirstName("Nguyen");
        request.setLastName("Van A");
        request.setDepartmentCode("DPM-IT");

        Account account = new Account();
        account.setCode("ACC-000001");
        account.setCompanyCode("CMP-1");
        when(accountRepository.findByCode("ACC-000001")).thenReturn(Optional.of(account));

        when(departmentRepository.findByCodeAndCompanyCode("DPM-IT", "CMP-1")).thenReturn(Optional.empty());

        BadRequestException ex = assertThrows(BadRequestException.class, () -> service.createUserProfile(request));

        assertEquals(String.format(Messages.ERROR_DEPARTMENT_NOT_FOUND_WITH_CODE, "DPM-IT"), ex.getMessage());
        verify(userProfileRepository, never()).save(any());
    }
}

