package com.dat.erp.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.dat.erp.constants.Messages;
import com.dat.erp.dto.response.PagedResponse;
import com.dat.erp.dto.response.SystemApiResponse;
import com.dat.erp.entities.SystemApi;
import com.dat.erp.mapper.interfaces.SystemApiMapper;
import com.dat.erp.repositories.customrepositories.SystemApiRepository;
import com.dat.erp.services.SystemApiService;
import com.dat.erp.utils.PageableUtils;

@Service
public class SystemApiServiceImpl implements SystemApiService {
    @Autowired
    private SystemApiRepository systemApiRepository;
    @Autowired
    private SystemApiMapper systemApiMapper;

    @Override
    public PagedResponse<SystemApiResponse> getListSystemApi(String searchValue, Integer page, Integer size,
            String sortBy, String sortDir) {
        Pageable pageable = PageableUtils.create(page, size, sortBy, sortDir);
        Page<SystemApi> data = systemApiRepository.findAll(pageable);
        return PageableUtils.mapPage(data, systemApiMapper::toResponse, Messages.SUCCESS);
    }

}
