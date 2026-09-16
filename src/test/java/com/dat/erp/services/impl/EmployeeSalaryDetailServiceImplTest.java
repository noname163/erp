package com.dat.erp.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.dat.erp.constants.Messages;
import com.dat.erp.constants.DayType;
import com.dat.erp.dto.request.EmployeeSalaryDetailRequest;
import com.dat.erp.entities.Account;
import com.dat.erp.entities.EmployeeSalary;
import com.dat.erp.entities.EmployeeSalaryDetail;
import com.dat.erp.entities.Salary;
import com.dat.erp.entities.UserProfile;
import com.dat.erp.exceptions.BadRequestException;
import com.dat.erp.exceptions.ConflictException;
import com.dat.erp.exceptions.ResourceNotFoundException;
import com.dat.erp.repositories.customrepositories.EmployeeSalaryDetailRepository;
import com.dat.erp.repositories.customrepositories.EmployeeSalaryRepository;
import com.dat.erp.repositories.customrepositories.SalaryRepository;
import com.dat.erp.services.CodeGenerator;
import com.dat.erp.services.SecurityContextService;
import com.dat.erp.systemconfigs.CustomUserDetails;

class EmployeeSalaryDetailServiceImplTest {

    @Mock
    private EmployeeSalaryDetailRepository employeeSalaryDetailRepository;

    @Mock
    private EmployeeSalaryRepository employeeSalaryRepository;

    @Mock
    private SalaryRepository salaryRepository;

    @Mock
    private CodeGenerator codeGenerator;

    @Mock
    private SecurityContextService securityContextService;

    @InjectMocks
    private EmployeeSalaryDetailServiceImpl employeeSalaryDetailService;

    private List<EmployeeSalaryDetailRequest> requests;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        EmployeeSalaryDetailRequest base = new EmployeeSalaryDetailRequest();
        base.setEmployeeSalaryCode("ESL-1");
        base.setSalaryCode("BASE");
        base.setAmount("15000000");
        base.setDayType(DayType.NORMAL);
        base.setIsFixed(Boolean.TRUE);

        EmployeeSalaryDetailRequest allowance = new EmployeeSalaryDetailRequest();
        allowance.setEmployeeSalaryCode("ESL-1");
        allowance.setSalaryCode("ALLOWANCE");
        allowance.setAmount("3000000");
        allowance.setDayType(DayType.NORMAL);

        requests = Arrays.asList(base, allowance);
    }

    @Test
    void createEmployeeSalaryDetails_success() {
        Account currentUserAccount = new Account();
        com.dat.erp.testutils.EntityTestData.setCode(currentUserAccount, "ACC-1");
        com.dat.erp.testutils.EntityTestData.setCompanyCode(currentUserAccount, "CMP-1");
        when(securityContextService.getCurrentUser()).thenReturn(new CustomUserDetails(currentUserAccount, null));
        when(securityContextService.getCurrentCompanyCode()).thenReturn(currentUserAccount.getCompanyCode());

        UserProfile userProfile = new UserProfile();
        com.dat.erp.testutils.EntityTestData.setCode(userProfile, "EMP001");
        EmployeeSalary employeeSalary = new EmployeeSalary();
        com.dat.erp.testutils.EntityTestData.setCode(employeeSalary, "ESL-1");
        com.dat.erp.testutils.EntityTestData.setCompanyCode(employeeSalary, "CMP-1");
        employeeSalary.setUserProfile(userProfile);
        when(employeeSalaryRepository.findByCodeAndIsDeletedFalse("ESL-1")).thenReturn(Optional.of(employeeSalary));

        when(employeeSalaryDetailRepository.findExistingSalaryCodes(eq("ESL-1"), anyCollection())).thenReturn(List.of());

        Salary baseSalary = new Salary();
        com.dat.erp.testutils.EntityTestData.setCode(baseSalary, "BASE");
        Salary allowanceSalary = new Salary();
        com.dat.erp.testutils.EntityTestData.setCode(allowanceSalary, "ALLOWANCE");
        when(salaryRepository.findAllByCodeIn(anyCollection())).thenReturn(List.of(baseSalary, allowanceSalary));

        when(codeGenerator.nextCode("ESD-")).thenReturn("ESD-000001", "ESD-000002");
        when(employeeSalaryDetailRepository.saveAll(anyList())).thenAnswer(invocation -> {
            List<EmployeeSalaryDetail> saved = invocation.getArgument(0);
            com.dat.erp.testutils.EntityTestData.setCode(saved.get(0), "ESD-000001");
            com.dat.erp.testutils.EntityTestData.setCode(saved.get(1), "ESD-000002");
            return saved;
        });

        String result = employeeSalaryDetailService.createEmployeeSalaryDetails(requests, employeeSalary.getCode());

        assertEquals(String.format(Messages.EMPLOYEE_SALARY_DETAIL_CREATE_SUCCESS, "EMP001"), result);

        ArgumentCaptor<List<EmployeeSalaryDetail>> captor = ArgumentCaptor.forClass(List.class);
        verify(employeeSalaryDetailRepository).saveAll(captor.capture());
        assertEquals(2, captor.getValue().size());
        assertEquals("ESD-000001", captor.getValue().get(0).getCode());
        assertEquals("ESD-000002", captor.getValue().get(1).getCode());
        assertEquals("BASE", captor.getValue().get(0).getSalary().getCode());
        assertEquals("ALLOWANCE", captor.getValue().get(1).getSalary().getCode());
        assertEquals(Boolean.TRUE, captor.getValue().get(0).getIsFixed());
        assertEquals(DayType.NORMAL, captor.getValue().get(0).getDayType());
    }

    @Test
    void createEmployeeSalaryDetails_conflictWhenDuplicateSalaryCodeInPayload() {
        EmployeeSalaryDetailRequest duplicate = new EmployeeSalaryDetailRequest();
        duplicate.setEmployeeSalaryCode("ESL-1");
        duplicate.setSalaryCode("BASE");
        duplicate.setAmount("15000000");
        duplicate.setDayType(DayType.NORMAL);
        requests = Arrays.asList(requests.get(0), duplicate);

        Account currentUserAccount = new Account();
        com.dat.erp.testutils.EntityTestData.setCode(currentUserAccount, "ACC-1");
        com.dat.erp.testutils.EntityTestData.setCompanyCode(currentUserAccount, "CMP-1");
        when(securityContextService.getCurrentUser()).thenReturn(new CustomUserDetails(currentUserAccount, null));
        when(securityContextService.getCurrentCompanyCode()).thenReturn(currentUserAccount.getCompanyCode());

        EmployeeSalary employeeSalary = new EmployeeSalary();
        com.dat.erp.testutils.EntityTestData.setCode(employeeSalary, "ESL-1");
        com.dat.erp.testutils.EntityTestData.setCompanyCode(employeeSalary, "CMP-1");
        employeeSalary.setUserProfile(new UserProfile());
        when(employeeSalaryRepository.findByCodeAndIsDeletedFalse("ESL-1")).thenReturn(Optional.of(employeeSalary));

        when(employeeSalaryDetailRepository.findExistingSalaryCodes(eq("ESL-1"), anyCollection())).thenReturn(List.of());
        when(codeGenerator.nextCode("ESD-")).thenReturn("ESD-000001");

        ConflictException ex = assertThrows(ConflictException.class,
                () -> employeeSalaryDetailService.createEmployeeSalaryDetails(requests, employeeSalary.getCode()));
        assertEquals(Messages.ERROR_EMPLOYEE_SALARY_DETAIL_ALREADY_EXISTS, ex.getMessage());
        verify(employeeSalaryDetailRepository, never()).saveAll(anyList());
    }

    @Test
    void createEmployeeSalaryDetails_badRequestWhenDependenceCodeNotInRequestList() {
        requests.get(1).setDependenceCode("BONUS");

        Account currentUserAccount = new Account();
        com.dat.erp.testutils.EntityTestData.setCode(currentUserAccount, "ACC-1");
        com.dat.erp.testutils.EntityTestData.setCompanyCode(currentUserAccount, "CMP-1");
        when(securityContextService.getCurrentUser()).thenReturn(new CustomUserDetails(currentUserAccount, null));
        when(securityContextService.getCurrentCompanyCode()).thenReturn(currentUserAccount.getCompanyCode());

        EmployeeSalary employeeSalary = new EmployeeSalary();
        com.dat.erp.testutils.EntityTestData.setCode(employeeSalary, "ESL-1");
        com.dat.erp.testutils.EntityTestData.setCompanyCode(employeeSalary, "CMP-1");
        employeeSalary.setUserProfile(new UserProfile());
        when(employeeSalaryRepository.findByCodeAndIsDeletedFalse("ESL-1")).thenReturn(Optional.of(employeeSalary));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> employeeSalaryDetailService.createEmployeeSalaryDetails(requests, employeeSalary.getCode()));
        assertEquals(Messages.ERROR_EMPLOYEE_SALARY_DETAIL_DEPENDENCE_CODE_MUST_EXIST_IN_REQUEST, ex.getMessage());
        verify(employeeSalaryDetailRepository, never()).saveAll(anyList());
    }

    @Test
    void createEmployeeSalaryDetails_successWithDependenceCodeInRequestList() {
        requests.get(1).setDependenceCode("BASE");

        Account currentUserAccount = new Account();
        com.dat.erp.testutils.EntityTestData.setCode(currentUserAccount, "ACC-1");
        com.dat.erp.testutils.EntityTestData.setCompanyCode(currentUserAccount, "CMP-1");
        when(securityContextService.getCurrentUser()).thenReturn(new CustomUserDetails(currentUserAccount, null));
        when(securityContextService.getCurrentCompanyCode()).thenReturn(currentUserAccount.getCompanyCode());

        UserProfile userProfile = new UserProfile();
        com.dat.erp.testutils.EntityTestData.setCode(userProfile, "EMP001");
        EmployeeSalary employeeSalary = new EmployeeSalary();
        com.dat.erp.testutils.EntityTestData.setCode(employeeSalary, "ESL-1");
        com.dat.erp.testutils.EntityTestData.setCompanyCode(employeeSalary, "CMP-1");
        employeeSalary.setUserProfile(userProfile);
        when(employeeSalaryRepository.findByCodeAndIsDeletedFalse("ESL-1")).thenReturn(Optional.of(employeeSalary));
        when(employeeSalaryDetailRepository.findExistingSalaryCodes(eq("ESL-1"), anyCollection())).thenReturn(List.of());

        Salary baseSalary = new Salary();
        com.dat.erp.testutils.EntityTestData.setCode(baseSalary, "BASE");
        Salary allowanceSalary = new Salary();
        com.dat.erp.testutils.EntityTestData.setCode(allowanceSalary, "ALLOWANCE");
        when(salaryRepository.findAllByCodeIn(anyCollection())).thenReturn(List.of(baseSalary, allowanceSalary));

        when(codeGenerator.nextCode("ESD-")).thenReturn("ESD-000001", "ESD-000002");
        when(employeeSalaryDetailRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        employeeSalaryDetailService.createEmployeeSalaryDetails(requests, employeeSalary.getCode());

        ArgumentCaptor<List<EmployeeSalaryDetail>> captor = ArgumentCaptor.forClass(List.class);
        verify(employeeSalaryDetailRepository).saveAll(captor.capture());
        assertEquals("BASE", captor.getValue().get(1).getDependenceCode().getCode());
    }

    @Test
    void createEmployeeSalaryDetails_notFoundWhenEmployeeSalaryMissing() {
        Account currentUserAccount = new Account();
        com.dat.erp.testutils.EntityTestData.setCode(currentUserAccount, "ACC-1");
        com.dat.erp.testutils.EntityTestData.setCompanyCode(currentUserAccount, "CMP-1");
        when(securityContextService.getCurrentUser()).thenReturn(new CustomUserDetails(currentUserAccount, null));
        when(securityContextService.getCurrentCompanyCode()).thenReturn(currentUserAccount.getCompanyCode());

        when(employeeSalaryRepository.findByCodeAndIsDeletedFalse("ESL-1")).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> employeeSalaryDetailService.createEmployeeSalaryDetails(requests, "ESL-1"));
        assertEquals(String.format(Messages.ERROR_EMPLOYEE_SALARY_NOT_FOUND_WITH_CODE, "ESL-1"), ex.getMessage());
        verify(employeeSalaryDetailRepository, never()).saveAll(anyList());
    }

    @Test
    void createEmployeeSalaryDetails_badRequestWhenCompanyMismatch() {
        Account currentUserAccount = new Account();
        com.dat.erp.testutils.EntityTestData.setCode(currentUserAccount, "ACC-1");
        com.dat.erp.testutils.EntityTestData.setCompanyCode(currentUserAccount, "CMP-1");
        when(securityContextService.getCurrentUser()).thenReturn(new CustomUserDetails(currentUserAccount, null));
        when(securityContextService.getCurrentCompanyCode()).thenReturn(currentUserAccount.getCompanyCode());

        EmployeeSalary employeeSalary = new EmployeeSalary();
        com.dat.erp.testutils.EntityTestData.setCode(employeeSalary, "ESL-1");
        com.dat.erp.testutils.EntityTestData.setCompanyCode(employeeSalary, "CMP-2");
        employeeSalary.setUserProfile(new UserProfile());
        when(employeeSalaryRepository.findByCodeAndIsDeletedFalse("ESL-1")).thenReturn(Optional.of(employeeSalary));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> employeeSalaryDetailService.createEmployeeSalaryDetails(requests, employeeSalary.getCode()));
        assertEquals(Messages.ERROR_EMPLOYEE_SALARY_DETAIL_EMPLOYEE_SALARY_COMPANY_MISMATCH, ex.getMessage());
        verify(employeeSalaryDetailRepository, never()).saveAll(anyList());
    }
}
