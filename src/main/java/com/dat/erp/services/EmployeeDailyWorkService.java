package com.dat.erp.services;

import java.util.List;

import com.dat.erp.dto.request.EmployeeDailyWorkRequest;

public interface EmployeeDailyWorkService {
    String createEmployeeDailyWorks(List<EmployeeDailyWorkRequest> requests);
}

