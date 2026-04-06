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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.dat.erp.constants.Messages;
import com.dat.erp.dto.request.SalaryTemplateDetailRequest;
import com.dat.erp.dto.request.SalaryTemplateRequest;
import com.dat.erp.dto.response.PagedResponse;
import com.dat.erp.dto.response.SalaryTemplateDetailListResponse;
import com.dat.erp.dto.response.SalaryTemplateListResponse;
import com.dat.erp.dto.response.SalaryTemplateResponse;
import com.dat.erp.dto.response.SelectionOptionResponse;
import com.dat.erp.entities.Account;
import com.dat.erp.entities.SalaryTemplate;
import com.dat.erp.exceptions.BadRequestException;
import com.dat.erp.exceptions.ConflictException;
import com.dat.erp.mapper.interfaces.SalaryTemplateMapper;
import com.dat.erp.repositories.customrepositories.SalaryTemplateRepository;
import com.dat.erp.services.CodeGenerator;
import com.dat.erp.services.SalaryTemplateDetailService;
import com.dat.erp.services.SecurityContextService;
import com.dat.erp.systemconfigs.CustomUserDetails;

class SalaryTemplateServiceImplTest {

    @Mock
    private SalaryTemplateRepository salaryTemplateRepository;

    @Mock
    private SalaryTemplateMapper salaryTemplateMapper;

    @Mock
    private SalaryTemplateDetailService salaryTemplateDetailService;

    @Mock
    private CodeGenerator codeGenerator;

    @Mock
    private SecurityContextService securityContextService;

    @InjectMocks
    private SalaryTemplateServiceImpl salaryTemplateService;

    private SalaryTemplateRequest request;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        SalaryTemplateDetailRequest base = new SalaryTemplateDetailRequest();
        base.setSalaryCode("BASE");
        base.setAmount("100");
        base.setQuantity("1");
        base.setUnitCode("MONTH");
        base.setSequenceOrder("1");

        SalaryTemplateDetailRequest allowance = new SalaryTemplateDetailRequest();
        allowance.setSalaryCode("ALLOWANCE");
        allowance.setAmount("50");
        allowance.setQuantity("1");
        allowance.setUnitCode("MONTH");
        allowance.setSequenceOrder("2");

        request = new SalaryTemplateRequest();
        request.setName("Standard HR Package");
        request.setDescription("Base salary + allowance");
        request.setTotalAmount("150");
        request.setEffectiveFrom(LocalDate.of(2025, 1, 1));
        request.setEffectiveTo(LocalDate.of(2025, 12, 31));
        request.setCurrency("vnd");
        request.setDetails(List.of(base, allowance));
    }

    @Test
    void createSalaryTemplate_success() {
        Account account = new Account();
        account.setCompanyCode("CMP-1");
        when(securityContextService.getCurrentUser()).thenReturn(new CustomUserDetails(account, null));
        when(salaryTemplateRepository.existsOverlappingByNameAndCompanyCode(eq("Standard HR Package"), eq("CMP-1"),
                any(LocalDate.class), any(LocalDate.class))).thenReturn(false);

        SalaryTemplate entity = new SalaryTemplate();
        entity.setTotalAmount(new BigDecimal("150"));
        when(salaryTemplateMapper.toEntity(request)).thenReturn(entity);

        when(codeGenerator.nextCode("STP-")).thenReturn("STP-000001");
        SalaryTemplate saved = new SalaryTemplate();
        saved.setCode("STP-000001");
        saved.setName("Standard HR Package");
        saved.setTotalAmount(new BigDecimal("150"));
        saved.setEffectiveFrom(request.getEffectiveFrom());
        saved.setEffectiveTo(request.getEffectiveTo());
        saved.setCurrency("VND");

        when(salaryTemplateRepository.save(any(SalaryTemplate.class))).thenReturn(saved);

        SalaryTemplateResponse response = new SalaryTemplateResponse();
        response.setCode("STP-000001");
        when(salaryTemplateMapper.toResponse(saved)).thenReturn(response);

        SalaryTemplateResponse result = salaryTemplateService.createSalaryTemplate(request);

        assertNotNull(result);
        assertEquals("STP-000001", result.getCode());
        verify(salaryTemplateRepository).save(any(SalaryTemplate.class));
        verify(salaryTemplateDetailService).createSalaryTemplateDetails(eq(request.getDetails()), eq(saved));
    }

    @Test
    void createSalaryTemplate_conflictWhenOverlappingName() {
        Account account = new Account();
        account.setCompanyCode("CMP-1");
        when(securityContextService.getCurrentUser()).thenReturn(new CustomUserDetails(account, null));
        when(salaryTemplateRepository.existsOverlappingByNameAndCompanyCode(eq("Standard HR Package"), eq("CMP-1"),
                any(LocalDate.class), any(LocalDate.class))).thenReturn(true);

        ConflictException ex = assertThrows(ConflictException.class,
                () -> salaryTemplateService.createSalaryTemplate(request));
        assertEquals(Messages.ERROR_SALARY_TEMPLATE_NAME_EXISTS, ex.getMessage());
        verify(salaryTemplateRepository, never()).save(any());
        verify(salaryTemplateDetailService, never()).createSalaryTemplateDetails(any(), any());
    }

    @Test
    void createSalaryTemplate_badRequestWhenDateRangeInvalid() {
        request.setEffectiveFrom(LocalDate.of(2025, 12, 31));
        request.setEffectiveTo(LocalDate.of(2025, 1, 1));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> salaryTemplateService.createSalaryTemplate(request));
        assertEquals(Messages.ERROR_SALARY_TEMPLATE_EFFECTIVE_DATES_INVALID, ex.getMessage());
        verify(salaryTemplateRepository, never()).save(any());
    }

    @Test
    void createSalaryTemplate_badRequestWhenTotalAmountMismatch() {
        request.setTotalAmount("999");

        Account account = new Account();
        account.setCompanyCode("CMP-1");
        when(securityContextService.getCurrentUser()).thenReturn(new CustomUserDetails(account, null));
        when(salaryTemplateRepository.existsOverlappingByNameAndCompanyCode(any(), any(), any(), any()))
                .thenReturn(false);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> salaryTemplateService.createSalaryTemplate(request));
        assertEquals(Messages.ERROR_SALARY_TEMPLATE_TOTAL_AMOUNT_MISMATCH, ex.getMessage());
        verify(salaryTemplateRepository, never()).save(any());
    }

    @Test
    void getSalaryTemplates_success() {
        Account account = new Account();
        account.setCompanyCode("CMP-1");
        when(securityContextService.getCurrentUser()).thenReturn(new CustomUserDetails(account, null));

        SalaryTemplate template = new SalaryTemplate();
        template.setName("Standard HR Package");

        when(salaryTemplateRepository.searchByConditions(eq("CMP-1"), isNull(), isNull(), isNull(), isNull(), any()))
                .thenReturn(new PageImpl<>(List.of(template), PageRequest.of(0, 20), 1));

        SalaryTemplateListResponse item = new SalaryTemplateListResponse();
        item.setName("Standard HR Package");
        when(salaryTemplateMapper.toListResponse(template)).thenReturn(item);

        PagedResponse<SalaryTemplateListResponse> result = salaryTemplateService.getSalaryTemplates(null, null, null, null,
                0, 20, null, "DESC");

        assertNotNull(result);
        assertEquals(1, result.getData().size());
        assertEquals("Standard HR Package", result.getData().get(0).getName());
        assertEquals(Messages.SUCCESS, result.getMessage());
        verify(salaryTemplateRepository).searchByConditions(eq("CMP-1"), isNull(), isNull(), isNull(), isNull(), any());
        verify(salaryTemplateMapper).toListResponse(template);
    }

    @Test
    void getSalaryTemplates_badRequestWhenDateRangeInvalid() {
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> salaryTemplateService.getSalaryTemplates(null, null, LocalDate.of(2025, 12, 31),
                        LocalDate.of(2025, 1, 1), 0, 20, null, "DESC"));
        assertEquals(Messages.ERROR_SALARY_TEMPLATE_EFFECTIVE_DATES_INVALID, ex.getMessage());
        verify(salaryTemplateRepository, never()).searchByConditions(any(), any(), any(), any(), any(), any());
    }

    @Test
    void getSalaryTemplateOptions_success() {
        Account account = new Account();
        account.setCompanyCode("CMP-1");
        when(securityContextService.getCurrentUser()).thenReturn(new CustomUserDetails(account, null));

        SalaryTemplate template = new SalaryTemplate();
        template.setCode("STP-1");
        template.setName("Template A");

        when(salaryTemplateRepository.findOptionsByFilters(eq("CMP-1"), isNull(), any()))
                .thenReturn(new PageImpl<>(List.of(template), PageRequest.of(0, 20), 1));
        SelectionOptionResponse option = new SelectionOptionResponse();
        option.setCode("STP-1");
        option.setName("Template A");
        when(salaryTemplateMapper.toOptionResponse(template)).thenReturn(option);

        PagedResponse<SelectionOptionResponse> result = salaryTemplateService.getSalaryTemplateOptions(null, 0, 20, null,
                "ASC");

        assertNotNull(result);
        assertEquals(1, result.getData().size());
        assertEquals("STP-1", result.getData().get(0).getCode());
        assertEquals("Template A", result.getData().get(0).getName());
        assertEquals(Messages.SUCCESS, result.getMessage());
        verify(salaryTemplateRepository).findOptionsByFilters(eq("CMP-1"), isNull(), any());
    }

    @Test
    void getSalaryTemplateDetails_success() {
        SalaryTemplateDetailListResponse detail = new SalaryTemplateDetailListResponse("SAL-1", "100", 1, "Month", "Base",
                Boolean.FALSE);
        when(salaryTemplateDetailService.getSalaryTemplateDetails("STP-1")).thenReturn(List.of(detail));

        List<SalaryTemplateDetailListResponse> result = salaryTemplateService.getSalaryTemplateDetails("STP-1");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("SAL-1", result.get(0).getSalaryCode());
        verify(salaryTemplateDetailService).getSalaryTemplateDetails("STP-1");
    }
}
