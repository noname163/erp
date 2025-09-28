package com.dat.erp.services.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.dat.erp.entities.EmployeeInformation;
import com.dat.erp.repositories.customrepositories.EmployeeInformationRepository;
import com.dat.erp.services.RoleHasApiService;
import com.dat.erp.services.SecurityContextService;
import com.dat.erp.systemconfigs.CustomUserDetails;

@Service
public class SecurityContextServiceImpl implements SecurityContextService {
    @Autowired
    private EmployeeInformationRepository employeeInformationRepository;
    @Autowired
    private RoleHasApiService roleHasApiService;

    @Override
    public CustomUserDetails setCurrentUser(String employeeCode) {
        EmployeeInformation employeeInformation = employeeInformationRepository.findByCode(employeeCode)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        List<String> employeeCodes = employeeInformationRepository.findAllCodesByManagerCode(employeeCode);
        employeeCodes.add(employeeCode);
        CustomUserDetails customUserDetails = new CustomUserDetails(employeeInformation);
        customUserDetails.setEmployeeCodes(employeeCodes);
        Map<String, Integer> permission = roleHasApiService.getUserPermissionByUserCode(employeeCode);
        customUserDetails.setPermissionMap(permission);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                customUserDetails, null,
                customUserDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return customUserDetails;
    }

    @Override
    public CustomUserDetails getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            Object principal = authentication.getPrincipal();
            return ((CustomUserDetails) principal);
        }
        return null;
    }

}
