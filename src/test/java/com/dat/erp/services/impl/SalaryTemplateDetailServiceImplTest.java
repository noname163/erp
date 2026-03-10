package com.dat.erp.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.dat.erp.constants.Messages;
import com.dat.erp.dto.response.SalaryTemplateDetailListResponse;
import com.dat.erp.entities.Account;
import com.dat.erp.entities.Salary;
import com.dat.erp.entities.SalaryTemplate;
import com.dat.erp.entities.SalaryTemplateDetail;
import com.dat.erp.entities.SystemUnit;
import com.dat.erp.exceptions.BadRequestException;
import com.dat.erp.exceptions.ResourceNotFoundException;
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
    private CodeGenerator codeGenerator;

    @Mock
    private SecurityContextService securityContextService;

    @InjectMocks
    private SalaryTemplateDetailServiceImpl salaryTemplateDetailService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getSalaryTemplateDetails_success() {
        Account account = new Account();
        account.setCompanyCode("CMP-1");
        when(securityContextService.getCurrentUser()).thenReturn(new CustomUserDetails(account, null));

        SalaryTemplate template = new SalaryTemplate();
        template.setCode("STP-1");
        when(salaryTemplateRepository.findByCodeAndCompanyCodeAndIsDeletedFalse("STP-1", "CMP-1"))
                .thenReturn(Optional.of(template));

        Salary salary = new Salary();
        salary.setCode("SAL-1");
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

        List<SalaryTemplateDetailListResponse> result = salaryTemplateDetailService.getSalaryTemplateDetails("STP-1");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("SAL-1", result.get(0).getSalaryCode());
        assertEquals("1000", result.get(0).getAmount());
        assertEquals(1, result.get(0).getQuantity());
        assertEquals("Month", result.get(0).getUnitName());
        assertEquals("Base Salary", result.get(0).getSalaryName());
    }

    @Test
    void getSalaryTemplateDetails_notFound() {
        Account account = new Account();
        account.setCompanyCode("CMP-1");
        when(securityContextService.getCurrentUser()).thenReturn(new CustomUserDetails(account, null));
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
