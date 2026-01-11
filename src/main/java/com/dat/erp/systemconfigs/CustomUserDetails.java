package com.dat.erp.systemconfigs;

import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.dat.erp.entities.Account;
import com.dat.erp.entities.Role;
import com.dat.erp.entities.UserProfile;

public class CustomUserDetails implements UserDetails {

    private final Account account;
    private final UserProfile userProfile;

    public CustomUserDetails(Account account, UserProfile userProfile) {
        this.account = account;
        this.userProfile = userProfile;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Role role = account.getRole();
        if (role == null) {
            return Collections.emptyList();
        }
        return Collections.singleton(new SimpleGrantedAuthority(role.getName()));
    }

    @Override
    public String getPassword() {
        return account.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return account.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return Boolean.TRUE.equals(account.getIsActive());
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return Boolean.TRUE.equals(account.getIsActive());
    }

    public Account getAccount() {
        return account;
    }

    public UserProfile getUserProfile() {
        return userProfile;
    }

    public String getCode() {
        return account.getCode();
    }
}
