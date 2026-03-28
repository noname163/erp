package com.dat.erp.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.dat.erp.constants.Messages;
import com.dat.erp.dto.request.EmployeePayrollPolicyRequest;
import com.dat.erp.dto.response.EmployeePayrollPolicyResponse;
import com.dat.erp.entities.Account;
import com.dat.erp.entities.EmployeePayrollPolicy;
import com.dat.erp.entities.PayrollPolicy;
import com.dat.erp.entities.UserProfile;
import com.dat.erp.exceptions.ConflictException;
import com.dat.erp.repositories.customrepositories.EmployeePayrollPolicyRepository;
import com.dat.erp.repositories.customrepositories.PayrollPolicyRepository;
import com.dat.erp.repositories.customrepositories.UserProfileRepository;
import com.dat.erp.services.CodeGenerator;
import com.dat.erp.services.SecurityContextService;
import com.dat.erp.systemconfigs.CustomUserDetails;

class EmployeePayrollPolicyServiceImplTest {

    @Mock
    private EmployeePayrollPolicyRepository employeePayrollPolicyRepository;

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private PayrollPolicyRepository payrollPolicyRepository;

    @Mock
    private CodeGenerator codeGenerator;

    @Mock
    private SecurityContextService securityContextService;

    @InjectMocks
    private EmployeePayrollPolicyServiceImpl employeePayrollPolicyService;

    private EmployeePayrollPolicyRequest request;
    private UserProfile userProfile;
    private PayrollPolicy payrollPolicy;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        request = new EmployeePayrollPolicyRequest();
        request.setUserProfileCode("USR-001");
        request.setPayrollPolicyCode("PPL-001");
        request.setEffectiveFrom(LocalDate.of(2026, 3, 22));
        request.setEffectiveTo(LocalDate.of(2026, 4, 22));

        userProfile = new UserProfile();
        userProfile.setCode("USR-001");
        userProfile.setCompanyCode("CMP-001");

        payrollPolicy = new PayrollPolicy();
        payrollPolicy.setCode("PPL-001");
        payrollPolicy.setCompanyCode("CMP-001");

        Account account = new Account();
        account.setCode("ACC-001");
        account.setCompanyCode("CMP-001");
        when(securityContextService.getCurrentUser()).thenReturn(new CustomUserDetails(account, null));
    }

    @Test
    void createEmployeePayrollPolicy_successWhenNoActiveOverlap() {
        when(userProfileRepository.findByCodeAndIsDeletedFalseForUpdate("USR-001")).thenReturn(Optional.of(userProfile));
        when(payrollPolicyRepository.findByCodeAndIsDeletedFalse("PPL-001")).thenReturn(Optional.of(payrollPolicy));
        when(employeePayrollPolicyRepository.existsActiveOverlap("USR-001", request.getEffectiveFrom(),
                request.getEffectiveTo(), null)).thenReturn(false);
        when(codeGenerator.nextCode("EPP-")).thenReturn("EPP-000001");
        when(employeePayrollPolicyRepository.save(any(EmployeePayrollPolicy.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EmployeePayrollPolicyResponse response = employeePayrollPolicyService.createEmployeePayrollPolicy(request);

        assertNotNull(response);
        assertEquals("EPP-000001", response.getCode());
        assertEquals("USR-001", response.getUserProfileCode());
        assertEquals("PPL-001", response.getPayrollPolicyCode());
        assertEquals(Boolean.TRUE, response.getIsActive());

        ArgumentCaptor<EmployeePayrollPolicy> captor = ArgumentCaptor.forClass(EmployeePayrollPolicy.class);
        verify(employeePayrollPolicyRepository).save(captor.capture());
        assertEquals(Boolean.TRUE, captor.getValue().getIsActive());
    }

    @Test
    void createEmployeePayrollPolicy_conflictWhenActiveOverlapExists() {
        when(userProfileRepository.findByCodeAndIsDeletedFalseForUpdate("USR-001")).thenReturn(Optional.of(userProfile));
        when(payrollPolicyRepository.findByCodeAndIsDeletedFalse("PPL-001")).thenReturn(Optional.of(payrollPolicy));
        when(employeePayrollPolicyRepository.existsActiveOverlap("USR-001", request.getEffectiveFrom(),
                request.getEffectiveTo(), null)).thenReturn(true);

        ConflictException ex = assertThrows(ConflictException.class,
                () -> employeePayrollPolicyService.createEmployeePayrollPolicy(request));

        assertEquals(Messages.ERROR_EMPLOYEE_PAYROLL_POLICY_OVERLAPS, ex.getMessage());
        verify(employeePayrollPolicyRepository, never()).save(any(EmployeePayrollPolicy.class));
    }

    @Test
    void deactivateEmployeePayrollPolicy_marksPolicyInactive() {
        EmployeePayrollPolicy employeePayrollPolicy = EmployeePayrollPolicy.builder()
                .userProfile(userProfile)
                .payrollPolicy(payrollPolicy)
                .effectiveFrom(request.getEffectiveFrom())
                .effectiveTo(request.getEffectiveTo())
                .isActive(true)
                .build();
        employeePayrollPolicy.setCode("EPP-000001");
        employeePayrollPolicy.setCompanyCode("CMP-001");

        when(employeePayrollPolicyRepository.findByCodeAndIsDeletedFalse("EPP-000001"))
                .thenReturn(Optional.of(employeePayrollPolicy));
        when(employeePayrollPolicyRepository.save(any(EmployeePayrollPolicy.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EmployeePayrollPolicyResponse response = employeePayrollPolicyService.deactivateEmployeePayrollPolicy("EPP-000001");

        assertFalse(response.getIsActive());
        verify(employeePayrollPolicyRepository).save(employeePayrollPolicy);
    }

    @Test
    void getEmployeePayrollPolicies_returnsAssignmentsForEmployee() {
        EmployeePayrollPolicy employeePayrollPolicy = EmployeePayrollPolicy.builder()
                .userProfile(userProfile)
                .payrollPolicy(payrollPolicy)
                .effectiveFrom(request.getEffectiveFrom())
                .effectiveTo(request.getEffectiveTo())
                .isActive(true)
                .build();

        when(userProfileRepository.findByCodeAndIsDeletedFalse("USR-001")).thenReturn(Optional.of(userProfile));
        when(employeePayrollPolicyRepository.findByUserProfile_CodeAndIsDeletedFalseOrderByEffectiveFromDesc("USR-001"))
                .thenReturn(List.of(employeePayrollPolicy));

        List<EmployeePayrollPolicyResponse> responses = employeePayrollPolicyService.getEmployeePayrollPolicies("USR-001");

        assertEquals(1, responses.size());
        assertEquals("USR-001", responses.get(0).getUserProfileCode());
        assertEquals("PPL-001", responses.get(0).getPayrollPolicyCode());
    }
}
