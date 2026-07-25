package com.dat.erp.services.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.dat.erp.constants.Messages;
import com.dat.erp.constants.SystemUnitType;
import com.dat.erp.dto.response.PagedResponse;
import com.dat.erp.dto.response.SelectionOptionResponse;
import com.dat.erp.entities.SystemUnit;
import com.dat.erp.mapper.interfaces.SystemUnitMapper;
import com.dat.erp.repositories.customrepositories.SystemUnitRepository;
import com.dat.erp.services.CodeGenerator;
import com.dat.erp.services.SecurityContextService;
import com.dat.erp.services.SystemUnitService;
import com.dat.erp.utils.CustomStringUtils;
import com.dat.erp.utils.PageableUtils;

@Service
public class SystemUnitServiceImpl implements SystemUnitService {


    private final SystemUnitRepository systemUnitRepository;
    private final SystemUnitMapper systemUnitMapper;

    public SystemUnitServiceImpl(
            SystemUnitRepository systemUnitRepository,
            SystemUnitMapper systemUnitMapper,
            CodeGenerator codeGenerator,
            SecurityContextService securityContextService) {
        this.systemUnitRepository = systemUnitRepository;
        this.systemUnitMapper = systemUnitMapper;
    }

    @Override
    public PagedResponse<SelectionOptionResponse> getSystemUnitOptionsByCompanyCode(String name, SystemUnitType type,
            Integer page, Integer size, String sortBy, String sortDir) {
        String normalizedName = CustomStringUtils.trimToNull(name);
        Pageable pageable = PageableUtils.create(page, size, sortBy, sortDir);
        Page<SystemUnit> units = systemUnitRepository.findOptionsByFilters(normalizedName, type, pageable);

        return PageableUtils.mapPage(units, systemUnitMapper::toOptionResponse, Messages.SUCCESS);
    }
}
