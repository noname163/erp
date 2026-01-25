package com.dat.erp.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
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
import com.dat.erp.dto.request.EmployeeSalaryDetailRequest;
import com.dat.erp.entities.Account;
import com.dat.erp.entities.EmployeeSalary;
import com.dat.erp.entities.EmployeeSalaryDetail;
import com.dat.erp.entities.Salary;
import com.dat.erp.entities.SystemUnit;
import com.dat.erp.entities.UserProfile;
import com.dat.erp.exceptions.BadRequestException;
import com.dat.erp.exceptions.ConflictException;
import com.dat.erp.exceptions.ResourceNotFoundException;
import com.dat.erp.repositories.customrepositories.EmployeeSalaryDetailRepository;
import com.dat.erp.repositories.customrepositories.EmployeeSalaryRepository;
import com.dat.erp.repositories.customrepositories.SalaryRepository;
import com.dat.erp.repositories.customrepositories.SystemUnitRepository;
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
    private SystemUnitRepository systemUnitRepository;

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
        base.setQuantity(1);
        base.setUnitCode("MONTH");

        EmployeeSalaryDetailRequest allowance = new EmployeeSalaryDetailRequest();
        allowance.setEmployeeSalaryCode("ESL-1");
        allowance.setSalaryCode("ALLOWANCE");
        allowance.setAmount("3000000");
        allowance.setQuantity(1);
        allowance.setUnitCode("MONTH");

        requests = Arrays.asList(base, allowance);
    }

    @Test
    void createEmployeeSalaryDetails_success() {
        Account currentUserAccount = new Account();
        currentUserAccount.setCode("ACC-1");
        currentUserAccount.setCompanyCode("CMP-1");
        when(securityContextService.getCurrentUser()).thenReturn(new CustomUserDetails(currentUserAccount, null));

        UserProfile userProfile = new UserProfile();
        userProfile.setCode("EMP001");
        EmployeeSalary employeeSalary = new EmployeeSalary();
        employeeSalary.setCode("ESL-1");
        employeeSalary.setCompanyCode("CMP-1");
        employeeSalary.setUserProfile(userProfile);
        when(employeeSalaryRepository.findByCodeAndIsDeletedFalse("ESL-1")).thenReturn(Optional.of(employeeSalary));

        when(employeeSalaryDetailRepository.existsByEmployeeSalary_CodeAndSalary_CodeAndIsDeletedFalse(eq("ESL-1"), any()))
                .thenReturn(false);

        when(salaryRepository.findByCode("BASE")).thenReturn(Optional.of(new Salary()));
        when(salaryRepository.findByCode("ALLOWANCE")).thenReturn(Optional.of(new Salary()));
        when(systemUnitRepository.findByCode("MONTH")).thenReturn(Optional.of(new SystemUnit()));

        when(codeGenerator.nextCode("ESD-")).thenReturn("ESD-000001", "ESD-000002");
        when(employeeSalaryDetailRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        String result = employeeSalaryDetailService.createEmployeeSalaryDetails(requests);

        assertEquals(String.format(Messages.EMPLOYEE_SALARY_DETAIL_CREATE_SUCCESS, "EMP001"), result);

        ArgumentCaptor<List<EmployeeSalaryDetail>> captor = ArgumentCaptor.forClass(List.class);
        verify(employeeSalaryDetailRepository).saveAll(captor.capture());
        assertEquals(2, captor.getValue().size());
        assertEquals("ESD-000001", captor.getValue().get(0).getCode());
        assertEquals("ESD-000002", captor.getValue().get(1).getCode());
    }

    @Test
    void createEmployeeSalaryDetails_conflictWhenDuplicateSalaryCodeInPayload() {
        EmployeeSalaryDetailRequest duplicate = new EmployeeSalaryDetailRequest();
        duplicate.setEmployeeSalaryCode("ESL-1");
        duplicate.setSalaryCode("BASE");
        duplicate.setAmount("15000000");
        duplicate.setQuantity(1);
        duplicate.setUnitCode("MONTH");
        requests = Arrays.asList(requests.get(0), duplicate);

        Account currentUserAccount = new Account();
        currentUserAccount.setCode("ACC-1");
        currentUserAccount.setCompanyCode("CMP-1");
        when(securityContextService.getCurrentUser()).thenReturn(new CustomUserDetails(currentUserAccount, null));

        EmployeeSalary employeeSalary = new EmployeeSalary();
        employeeSalary.setCode("ESL-1");
        employeeSalary.setCompanyCode("CMP-1");
        employeeSalary.setUserProfile(new UserProfile());
        when(employeeSalaryRepository.findByCodeAndIsDeletedFalse("ESL-1")).thenReturn(Optional.of(employeeSalary));

        when(employeeSalaryDetailRepository.existsByEmployeeSalary_CodeAndSalary_CodeAndIsDeletedFalse(eq("ESL-1"), any()))
                .thenReturn(false);
        when(salaryRepository.findByCode("BASE")).thenReturn(Optional.of(new Salary()));
        when(systemUnitRepository.findByCode("MONTH")).thenReturn(Optional.of(new SystemUnit()));
        when(codeGenerator.nextCode("ESD-")).thenReturn("ESD-000001");

        ConflictException ex = assertThrows(ConflictException.class,
                () -> employeeSalaryDetailService.createEmployeeSalaryDetails(requests));
        assertEquals(Messages.ERROR_EMPLOYEE_SALARY_DETAIL_ALREADY_EXISTS, ex.getMessage());
        verify(employeeSalaryDetailRepository, never()).saveAll(anyList());
    }

    @Test
    void createEmployeeSalaryDetails_notFoundWhenEmployeeSalaryMissing() {
        Account currentUserAccount = new Account();
        currentUserAccount.setCode("ACC-1");
        currentUserAccount.setCompanyCode("CMP-1");
        when(securityContextService.getCurrentUser()).thenReturn(new CustomUserDetails(currentUserAccount, null));

        when(employeeSalaryRepository.findByCodeAndIsDeletedFalse("ESL-1")).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> employeeSalaryDetailService.createEmployeeSalaryDetails(requests));
        assertEquals(String.format(Messages.ERROR_EMPLOYEE_SALARY_NOT_FOUND_WITH_CODE, "ESL-1"), ex.getMessage());
        verify(employeeSalaryDetailRepository, never()).saveAll(anyList());
    }

    @Test
    void createEmployeeSalaryDetails_badRequestWhenCompanyMismatch() {
        Account currentUserAccount = new Account();
        currentUserAccount.setCode("ACC-1");
        currentUserAccount.setCompanyCode("CMP-1");
        when(securityContextService.getCurrentUser()).thenReturn(new CustomUserDetails(currentUserAccount, null));

        EmployeeSalary employeeSalary = new EmployeeSalary();
        employeeSalary.setCode("ESL-1");
        employeeSalary.setCompanyCode("CMP-2");
        employeeSalary.setUserProfile(new UserProfile());
        when(employeeSalaryRepository.findByCodeAndIsDeletedFalse("ESL-1")).thenReturn(Optional.of(employeeSalary));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> employeeSalaryDetailService.createEmployeeSalaryDetails(requests));
        assertEquals(Messages.ERROR_EMPLOYEE_SALARY_DETAIL_EMPLOYEE_SALARY_COMPANY_MISMATCH, ex.getMessage());
        verify(employeeSalaryDetailRepository, never()).saveAll(anyList());
    }
}
