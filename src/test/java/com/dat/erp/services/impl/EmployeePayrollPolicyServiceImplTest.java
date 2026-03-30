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
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.dat.erp.constants.Messages;
import com.dat.erp.dto.request.EmployeePayrollPolicyBatchRequest;
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
    private EmployeePayrollPolicyBatchRequest batchRequest;
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

        batchRequest = new EmployeePayrollPolicyBatchRequest();
        batchRequest.setPolicyCode("PPL-001");
        batchRequest.setEmployeeCodes(List.of("USR-001", "USR-002"));
        batchRequest.setEffectiveFrom(LocalDate.of(2026, 3, 22));
        batchRequest.setEffectiveTo(LocalDate.of(2026, 4, 22));

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
        when(employeePayrollPolicyRepository.findActiveOverlapUserProfileCodes("CMP-001", List.of("USR-001"),
                request.getEffectiveFrom(), request.getEffectiveTo())).thenReturn(List.of());
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
        when(employeePayrollPolicyRepository.findActiveOverlapUserProfileCodes("CMP-001", List.of("USR-001"),
                request.getEffectiveFrom(), request.getEffectiveTo())).thenReturn(List.of("USR-001"));

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

    @Test
    void applyPayrollPolicyToEmployees_success() {
        UserProfile secondUserProfile = new UserProfile();
        secondUserProfile.setCode("USR-002");
        secondUserProfile.setCompanyCode("CMP-001");

        when(payrollPolicyRepository.findByCodeAndIsDeletedFalse("PPL-001")).thenReturn(Optional.of(payrollPolicy));
        when(userProfileRepository.findAllByCodeInAndIsDeletedFalseForUpdate(List.of("USR-001", "USR-002")))
                .thenReturn(List.of(userProfile, secondUserProfile));
        when(employeePayrollPolicyRepository.findActiveOverlapUserProfileCodes("CMP-001", List.of("USR-001", "USR-002"),
                batchRequest.getEffectiveFrom(), batchRequest.getEffectiveTo()))
                .thenReturn(List.of());
        when(codeGenerator.nextCode("EPP-")).thenReturn("EPP-000001", "EPP-000002");
        when(employeePayrollPolicyRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        List<EmployeePayrollPolicyResponse> responses = employeePayrollPolicyService.applyPayrollPolicyToEmployees(batchRequest);

        assertEquals(2, responses.size());
        assertEquals("USR-001", responses.get(0).getUserProfileCode());
        assertEquals("USR-002", responses.get(1).getUserProfileCode());
        assertEquals("PPL-001", responses.get(0).getPayrollPolicyCode());
        assertEquals("PPL-001", responses.get(1).getPayrollPolicyCode());
    }

    @Test
    void applyPayrollPolicyToEmployees_conflictWhenAnyEmployeeOverlaps() {
        UserProfile secondUserProfile = new UserProfile();
        secondUserProfile.setCode("USR-002");
        secondUserProfile.setCompanyCode("CMP-001");

        when(payrollPolicyRepository.findByCodeAndIsDeletedFalse("PPL-001")).thenReturn(Optional.of(payrollPolicy));
        when(userProfileRepository.findAllByCodeInAndIsDeletedFalseForUpdate(List.of("USR-001", "USR-002")))
                .thenReturn(List.of(userProfile, secondUserProfile));
        when(employeePayrollPolicyRepository.findActiveOverlapUserProfileCodes("CMP-001", List.of("USR-001", "USR-002"),
                batchRequest.getEffectiveFrom(), batchRequest.getEffectiveTo()))
                .thenReturn(List.of("USR-002"));

        ConflictException ex = assertThrows(ConflictException.class,
                () -> employeePayrollPolicyService.applyPayrollPolicyToEmployees(batchRequest));

        assertEquals(Messages.ERROR_EMPLOYEE_PAYROLL_POLICY_OVERLAPS, ex.getMessage());
        verify(employeePayrollPolicyRepository, never()).saveAll(any());
    }

    @Test
    void getCompanyPoliciesByEmployeeCodesAndDate_returnsPolicyMapByEmployeeCode() {
        UserProfile secondUserProfile = new UserProfile();
        secondUserProfile.setCode("USR-002");
        secondUserProfile.setCompanyCode("CMP-001");

        PayrollPolicy secondPayrollPolicy = new PayrollPolicy();
        secondPayrollPolicy.setCode("PPL-002");
        secondPayrollPolicy.setCompanyCode("CMP-001");

        EmployeePayrollPolicy firstEmployeePolicy = EmployeePayrollPolicy.builder()
                .userProfile(userProfile)
                .payrollPolicy(payrollPolicy)
                .effectiveFrom(LocalDate.of(2026, 1, 1))
                .effectiveTo(LocalDate.of(2026, 12, 31))
                .isActive(true)
                .build();
        EmployeePayrollPolicy secondEmployeePolicy = EmployeePayrollPolicy.builder()
                .userProfile(secondUserProfile)
                .payrollPolicy(secondPayrollPolicy)
                .effectiveFrom(LocalDate.of(2026, 1, 1))
                .effectiveTo(LocalDate.of(2026, 12, 31))
                .isActive(true)
                .build();

        when(employeePayrollPolicyRepository.findActivePoliciesByEmployeeCodesAndDate(
                "CMP-001",
                List.of("USR-001", "USR-002"),
                LocalDate.of(2026, 3, 25)))
                .thenReturn(List.of(firstEmployeePolicy, secondEmployeePolicy));

        Map<String, PayrollPolicy> result = employeePayrollPolicyService.getCompanyPoliciesByEmployeeCodesAndDate(
                List.of(" USR-001 ", "USR-002", "USR-001"),
                LocalDate.of(2026, 3, 25));

        assertEquals(2, result.size());
        assertEquals("PPL-001", result.get("USR-001").getCode());
        assertEquals("PPL-002", result.get("USR-002").getCode());
    }
}
