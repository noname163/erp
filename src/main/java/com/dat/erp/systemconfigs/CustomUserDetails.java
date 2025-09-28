package com.dat.erp.systemconfigs;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.dat.erp.entities.EmployeeInformation;

public class CustomUserDetails extends EmployeeInformation implements UserDetails {

    private final transient EmployeeInformation employee;

    private List<String> employeeCodes;

    private Map<String, Integer> permissionMap;

    private Boolean viewAll;

    private Boolean viewOwnedOnly;

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

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    public List<String> getEmployeeCodes() {
        return employeeCodes;
    }

    public void setEmployeeCodes(List<String> employeeCodes) {
        this.employeeCodes = employeeCodes;
    }

    public Map<String, Integer> getPermissionMap() {
        return permissionMap;
    }

    public void setPermissionMap(Map<String, Integer> permissionMap) {
        this.permissionMap = permissionMap;
    }

    public Boolean getViewAll() {
        return viewAll;
    }

    public void setViewAll(Boolean viewAll) {
        this.viewAll = viewAll;
    }

    public Boolean getViewOwnedOnly() {
        return viewOwnedOnly;
    }

    public void setViewOwnedOnly(Boolean viewOwnedOnly) {
        this.viewOwnedOnly = viewOwnedOnly;
    }

}
