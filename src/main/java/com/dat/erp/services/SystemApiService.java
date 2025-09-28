package com.dat.erp.services;

import com.dat.erp.dto.response.PagedResponse;
import com.dat.erp.dto.response.SystemApiResponse;

public interface SystemApiService {
    PagedResponse<SystemApiResponse> getListSystemApi(String searchValue, Integer page, Integer size, String sortBy,
            String sortDir);
}
