package com.dat.erp.services;

import com.dat.erp.dto.request.UserProfileCreateRequest;
import com.dat.erp.dto.response.PagedResponse;
import com.dat.erp.dto.response.SelectionOptionResponse;
import com.dat.erp.entities.UserProfile;

public interface UserProfileService {
    UserProfile createUserProfile(UserProfileCreateRequest request);

    PagedResponse<SelectionOptionResponse> getUserProfileOptionsByFirstName(String firstName, Integer page, Integer size,
            String sortBy, String sortDir);
}
