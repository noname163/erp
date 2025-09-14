package com.dat.erp.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import com.dat.erp.entities.EmployeeInformation;
import com.dat.erp.repositories.customrepositories.EmployeeInformationRepository;
import com.dat.erp.services.SecurityContextService;
import com.dat.erp.systemconfigs.CustomUserDetails;

public class SecurityContextServiceImpl implements SecurityContextService {
    @Autowired
    private EmployeeInformationRepository employeeInformationRepository;

    @Override
    public void setCurrentUser(String employeeCode) {
        EmployeeInformation employeeInformation = employeeInformationRepository.findBasicByEmployeeCode(employeeCode)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        UserDetails userDetails = new CustomUserDetails(employeeInformation);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null,
                userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Override
    public EmployeeInformation getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();
        return ((EmployeeInformation) principal);
    }

}
