package com.dat.erp.services;

import org.springframework.stereotype.Service;

import com.dat.erp.entities.EmployeeInformation;

@Service
public interface SecurityContextService {
    public void setCurrentUser(String employeeCode);

    public EmployeeInformation getCurrentUser();
}
