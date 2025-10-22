package com.dat.erp.services.impl;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.dat.erp.constants.CodePrefixes;
import com.dat.erp.constants.Messages;
import com.dat.erp.dto.request.CompanyRequest;
import com.dat.erp.dto.response.CompanyResponse;
import com.dat.erp.dto.response.PagedResponse;
import com.dat.erp.entities.Company;
import com.dat.erp.mapper.interfaces.CompanyMapper;
import com.dat.erp.repositories.customrepositories.CompanyRepository;
import com.dat.erp.services.CompanyService;
import com.dat.erp.utils.PageableUtils;

@Service
public class CompanyServiceImpl implements CompanyService {

    @Autowired
    private CompanyRepository companyRepository;
    @Autowired
    private CompanyMapper companyMapper;

    @Override
    public String createCompany(CompanyRequest companyRequest) {
        Company company = companyMapper.toEntity(companyRequest);
        companyRepository.findByName(company.getName())
                .ifPresent(existing -> {
                    throw new com.dat.erp.exceptions.ConflictException(Messages.ERROR_COMPANY_NAME_EXISTS);
                });
        company.setCode(CodePrefixes.COMPANY + UUID.randomUUID());
        companyRepository.save(company);
        return company.getCode();
    }

    @Override
    public PagedResponse<CompanyResponse> getCompanies(String searchKey, String searchValue, Integer page, Integer size,
            String sortBy, String sortDir) {
        Pageable pageable = PageableUtils.create(page, size, sortBy, sortDir);
        Page<Company> companies = companyRepository.findAll(pageable);
        return PageableUtils.mapPage(companies, companyMapper::toResponse, Messages.SUCCESS);
    }

}
