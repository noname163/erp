package com.dat.erp.services.impl;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.dat.erp.constants.Messages;
import com.dat.erp.entities.Account;
import com.dat.erp.entities.UserProfile;
import com.dat.erp.exceptions.ResourceNotFoundException;
import com.dat.erp.exceptions.UnauthorizedException;
import com.dat.erp.repositories.customrepositories.AccountRepository;
import com.dat.erp.services.SecurityContextService;
import com.dat.erp.systemconfigs.CustomUserDetails;

@Service
public class SecurityContextServiceImpl implements SecurityContextService {

    private final AccountRepository accountRepository;

    public SecurityContextServiceImpl(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public CustomUserDetails setCurrentUser(String accountCode) {
        Account account = accountRepository.findByCodeWithRoleAndUserProfileAndDepartment(accountCode)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(Messages.ERROR_ACCOUNT_NOT_FOUND_WITH_CODE, accountCode)));
        UserProfile profile = account.getUserProfile();
        CustomUserDetails customUserDetails = new CustomUserDetails(account, profile);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                customUserDetails, null, customUserDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return customUserDetails;
    }

    @Override
    public CustomUserDetails getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails principal) {
            return principal;
        }
        throw new UnauthorizedException("User is not authenticated");
    }
}
