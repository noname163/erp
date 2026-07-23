package com.dat.erp.services;

import com.dat.erp.dto.request.AccountRequest;

public interface AccountService {
    String createDefaultAccount(AccountRequest request, String actorCode);
}

