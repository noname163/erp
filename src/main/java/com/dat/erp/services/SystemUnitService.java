package com.dat.erp.services;

import com.dat.erp.constants.SystemUnitType;
import com.dat.erp.dto.response.PagedResponse;
import com.dat.erp.dto.response.SelectionOptionResponse;

public interface SystemUnitService {
    PagedResponse<SelectionOptionResponse> getSystemUnitOptionsByCompanyCode(String name, SystemUnitType type,
            Integer page, Integer size, String sortBy, String sortDir);
}
