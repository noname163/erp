package com.dat.erp.services.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.dat.erp.constants.RoleType;
import com.dat.erp.dto.request.AccountRequest;
import com.dat.erp.dto.request.DepartmentRequest;
import com.dat.erp.services.AccountService;
import com.dat.erp.services.DepartmentService;
import com.dat.erp.services.PasswordGenerator;

@Service
public class CompanyDefaultSetupService {
    private static final Logger log = LoggerFactory.getLogger(CompanyDefaultSetupService.class);

    private final AccountService accountService;
    private final DepartmentService departmentService;
    private final PasswordGenerator passwordGenerator;

    public CompanyDefaultSetupService(
            AccountService accountService,
            DepartmentService departmentService,
            PasswordGenerator passwordGenerator) {
        this.accountService = accountService;
        this.departmentService = departmentService;
        this.passwordGenerator = passwordGenerator;
    }

    @Async("defaultSetupTaskExecutor")
    public void setAccountDefault(String companyCode, String companyEmail, String companyName, String actorCode) {
        try {
            String password = passwordGenerator.generate();
            AccountRequest request = new AccountRequest(
                    companyEmail,
                    password,
                    RoleType.ROLE_COMPANY_MANAGER,
                    companyCode,
                    companyName);
            accountService.createDefaultAccount(request, actorCode);
        } catch (Exception e) {
            log.warn("AUDIT action=CREATE_DEFAULT_ACCOUNT actor={} companyCode={} result=FAILED error={}",
                    actorCode, companyCode, e.getMessage());
        }
    }

    @Async("defaultSetupTaskExecutor")
    public void setDepartmentDefault(String companyCode, String actorCode) {
        try {
            DepartmentRequest request = new DepartmentRequest();
            request.setName(RoleType.DEFAULT_DEPARTMENT_MANAGER_NAME);
            request.setDescription(null);
            request.setCompanyCode(companyCode);
            departmentService.createDefaultDepartment(request, actorCode);
        } catch (Exception e) {
            log.warn("AUDIT action=CREATE_DEFAULT_DEPARTMENT actor={} companyCode={} result=FAILED error={}",
                    actorCode, companyCode, e.getMessage());
        }
    }
}
