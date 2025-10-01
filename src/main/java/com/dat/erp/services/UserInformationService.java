package com.dat.erp.services;

import com.dat.erp.dto.request.UserInformationRequest;
import com.dat.erp.dto.response.PagedResponse;
import com.dat.erp.dto.response.UserInformationResponse;

public interface UserInformationService {
    public String createUserInformation(UserInformationRequest request);

    public PagedResponse<UserInformationResponse> getListUserInformation(String searchkey, String searchValue,
            Integer pageSize, Integer pageNum, String sortBy, String sortDir);
}
