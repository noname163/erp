package com.dat.erp.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.dat.erp.constants.CodePrefixes;
import com.dat.erp.dto.request.CompanyRequest;
import com.dat.erp.dto.response.CompanyResponse;
import com.dat.erp.dto.response.PagedResponse;
import com.dat.erp.entities.Account;
import com.dat.erp.entities.Company;
import com.dat.erp.exceptions.ConflictException;
import com.dat.erp.mapper.interfaces.CompanyMapper;
import com.dat.erp.repositories.customrepositories.CompanyRepository;
import com.dat.erp.services.CodeGenerator;
import com.dat.erp.services.SecurityContextService;
import com.dat.erp.systemconfigs.CustomUserDetails;

class CompanyServiceImplTest {

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private CompanyMapper companyMapper;

    @Mock
    private CodeGenerator codeGenerator;

    @Mock
    private SecurityContextService securityContextService;

    @Mock
    private CompanyDefaultSetupService companyDefaultSetupService;

    @InjectMocks
    private CompanyServiceImpl companyService;

    private CompanyRequest request;
    private Company company;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        request = new CompanyRequest();
        request.setEmail("admin@openai.com");
        request.setName("OpenAI");
        request.setTaxNumber("123456789");

        company = new Company();
        company.setEmail("admin@openai.com");
        company.setName("OpenAI");
        company.setTaxNumber("123456789");
    }

    // -----------------------------
    // createCompany
    // -----------------------------
    @Test
    void testCreateCompany_Success() {
        when(companyMapper.toEntity(request)).thenReturn(company);
        when(companyRepository.findByEmail("admin@openai.com")).thenReturn(Optional.empty());
        when(companyRepository.findByTaxNumber("123456789")).thenReturn(Optional.empty());
        when(codeGenerator.nextCode(CodePrefixes.COMPANY)).thenReturn("CMP-000001");

        Account account = new Account();
        com.dat.erp.testutils.EntityTestData.setCode(account, "ACC-ADMIN");
        when(securityContextService.getCurrentUser()).thenReturn(new CustomUserDetails(account, null));
        when(securityContextService.getCurrentCompanyCode()).thenReturn(account.getCompanyCode());
        when(securityContextService.getCurrentUserCode()).thenReturn("ACC-ADMIN");

        CompanyResponse mapped = new CompanyResponse();
        when(companyMapper.toResponse(company)).thenReturn(mapped);

        CompanyResponse response = companyService.createCompany(request);

        assertSame(mapped, response);
        assertEquals("CMP-000001", company.getCode());
        verify(companyRepository).save(company);
        verify(companyDefaultSetupService).setAccountDefault(eq("CMP-000001"), eq("admin@openai.com"), eq("OpenAI"),
                eq("ACC-ADMIN"));
        verify(companyDefaultSetupService).setDepartmentDefault(eq("CMP-000001"), eq("ACC-ADMIN"));
    }

    @Test
    void testCreateCompany_EmailConflict() {
        when(companyMapper.toEntity(request)).thenReturn(company);
        when(companyRepository.findByEmail("admin@openai.com")).thenReturn(Optional.of(new Company()));

        assertThrows(ConflictException.class, () -> companyService.createCompany(request));

        verify(companyRepository, never()).save(any());
    }

    @Test
    void testCreateCompany_TaxNumberConflict() {
        when(companyMapper.toEntity(request)).thenReturn(company);
        when(companyRepository.findByEmail("admin@openai.com")).thenReturn(Optional.empty());
        when(companyRepository.findByTaxNumber("123456789")).thenReturn(Optional.of(new Company()));

        assertThrows(ConflictException.class, () -> companyService.createCompany(request));

        verify(companyRepository, never()).save(any());
    }

    // -----------------------------
    // getCompanies
    // -----------------------------
    @Test
    void testGetCompanies_Success() {
        Page<Company> page = new PageImpl<>(Collections.singletonList(company));
        when(companyRepository.findAll(any(Pageable.class))).thenReturn(page);

        CompanyResponse response = new CompanyResponse();
        when(companyMapper.toResponse(company)).thenReturn(response);

        PagedResponse<CompanyResponse> result = companyService.getCompanies("name", "OpenAI", 0, 10, "id", "asc");

        assertNotNull(result);
        assertEquals(1, result.getData().size());
        assertEquals(response, result.getData().get(0));
    }
}
