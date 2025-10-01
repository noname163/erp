package com.dat.erp.services.impl;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.dat.erp.dto.request.UserInformationRequest;
import com.dat.erp.dto.response.PagedResponse;
import com.dat.erp.dto.response.UserInformationResponse;
import com.dat.erp.entities.UserInformation;
import com.dat.erp.mapper.interfaces.UserMapper;
import com.dat.erp.repositories.customrepositories.UserInformationRepository;
import com.dat.erp.services.UserInformationService;
import com.dat.erp.utils.PageableUtils;

@Service
public class UserInformationServiceImpl implements UserInformationService {

    @Autowired
    private UserInformationRepository userInformationRepository;
    @Autowired
    private UserMapper userMapper;

    @Override
    public String createUserInformation(UserInformationRequest request) {
        UserInformation userInformation = userMapper.toEntity(request);
        userInformation.setCode("USR-" + UUID.randomUUID());
        userInformationRepository.save(userInformation);
        return userInformation.getCode();
    }

    @Override
    public PagedResponse<UserInformationResponse> getListUserInformation(String searchkey, String searchValue,
            Integer pageSize, Integer pageNum, String sortBy, String sortDir) {
        Pageable pageable = PageableUtils.create(pageNum, pageSize, sortBy, sortDir);

        Page<UserInformation> data = userInformationRepository.findAll(pageable);

        return PageableUtils.mapPage(data, userMapper::toResponse, "Success");
    }

}
