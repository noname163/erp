package com.dat.erp.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.dat.erp.constants.Messages;
import com.dat.erp.constants.SalaryCalculateMethod;
import com.dat.erp.dto.request.SalaryRequest;
import com.dat.erp.dto.response.PagedResponse;
import com.dat.erp.dto.response.SalaryResponse;
import com.dat.erp.dto.response.SelectionOptionResponse;
import com.dat.erp.entities.Account;
import com.dat.erp.entities.Salary;
import com.dat.erp.exceptions.BadRequestException;
import com.dat.erp.exceptions.ConflictException;
import com.dat.erp.mapper.interfaces.SalaryMapper;
import com.dat.erp.repositories.customrepositories.SalaryRepository;
import com.dat.erp.services.CodeGenerator;
import com.dat.erp.services.SecurityContextService;
import com.dat.erp.systemconfigs.CustomUserDetails;

class SalaryServiceImplTest {

    @Mock
    private SalaryRepository salaryRepository;

    @Mock
    private SalaryMapper salaryMapper;

    @Mock
    private CodeGenerator codeGenerator;

    @Mock
    private SecurityContextService securityContextService;

    @InjectMocks
    private SalaryServiceImpl salaryService;

    private SalaryRequest base;
    private SalaryRequest deduct;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        base = new SalaryRequest();
        base.setName("BASE");
        base.setCalculateMethod("PLUS");
        base.setIsDeduct(false);

        deduct = new SalaryRequest();
        deduct.setName("LATE_DEDUCTION");
        deduct.setCalculateMethod("MINUS");
        deduct.setIsDeduct(true);
    }

    @Test
    void createSalaries_success() {
        Account account = new Account();
        account.setCompanyCode("CMP-1");
        when(securityContextService.getCurrentUser()).thenReturn(new CustomUserDetails(account, null));

        when(salaryRepository.findExistingUpperCaseNamesByCompanyCodeAndIsDeletedFalse("CMP-1",
                List.of("BASE", "LATE_DEDUCTION"))).thenReturn(List.of());

        Salary baseEntity = new Salary();
        Salary deductEntity = new Salary();
        when(salaryMapper.toEntities(List.of(base, deduct))).thenReturn(List.of(baseEntity, deductEntity));

        when(codeGenerator.nextCode("SAL-")).thenReturn("SAL-000001", "SAL-000002");

        Salary baseSaved = new Salary();
        baseSaved.setCode("SAL-000001");
        baseSaved.setName("BASE");
        baseSaved.setCalculateMethod(SalaryCalculateMethod.PLUS);
        baseSaved.setIsDeduct(false);
        Salary deductSaved = new Salary();
        deductSaved.setCode("SAL-000002");
        deductSaved.setName("LATE_DEDUCTION");
        deductSaved.setCalculateMethod(SalaryCalculateMethod.MINUS);
        deductSaved.setIsDeduct(true);
        when(salaryRepository.saveAll(any())).thenReturn(List.of(baseSaved, deductSaved));

        SalaryResponse baseResp = new SalaryResponse("SAL-000001", "BASE", "PLUS", false);
        SalaryResponse deductResp = new SalaryResponse("SAL-000002", "LATE_DEDUCTION", "MINUS", true);
        when(salaryMapper.toResponses(List.of(baseSaved, deductSaved))).thenReturn(List.of(baseResp, deductResp));

        List<SalaryResponse> result = salaryService.createSalaries(List.of(base, deduct));

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("SAL-000001", result.get(0).getCode());
        verify(salaryRepository).saveAll(any());
    }

    @Test
    void createSalaries_conflictWhenNameExistsInDb() {
        Account account = new Account();
        account.setCompanyCode("CMP-1");
        when(securityContextService.getCurrentUser()).thenReturn(new CustomUserDetails(account, null));

        when(salaryRepository.findExistingUpperCaseNamesByCompanyCodeAndIsDeletedFalse("CMP-1",
                List.of("BASE"))).thenReturn(List.of("BASE"));

        ConflictException ex = assertThrows(ConflictException.class,
                () -> salaryService.createSalaries(List.of(base)));
        assertEquals(Messages.ERROR_SALARY_NAME_EXISTS, ex.getMessage());
        verify(salaryRepository, never()).saveAll(any());
    }

    @Test
    void createSalaries_badRequestWhenDuplicateNamesInRequest() {
        SalaryRequest dup = new SalaryRequest();
        dup.setName("base");
        dup.setCalculateMethod("PLUS");
        dup.setIsDeduct(false);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> salaryService.createSalaries(List.of(base, dup)));
        assertEquals(Messages.ERROR_SALARY_NAME_INVALID, ex.getMessage());
        verify(salaryRepository, never()).saveAll(any());
    }

    @Test
    void createSalaries_badRequestWhenCalculateMethodInvalid() {
        base.setCalculateMethod("INVALID");

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> salaryService.createSalaries(List.of(base)));
        assertEquals(Messages.ERROR_SALARY_CALCULATE_METHOD_INVALID, ex.getMessage());
        verify(salaryRepository, never()).saveAll(any());
    }

    @Test
    void getSalaryOptionsByCompanyCode_success() {
        Account account = new Account();
        account.setCompanyCode("CMP-1");
        when(securityContextService.getCurrentUser()).thenReturn(new CustomUserDetails(account, null));

        Salary salary = new Salary();
        salary.setCode("SAL-000001");
        salary.setName("BASE");

        when(salaryRepository.findOptionsByFilters(eq("CMP-1"), isNull(), any()))
                .thenReturn(new PageImpl<>(List.of(salary), PageRequest.of(0, 20), 1));
        SelectionOptionResponse option = new SelectionOptionResponse();
        option.setCode("SAL-000001");
        option.setName("BASE");
        when(salaryMapper.toOptionResponse(salary)).thenReturn(option);

        PagedResponse<SelectionOptionResponse> result = salaryService.getSalaryOptionsByCompanyCode(null, 0, 20, null,
                "ASC");

        assertNotNull(result);
        assertEquals(1, result.getData().size());
        assertEquals("SAL-000001", result.getData().get(0).getCode());
        assertEquals("BASE", result.getData().get(0).getName());
        assertEquals(Messages.SUCCESS, result.getMessage());
        verify(salaryRepository).findOptionsByFilters(eq("CMP-1"), isNull(), any());
    }
}
