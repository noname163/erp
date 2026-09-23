package com.dat.erp.services.impl;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dat.erp.constants.CodePrefixes;
import com.dat.erp.constants.Messages;
import com.dat.erp.dto.request.UserProfileCreateRequest;
import com.dat.erp.dto.response.PagedResponse;
import com.dat.erp.dto.response.SelectionOptionResponse;
import com.dat.erp.entities.Account;
import com.dat.erp.entities.Department;
import com.dat.erp.entities.UserProfile;
import com.dat.erp.exceptions.BadRequestException;
import com.dat.erp.mapper.interfaces.UserProfileMapper;
import com.dat.erp.repositories.customrepositories.AccountRepository;
import com.dat.erp.repositories.customrepositories.DepartmentRepository;
import com.dat.erp.repositories.customrepositories.UserProfileRepository;
import com.dat.erp.services.CodeGenerator;
import com.dat.erp.services.SecurityContextService;
import com.dat.erp.services.UserProfileService;
import com.dat.erp.utils.CustomStringUtils;
import com.dat.erp.utils.PageableUtils;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {
    private final AccountRepository accountRepository;
    private final DepartmentRepository departmentRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserProfileMapper userProfileMapper;
    private final CodeGenerator codeGenerator;
    private final SecurityContextService securityContextService;

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
        profile.initializeCode(codeGenerator.nextCode(CodePrefixes.USER));
        profile.assignCompanyCode(companyCode);
        profile.setAccount(account);
        profile.setDepartment(department);
        profile.setHireDate(LocalDate.now());
        profile.setIsActive(true);
        return userProfileRepository.save(profile);
    }

    @Override
    public PagedResponse<SelectionOptionResponse> getUserProfileOptionsByFirstName(String firstName, Integer page,
            Integer size, String sortBy, String sortDir) {
        String companyCode = securityContextService.getCurrentCompanyCode();
        String normalizedFirstName = CustomStringUtils.trimToNull(firstName);
        Pageable pageable = PageableUtils.create(page, size, sortBy, sortDir);
        Page<UserProfile> profiles = userProfileRepository.findOptionsByFilters(companyCode, normalizedFirstName, pageable);

        return PageableUtils.mapPage(profiles, userProfileMapper::toOptionResponse, Messages.SUCCESS);
    }

    @Override
    public List<String> getActiveUserProfileCodesOfCurrentCompany() {
        String companyCode = securityContextService.getCurrentCompanyCode();
        if (companyCode == null || companyCode.isBlank() || "SYSTEM".equals(companyCode)) {
            throw new BadRequestException(Messages.ERROR_CURRENT_USER_COMPANY_MISSING);
        }
        return userProfileRepository.findActiveCodesByCompanyCode(companyCode);
    }
}
