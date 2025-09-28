package com.dat.erp.services;

import org.springframework.stereotype.Service;

import com.dat.erp.dto.request.EmployeeInformationRequest;

@Service
public interface EmployeeInformationService {
    public String createEmployeeInformation(EmployeeInformationRequest request);
}
