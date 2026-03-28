package com.dat.erp.services.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.dat.erp.constants.SystemUnitType;
import com.dat.erp.constants.Messages;
import com.dat.erp.dto.response.PagedResponse;
import com.dat.erp.dto.response.SelectionOptionResponse;
import com.dat.erp.entities.SystemUnit;
import com.dat.erp.repositories.customrepositories.SystemUnitRepository;
import com.dat.erp.services.SecurityContextService;
import com.dat.erp.services.SystemUnitService;
import com.dat.erp.services.base.AbstractAuditableService;
import com.dat.erp.utils.PageableUtils;

@Service
public class SystemUnitServiceImpl extends AbstractAuditableService implements SystemUnitService {

    private final SystemUnitRepository systemUnitRepository;

    public SystemUnitServiceImpl(SystemUnitRepository systemUnitRepository, SecurityContextService securityContextService) {
        this.systemUnitRepository = systemUnitRepository;
        this.securityContextService = securityContextService;
    }

    @Override
    public PagedResponse<SelectionOptionResponse> getSystemUnitOptionsByCompanyCode(String name, SystemUnitType type,
            Integer page, Integer size, String sortBy, String sortDir) {
        String normalizedName = normalizeSearchText(name);
        Pageable pageable = PageableUtils.create(page, size, sortBy, sortDir);
        Page<SystemUnit> units = systemUnitRepository.findOptionsByFilters(normalizedName, type, pageable);

        return PageableUtils.mapPage(units, unit -> {
            SelectionOptionResponse option = new SelectionOptionResponse();
            option.setCode(unit.getCode());
            option.setName(unit.getName());
            return option;
        }, Messages.SUCCESS);
    }

    private String normalizeSearchText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
