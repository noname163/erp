package com.dat.erp.services;

import com.dat.erp.systemconfigs.CustomUserDetails;

public interface SecurityContextService {
    public CustomUserDetails setCurrentUser(String employeeCode);

    public CustomUserDetails getCurrentUser();
}
