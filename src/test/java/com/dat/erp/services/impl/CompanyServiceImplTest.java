package com.dat.erp.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.dat.erp.dto.request.CompanyRequest;
import com.dat.erp.dto.response.CompanyResponse;
import com.dat.erp.dto.response.PagedResponse;
import com.dat.erp.entities.Company;
import com.dat.erp.mapper.interfaces.CompanyMapper;
import com.dat.erp.repositories.customrepositories.CompanyRepository;

class CompanyServiceImplTest {

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private CompanyMapper companyMapper;

    @InjectMocks
    private CompanyServiceImpl companyService;

    private CompanyRequest request;
    private Company company;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        request = new CompanyRequest();
        request.setName("OpenAI");

        company = new Company();
        company.setName("OpenAI");
    }

    // -----------------------------
    // createCompany
    // -----------------------------
    @Test
    void testCreateCompany_Success() {
        when(companyMapper.toEntity(request)).thenReturn(company);

        String code = companyService.createCompany(request);

        assertNotNull(code);
        assertTrue(code.startsWith("CMP-"));
        verify(companyRepository).save(company);
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
        assertSame(response, result.getData().get(0));
    }
}
