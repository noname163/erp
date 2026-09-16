package com.dat.erp.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.dat.erp.constants.Messages;
import com.dat.erp.dto.request.SalaryTemplateDetailRequest;
import com.dat.erp.dto.response.SalaryTemplateDetailListResponse;
import com.dat.erp.entities.Account;
import com.dat.erp.entities.Salary;
import com.dat.erp.entities.SalaryTemplate;
import com.dat.erp.entities.SalaryTemplateDetail;
import com.dat.erp.entities.SystemUnit;
import com.dat.erp.exceptions.BadRequestException;
import com.dat.erp.exceptions.ResourceNotFoundException;
import com.dat.erp.mapper.interfaces.SalaryTemplateDetailMapper;
import com.dat.erp.repositories.customrepositories.SalaryRepository;
import com.dat.erp.repositories.customrepositories.SalaryTemplateDetailRepository;
import com.dat.erp.repositories.customrepositories.SalaryTemplateRepository;
import com.dat.erp.repositories.customrepositories.SystemUnitRepository;
import com.dat.erp.services.CodeGenerator;
import com.dat.erp.services.SecurityContextService;
import com.dat.erp.systemconfigs.CustomUserDetails;

class SalaryTemplateDetailServiceImplTest {

    @Mock
    private SalaryTemplateDetailRepository salaryTemplateDetailRepository;

    @Mock
    private SalaryRepository salaryRepository;

    @Mock
    private SystemUnitRepository systemUnitRepository;

    @Mock
    private SalaryTemplateRepository salaryTemplateRepository;

    @Mock
    private SalaryTemplateDetailMapper salaryTemplateDetailMapper;

    @Mock
    private CodeGenerator codeGenerator;

    @Mock
    private SecurityContextService securityContextService;

    @InjectMocks
    private SalaryTemplateDetailServiceImpl salaryTemplateDetailService;

    private List<SalaryTemplateDetailRequest> requests;
    private SalaryTemplate salaryTemplate;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        SalaryTemplateDetailRequest base = new SalaryTemplateDetailRequest();
        base.setSalaryCode("BASE");
        base.setAmount("100");
        base.setQuantity("1");
        base.setUnitCode("MONTH");
        base.setSequenceOrder("1");
        base.setIsFixed(Boolean.TRUE);

        SalaryTemplateDetailRequest allowance = new SalaryTemplateDetailRequest();
        allowance.setSalaryCode("ALLOWANCE");
        allowance.setAmount("50");
        allowance.setDependenceCode("BASE");
        allowance.setQuantity("1");
        allowance.setUnitCode("MONTH");
        allowance.setSequenceOrder("2");

        requests = Arrays.asList(base, allowance);

        salaryTemplate = new SalaryTemplate();
        com.dat.erp.testutils.EntityTestData.setCode(salaryTemplate, "STP-1");
    }

    @Test
    void createSalaryTemplateDetails_success() {
        Salary baseSalary = new Salary();
        com.dat.erp.testutils.EntityTestData.setCode(baseSalary, "BASE");
        Salary allowanceSalary = new Salary();
        com.dat.erp.testutils.EntityTestData.setCode(allowanceSalary, "ALLOWANCE");
        when(salaryRepository.findAllByCodeIn(anyCollection())).thenReturn(List.of(baseSalary, allowanceSalary));

        SystemUnit month = new SystemUnit();
        com.dat.erp.testutils.EntityTestData.setCode(month, "MONTH");
        when(systemUnitRepository.findAllByCodeIn(anyCollection())).thenReturn(List.of(month));

        when(codeGenerator.nextCode("STD-")).thenReturn("STD-000001", "STD-000002");
        when(salaryTemplateDetailRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        List<SalaryTemplateDetail> result = salaryTemplateDetailService.createSalaryTemplateDetails(requests, salaryTemplate);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("BASE", result.get(0).getSalary().getCode());
        assertEquals("MONTH", result.get(0).getUnit().getCode());
        assertEquals(1, result.get(0).getQuantity());
        assertEquals(2, result.get(1).getSequenceOrder());
        assertEquals("BASE", result.get(1).getDependenceCode().getCode());
        assertEquals(Boolean.TRUE, result.get(0).getIsFixed());
        verify(salaryRepository).findAllByCodeIn(anyCollection());
        verify(systemUnitRepository).findAllByCodeIn(anyCollection());
    }

    @Test
    void createSalaryTemplateDetails_badRequestWhenSalaryCodeInvalid() {
        Salary allowanceSalary = new Salary();
        com.dat.erp.testutils.EntityTestData.setCode(allowanceSalary, "ALLOWANCE");
        when(salaryRepository.findAllByCodeIn(anyCollection())).thenReturn(List.of(allowanceSalary));

        SystemUnit month = new SystemUnit();
        com.dat.erp.testutils.EntityTestData.setCode(month, "MONTH");
        when(systemUnitRepository.findAllByCodeIn(anyCollection())).thenReturn(List.of(month));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> salaryTemplateDetailService.createSalaryTemplateDetails(requests, salaryTemplate));

        assertEquals(Messages.ERROR_SALARY_TEMPLATE_DETAIL_SALARY_CODE_INVALID, ex.getMessage());
        verify(salaryTemplateDetailRepository, never()).saveAll(any());
    }

    @Test
    void createSalaryTemplateDetails_badRequestWhenUnitCodeInvalid() {
        Salary baseSalary = new Salary();
        com.dat.erp.testutils.EntityTestData.setCode(baseSalary, "BASE");
        Salary allowanceSalary = new Salary();
        com.dat.erp.testutils.EntityTestData.setCode(allowanceSalary, "ALLOWANCE");
        when(salaryRepository.findAllByCodeIn(anyCollection())).thenReturn(List.of(baseSalary, allowanceSalary));
        when(systemUnitRepository.findAllByCodeIn(anyCollection())).thenReturn(List.of());

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> salaryTemplateDetailService.createSalaryTemplateDetails(requests, salaryTemplate));

        assertEquals(Messages.ERROR_SALARY_TEMPLATE_DETAIL_UNIT_CODE_INVALID, ex.getMessage());
        verify(salaryTemplateDetailRepository, never()).saveAll(any());
    }

    @Test
    void createSalaryTemplateDetails_badRequestWhenDependenceCodeMissingFromRequest() {
        requests.get(1).setDependenceCode("UNKNOWN");

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> salaryTemplateDetailService.createSalaryTemplateDetails(requests, salaryTemplate));

        assertEquals(Messages.ERROR_EMPLOYEE_SALARY_DETAIL_DEPENDENCE_CODE_MUST_EXIST_IN_REQUEST, ex.getMessage());
        verify(salaryTemplateDetailRepository, never()).saveAll(any());
    }

    @Test
    void getSalaryTemplateDetails_success() {
        Account account = new Account();
        com.dat.erp.testutils.EntityTestData.setCompanyCode(account, "CMP-1");
        when(securityContextService.getCurrentUser()).thenReturn(new CustomUserDetails(account, null));
        when(securityContextService.getCurrentCompanyCode()).thenReturn(account.getCompanyCode());

        SalaryTemplate template = new SalaryTemplate();
        com.dat.erp.testutils.EntityTestData.setCode(template, "STP-1");
        when(salaryTemplateRepository.findByCodeAndCompanyCodeAndIsDeletedFalse("STP-1", "CMP-1"))
                .thenReturn(Optional.of(template));

        Salary salary = new Salary();
        com.dat.erp.testutils.EntityTestData.setCode(salary, "SAL-1");
        salary.setName("Base Salary");
        SystemUnit unit = new SystemUnit();
        unit.setName("Month");

        SalaryTemplateDetail detail = new SalaryTemplateDetail();
        detail.setSalary(salary);
        detail.setAmount("1000");
        detail.setQuantity(1);
        detail.setUnit(unit);

        when(salaryTemplateDetailRepository.findBySalaryTemplateCodeAndCompanyCode("STP-1", "CMP-1"))
                .thenReturn(List.of(detail));
        SalaryTemplateDetailListResponse mappedResponse = new SalaryTemplateDetailListResponse(
                "SAL-1",
                null,
                "1000",
                1,
                "Month",
                "Base Salary",
                null,
                null,
                Boolean.FALSE);
        when(salaryTemplateDetailMapper.toListResponses(List.of(detail))).thenReturn(List.of(mappedResponse));

        List<SalaryTemplateDetailListResponse> result = salaryTemplateDetailService.getSalaryTemplateDetails("STP-1");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("SAL-1", result.get(0).getSalaryCode());
        assertEquals("1000", result.get(0).getAmount());
        assertEquals(1, result.get(0).getQuantity());
        assertEquals("Month", result.get(0).getUnitName());
        assertEquals("Base Salary", result.get(0).getSalaryName());
        assertEquals(Boolean.FALSE, result.get(0).getIsFixed());
    }

    @Test
    void getSalaryTemplateDetails_notFound() {
        Account account = new Account();
        com.dat.erp.testutils.EntityTestData.setCompanyCode(account, "CMP-1");
        when(securityContextService.getCurrentUser()).thenReturn(new CustomUserDetails(account, null));
        when(securityContextService.getCurrentCompanyCode()).thenReturn(account.getCompanyCode());
        when(salaryTemplateRepository.findByCodeAndCompanyCodeAndIsDeletedFalse("STP-404", "CMP-1"))
                .thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> salaryTemplateDetailService.getSalaryTemplateDetails("STP-404"));

        assertEquals(String.format(Messages.ERROR_SALARY_TEMPLATE_NOT_FOUND_WITH_CODE, "STP-404"), ex.getMessage());
        verify(salaryTemplateDetailRepository, never()).findBySalaryTemplateCodeAndCompanyCode(any(), any());
    }

    @Test
    void getSalaryTemplateDetails_badRequestWhenCodeBlank() {
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> salaryTemplateDetailService.getSalaryTemplateDetails(" "));

        assertEquals("salaryTemplateCode is invalid", ex.getMessage());
        verify(salaryTemplateRepository, never()).findByCodeAndCompanyCodeAndIsDeletedFalse(any(), any());
    }
}
