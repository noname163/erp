package com.dat.erp.services;

import com.dat.erp.dto.request.UserProfileCreateRequest;
import com.dat.erp.entities.UserProfile;

public interface UserProfileService {
    UserProfile createUserProfile(UserProfileCreateRequest request);
}
