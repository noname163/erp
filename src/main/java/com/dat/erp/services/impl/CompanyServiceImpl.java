package com.dat.erp.services.impl;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dat.erp.constants.CodePrefixes;
import com.dat.erp.constants.Messages;
import com.dat.erp.dto.request.CompanyRequest;
import com.dat.erp.dto.response.CompanyResponse;
import com.dat.erp.dto.response.PagedResponse;
import com.dat.erp.entities.Company;
import com.dat.erp.exceptions.ConflictException;
import com.dat.erp.mapper.interfaces.CompanyMapper;
import com.dat.erp.repositories.customrepositories.CompanyRepository;
import com.dat.erp.services.CompanyService;
import com.dat.erp.services.CodeGenerator;
import com.dat.erp.services.SecurityContextService;
import com.dat.erp.utils.PageableUtils;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class CompanyServiceImpl implements CompanyService {
    private static final Logger log = LoggerFactory.getLogger(CompanyServiceImpl.class);

    private final CompanyRepository companyRepository;
  
    private final CompanyMapper companyMapper;

    private final CompanyDefaultSetupService companyDefaultSetupService;

    private final SecurityContextService securityContextService;

    private final CodeGenerator codeGenerator;

    @Transactional
    @Override
    public CompanyResponse createCompany(CompanyRequest companyRequest) {
        Company company = companyMapper.toEntity(companyRequest);
        companyRepository.findByEmail(company.getEmail())
                .ifPresent(existing -> {
                    throw new ConflictException(Messages.ERROR_COMPANY_EMAIL_EXISTS);
                });
        companyRepository.findByTaxNumber(company.getTaxNumber())
                .ifPresent(existing -> {
                    throw new ConflictException(Messages.ERROR_COMPANY_TAX_NUMBER_EXISTS);
                });
        company.setSecretKey(UUID.randomUUID().toString());
        company.initializeCodeIfMissing(() -> codeGenerator.nextCode(CodePrefixes.COMPANY));
        companyRepository.save(company);

        String actorCode = securityContextService.getCurrentUserCode();
        log.info("AUDIT action=CREATE_COMPANY actor={} companyCode={} result=SUCCESS", actorCode, company.getCode());

        companyDefaultSetupService.setAccountDefault(company.getCode(), company.getEmail(), company.getName(), actorCode);
        companyDefaultSetupService.setDepartmentDefault(company.getCode(), actorCode);
        return companyMapper.toResponse(company);
    }

    @Override
    public PagedResponse<CompanyResponse> getCompanies(String searchKey, String searchValue, Integer page, Integer size,
            String sortBy, String sortDir) {
        Pageable pageable = PageableUtils.create(page, size, sortBy, sortDir);
        Page<Company> companies = companyRepository.findAll(pageable);
        return PageableUtils.mapPage(companies, companyMapper::toResponse, Messages.SUCCESS);
    }

}
