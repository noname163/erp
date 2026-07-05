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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.dat.erp.constants.Messages;
import com.dat.erp.dto.request.EmployeeSalaryRequest;
import com.dat.erp.dto.response.EmployeeSalaryListResponse;
import com.dat.erp.dto.response.EmployeeSalaryResponse;
import com.dat.erp.dto.response.PagedResponse;
import com.dat.erp.dto.response.salary.MonthlySalaryCalculationResponse;
import com.dat.erp.entities.Account;
import com.dat.erp.entities.Company;
import com.dat.erp.entities.EmployeeSalary;
import com.dat.erp.entities.PayrollResult;
import com.dat.erp.entities.UserProfile;
import com.dat.erp.exceptions.BadRequestException;
import com.dat.erp.exceptions.ConflictException;
import com.dat.erp.exceptions.ResourceNotFoundException;
import com.dat.erp.mapper.interfaces.EmployeeSalaryMapper;
import com.dat.erp.repositories.customrepositories.CompanyRepository;
import com.dat.erp.repositories.customrepositories.EmployeeSalaryRepository;
import com.dat.erp.repositories.customrepositories.PayrollResultDetailRepository;
import com.dat.erp.repositories.customrepositories.PayrollResultRepository;
import com.dat.erp.repositories.customrepositories.UserProfileRepository;
import com.dat.erp.services.CodeGenerator;
import com.dat.erp.services.EmployeeSalaryDetailService;
import com.dat.erp.services.MonthlySalaryCalculationService;
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

    @Mock
    private MonthlySalaryCalculationService monthlySalaryCalculationService;

    @Mock
    private PayrollResultRepository payrollResultRepository;

    @Mock
    private PayrollResultDetailRepository payrollResultDetailRepository;

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

    @Test
    void getEmployeeSalaries_successWithFilters() {
        Account currentUserAccount = new Account();
        currentUserAccount.setCompanyCode("CMP-1");
        when(securityContextService.getCurrentUser()).thenReturn(new CustomUserDetails(currentUserAccount, null));

        Company company = new Company();
        company.setCode("CMP-1");
        company.setSecretKey("company-secret-key");
        when(companyRepository.findByCode("CMP-1")).thenReturn(Optional.of(company));

        UserProfile userProfile1 = new UserProfile();
        userProfile1.setFirstName("John");
        userProfile1.setLastName("Doe");

        EmployeeSalary salary1 = new EmployeeSalary();
        salary1.setCode("ESL-000001");
        salary1.setUserProfile(userProfile1);
        salary1.setEffectiveFrom(LocalDate.of(2025, 1, 1));
        salary1.setEffectiveTo(LocalDate.of(2025, 12, 31));
        salary1.setTotalAmount(CompanySecretKeyCryptoUtils.encrypt("1000", "company-secret-key"));
        salary1.setCurrency("USD");

        UserProfile userProfile2 = new UserProfile();
        userProfile2.setFirstName("Jane");
        userProfile2.setLastName("Smith");

        EmployeeSalary salary2 = new EmployeeSalary();
        salary2.setCode("ESL-000002");
        salary2.setUserProfile(userProfile2);
        salary2.setEffectiveFrom(LocalDate.of(2025, 2, 1));
        salary2.setEffectiveTo(LocalDate.of(2025, 12, 31));
        salary2.setTotalAmount(CompanySecretKeyCryptoUtils.encrypt("3000", "company-secret-key"));
        salary2.setCurrency("USD");

        when(employeeSalaryRepository.searchByConditions("CMP-1", "Jane",
                LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31)))
                .thenReturn(List.of(salary1, salary2));

        PagedResponse<EmployeeSalaryListResponse> result = employeeSalaryService.getEmployeeSalaries(
                "Jane",
                new BigDecimal("2000"),
                new BigDecimal("4000"),
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 12, 31),
                0,
                20,
                "salaryCode",
                "ASC");

        assertNotNull(result);
        assertEquals(1, result.getData().size());
        assertEquals("ESL-000002", result.getData().get(0).getSalaryCode());
        assertEquals("Jane Smith", result.getData().get(0).getEmployeeName());
        assertEquals("3000", result.getData().get(0).getTotalAmount());
    }

    @Test
    void getEmployeeSalaries_badRequestWhenAmountRangeInvalid() {
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> employeeSalaryService.getEmployeeSalaries(
                        null,
                        new BigDecimal("2000"),
                        new BigDecimal("1000"),
                        null,
                        null,
                        null,
                        null,
                        null,
                        null));
        assertEquals(Messages.ERROR_EMPLOYEE_SALARY_AMOUNT_RANGE_INVALID, ex.getMessage());
        assertFalse(ex.getMessage().isBlank());
    }

    @Test
    void employeeSalaryCalculation_updatesActualAmountsAndPersistsResults() {
        Account currentUserAccount = new Account();
        currentUserAccount.setCode("ACC-1");
        currentUserAccount.setCompanyCode("CMP-1");
        when(securityContextService.getCurrentUser()).thenReturn(new CustomUserDetails(currentUserAccount, null));

        Company company = new Company();
        company.setCode("CMP-1");
        company.setSecretKey("company-secret-key");
        when(companyRepository.findByCode("CMP-1")).thenReturn(Optional.of(company));

        PayrollResult firstPayrollResult = payrollResult(1L, "EMP001");
        PayrollResult secondPayrollResult = payrollResult(2L, "EMP002");

        when(monthlySalaryCalculationService.calculateEmployeeMonthlySalary("EMP001", YearMonth.of(2025, 3)))
                .thenReturn(monthlySalaryCalculationResponse("EMP001", "1234.5000", "160"));
        when(monthlySalaryCalculationService.calculateEmployeeMonthlySalary("EMP002", YearMonth.of(2025, 3)))
                .thenReturn(monthlySalaryCalculationResponse("EMP002", "2345.0000", "152"));

        employeeSalaryService.employeeSalaryCalculation(
                "CMP-1",
                List.of("EMP001", "EMP002"),
                List.of(firstPayrollResult, secondPayrollResult),
                LocalDate.of(2025, 3, 31));

        ArgumentCaptor<List<PayrollResult>> captor = ArgumentCaptor.forClass(List.class);
        verify(payrollResultRepository).saveAll(captor.capture());

        List<PayrollResult> savedResults = captor.getValue();
        assertEquals(2, savedResults.size());
        assertEquals("1234.5000",
                CompanySecretKeyCryptoUtils.decrypt(savedResults.get(0).getActualAmount(), "company-secret-key"));
        assertEquals(160, savedResults.get(0).getActualQuantity());
        assertEquals("ACC-1", savedResults.get(0).getUpdatedBy());
        assertEquals("2345.0000",
                CompanySecretKeyCryptoUtils.decrypt(savedResults.get(1).getActualAmount(), "company-secret-key"));
        assertEquals(152, savedResults.get(1).getActualQuantity());
    }

    @Test
    void employeeSalaryCalculation_badRequestWhenRunDateMissing() {
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> employeeSalaryService.employeeSalaryCalculation(
                        "CMP-1",
                        List.of("EMP001"),
                        List.of(new PayrollResult()),
                        null));

        assertEquals(Messages.ERROR_PAYROLL_MONTH_INVALID, ex.getMessage());
        verify(payrollResultRepository, never()).saveAll(any());
    }

    private PayrollResult payrollResult(Long userProfileId, String userProfileCode) {
        UserProfile userProfile = new UserProfile();
        userProfile.setId(userProfileId);
        userProfile.setCode(userProfileCode);

        EmployeeSalary employeeSalary = new EmployeeSalary();
        employeeSalary.setUserProfile(userProfile);

        PayrollResult payrollResult = new PayrollResult();
        payrollResult.setEmployeeSalary(employeeSalary);
        return payrollResult;
    }

    private MonthlySalaryCalculationResponse monthlySalaryCalculationResponse(
            String employeeCode,
            String finalSalary,
            String actualWorkingHours) {
        return new MonthlySalaryCalculationResponse(
                employeeCode,
                YearMonth.of(2025, 3),
                new BigDecimal("160"),
                new BigDecimal(actualWorkingHours),
                new BigDecimal("7.5000"),
                new BigDecimal(finalSalary),
                java.util.Map.of(),
                List.of());
    }
}
