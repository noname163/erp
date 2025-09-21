package com.dat.erp.systemconfigs;

import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.dat.erp.entities.EmployeeInformation;

public class CustomUserDetails extends EmployeeInformation implements UserDetails {

    private final transient EmployeeInformation employee;

    public CustomUserDetails(EmployeeInformation employee) {
        this.employee = employee;
    }

    @Override
    public String getCode() {
        return employee.getCode();
    }

    @Override
    public String getEmail() {
        return employee.getEmail();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(new SimpleGrantedAuthority(employee.getRole().getCode()));
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return employee.getNickname();
    }

    public EmployeeInformation getEmployee() {
        return employee;
    }

}
