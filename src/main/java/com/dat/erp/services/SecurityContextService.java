package com.dat.erp.services;

import com.dat.erp.systemconfigs.CustomUserDetails;

public interface SecurityContextService {
    CustomUserDetails setCurrentUser(String accountCode);

    CustomUserDetails getCurrentUser();

    String getCurrentUserCode();

    String getCurrentCompanyCode();
}
