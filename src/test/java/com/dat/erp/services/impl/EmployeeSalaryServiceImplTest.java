package com.dat.erp.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.dat.erp.constants.Messages;
import com.dat.erp.dto.request.EmployeeSalaryRequest;
import com.dat.erp.dto.response.EmployeeSalaryResponse;
import com.dat.erp.entities.Account;
import com.dat.erp.entities.Company;
import com.dat.erp.entities.EmployeeSalary;
import com.dat.erp.entities.UserProfile;
import com.dat.erp.exceptions.BadRequestException;
import com.dat.erp.exceptions.ConflictException;
import com.dat.erp.exceptions.ResourceNotFoundException;
import com.dat.erp.mapper.interfaces.EmployeeSalaryMapper;
import com.dat.erp.repositories.customrepositories.CompanyRepository;
import com.dat.erp.repositories.customrepositories.EmployeeSalaryRepository;
import com.dat.erp.repositories.customrepositories.UserProfileRepository;
import com.dat.erp.services.CodeGenerator;
import com.dat.erp.services.EmployeeSalaryDetailService;
import com.dat.erp.services.SecurityContextService;
import com.dat.erp.systemconfigs.CustomUserDetails;
import com.dat.erp.utils.CompanySecretKeyCryptoUtils;

class EmployeeSalaryServiceImplTest {

    @Mock
    private EmployeeSalaryRepository employeeSalaryRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private EmployeeSalaryMapper employeeSalaryMapper;

    @Mock
    private CodeGenerator codeGenerator;

    @Mock
    private SecurityContextService securityContextService;

    @Mock
    private EmployeeSalaryDetailService employeeSalaryDetailService;

    @InjectMocks
    private EmployeeSalaryServiceImpl employeeSalaryService;

    private EmployeeSalaryRequest request;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        request = new EmployeeSalaryRequest();
        request.setUserProfileCode("EMP001");
        request.setEffectiveFrom(LocalDate.of(2025, 1, 1));
        request.setEffectiveTo(LocalDate.of(2025, 12, 31));
        request.setTotalAmount("20000000");
        request.setCurrency("vnd");
    }

    @Test
    void createEmployeeSalary_success() {
        Account currentUserAccount = new Account();
        currentUserAccount.setCompanyCode("CMP-1");
        when(securityContextService.getCurrentUser()).thenReturn(new CustomUserDetails(currentUserAccount, null));

        Company company = new Company();
        company.setCode("CMP-1");
        company.setSecretKey("company-secret-key");
        when(companyRepository.findByCode("CMP-1")).thenReturn(Optional.of(company));

        Account employeeAccount = new Account();
        employeeAccount.setCompanyCode("CMP-1");
        UserProfile employee = new UserProfile();
        employee.setCode("EMP001");
        employee.setAccount(employeeAccount);
        employee.setIsActive(true);
        when(userProfileRepository.findByCode("EMP001")).thenReturn(Optional.of(employee));

        when(employeeSalaryRepository.existsOverlappingByUserProfileCodeAndCompanyCode(eq("EMP001"), eq("CMP-1"),
                any(LocalDate.class), any(LocalDate.class))).thenReturn(false);

        EmployeeSalary entity = new EmployeeSalary();
        when(employeeSalaryMapper.toEntity(request)).thenReturn(entity);
        when(codeGenerator.nextCode("ESL-")).thenReturn("ESL-000001");

        EmployeeSalary saved = new EmployeeSalary();
        saved.setCode("ESL-000001");
        saved.setUserProfile(employee);
        saved.setEffectiveFrom(request.getEffectiveFrom());
        saved.setEffectiveTo(request.getEffectiveTo());
        saved.setTotalAmount("20000000");
        saved.setCurrency("VND");
        when(employeeSalaryRepository.save(any(EmployeeSalary.class))).thenReturn(saved);

        EmployeeSalaryResponse response = new EmployeeSalaryResponse();
        response.setCode("ESL-000001");
        when(employeeSalaryMapper.toResponse(saved)).thenReturn(response);

        EmployeeSalaryResponse result = employeeSalaryService.createEmployeeSalary(request);

        assertNotNull(result);
        assertEquals("ESL-000001", result.getCode());
        ArgumentCaptor<EmployeeSalary> captor = ArgumentCaptor.forClass(EmployeeSalary.class);
        verify(employeeSalaryRepository).save(captor.capture());
        String persistedEncrypted = captor.getValue().getTotalAmount();
        assertEquals("20000000", CompanySecretKeyCryptoUtils.decrypt(persistedEncrypted, "company-secret-key"));
    }

    @Test
    void createEmployeeSalary_conflictWhenOverlappingPeriod() {
        Account currentUserAccount = new Account();
        currentUserAccount.setCompanyCode("CMP-1");
        when(securityContextService.getCurrentUser()).thenReturn(new CustomUserDetails(currentUserAccount, null));

        Company company = new Company();
        company.setCode("CMP-1");
        company.setSecretKey("company-secret-key");
        when(companyRepository.findByCode("CMP-1")).thenReturn(Optional.of(company));

        Account employeeAccount = new Account();
        employeeAccount.setCompanyCode("CMP-1");
        UserProfile employee = new UserProfile();
        employee.setCode("EMP001");
        employee.setAccount(employeeAccount);
        employee.setIsActive(true);
        when(userProfileRepository.findByCode("EMP001")).thenReturn(Optional.of(employee));

        when(employeeSalaryRepository.existsOverlappingByUserProfileCodeAndCompanyCode(eq("EMP001"), eq("CMP-1"),
                any(LocalDate.class), any(LocalDate.class))).thenReturn(true);

        ConflictException ex = assertThrows(ConflictException.class,
                () -> employeeSalaryService.createEmployeeSalary(request));
        assertEquals(Messages.ERROR_EMPLOYEE_SALARY_PERIOD_OVERLAPS, ex.getMessage());
        verify(employeeSalaryRepository, never()).save(any());
    }

    @Test
    void createEmployeeSalary_notFoundWhenEmployeeMissing() {
        Account currentUserAccount = new Account();
        currentUserAccount.setCompanyCode("CMP-1");
        when(securityContextService.getCurrentUser()).thenReturn(new CustomUserDetails(currentUserAccount, null));

        Company company = new Company();
        company.setCode("CMP-1");
        company.setSecretKey("company-secret-key");
        when(companyRepository.findByCode("CMP-1")).thenReturn(Optional.of(company));

        when(userProfileRepository.findByCode("EMP001")).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> employeeSalaryService.createEmployeeSalary(request));
        assertEquals(Messages.ERROR_EMPLOYEE_SALARY_EMPLOYEE_NOT_FOUND, ex.getMessage());
        verify(employeeSalaryRepository, never()).save(any());
    }

    @Test
    void createEmployeeSalary_badRequestWhenDateRangeInvalid() {
        request.setEffectiveFrom(LocalDate.of(2025, 12, 31));
        request.setEffectiveTo(LocalDate.of(2025, 1, 1));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> employeeSalaryService.createEmployeeSalary(request));
        assertEquals(Messages.ERROR_EMPLOYEE_SALARY_EFFECTIVE_DATES_INVALID, ex.getMessage());
        verify(employeeSalaryRepository, never()).save(any());
    }

    @Test
    void createEmployeeSalary_badRequestWhenEmployeeInactive() {
        Account currentUserAccount = new Account();
        currentUserAccount.setCompanyCode("CMP-1");
        when(securityContextService.getCurrentUser()).thenReturn(new CustomUserDetails(currentUserAccount, null));

        Company company = new Company();
        company.setCode("CMP-1");
        company.setSecretKey("company-secret-key");
        when(companyRepository.findByCode("CMP-1")).thenReturn(Optional.of(company));

        Account employeeAccount = new Account();
        employeeAccount.setCompanyCode("CMP-1");
        UserProfile employee = new UserProfile();
        employee.setCode("EMP001");
        employee.setAccount(employeeAccount);
        employee.setIsActive(false);
        when(userProfileRepository.findByCode("EMP001")).thenReturn(Optional.of(employee));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> employeeSalaryService.createEmployeeSalary(request));
        assertEquals(Messages.ERROR_EMPLOYEE_SALARY_EMPLOYEE_INACTIVE, ex.getMessage());
        verify(employeeSalaryRepository, never()).save(any());
    }
}
