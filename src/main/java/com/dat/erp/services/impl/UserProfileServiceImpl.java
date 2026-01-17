package com.dat.erp.services.impl;

import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dat.erp.constants.CodePrefixes;
import com.dat.erp.constants.Messages;
import com.dat.erp.dto.request.UserProfileCreateRequest;
import com.dat.erp.entities.Account;
import com.dat.erp.entities.Department;
import com.dat.erp.entities.UserProfile;
import com.dat.erp.exceptions.BadRequestException;
import com.dat.erp.mapper.interfaces.UserProfileMapper;
import com.dat.erp.repositories.customrepositories.AccountRepository;
import com.dat.erp.repositories.customrepositories.DepartmentRepository;
import com.dat.erp.repositories.customrepositories.UserProfileRepository;
import com.dat.erp.services.UserProfileService;
import com.dat.erp.services.base.AbstractAuditableService;

@Service
public class UserProfileServiceImpl extends AbstractAuditableService implements UserProfileService {
    private final AccountRepository accountRepository;
    private final DepartmentRepository departmentRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserProfileMapper userProfileMapper;

    public UserProfileServiceImpl(
            AccountRepository accountRepository,
            DepartmentRepository departmentRepository,
            UserProfileRepository userProfileRepository,
            UserProfileMapper userProfileMapper) {
        this.accountRepository = accountRepository;
        this.departmentRepository = departmentRepository;
        this.userProfileRepository = userProfileRepository;
        this.userProfileMapper = userProfileMapper;
    }

    @Transactional
    @Override
    public UserProfile createUserProfile(UserProfileCreateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("UserProfileCreateRequest cannot be null");
        }

        Account account = accountRepository.findByCode(request.getAccountCode())
                .orElseThrow(() -> new BadRequestException(
                        String.format(Messages.ERROR_ACCOUNT_NOT_FOUND_WITH_CODE, request.getAccountCode())));

        String companyCode = account.getCompanyCode();
        if (companyCode == null || companyCode.isBlank()) {
            throw new BadRequestException(Messages.ERROR_CURRENT_USER_COMPANY_MISSING);
        }

        Department department = departmentRepository.findByCodeAndCompanyCode(request.getDepartmentCode(), companyCode)
                .orElseThrow(() -> new BadRequestException(
                        String.format(Messages.ERROR_DEPARTMENT_NOT_FOUND_WITH_CODE, request.getDepartmentCode())));

        UserProfile profile = userProfileMapper.toUserProfile(request);
        profile.setAccount(account);
        profile.setDepartment(department);
        profile.setHireDate(LocalDate.now());
        profile.setIsActive(true);

        generateCodeIfMissing(profile, CodePrefixes.USER);
        applyInsertAudit(profile);
        return userProfileRepository.save(profile);
    }
}
