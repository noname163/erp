package com.dat.erp.services.impl;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dat.erp.constants.CodePrefixes;
import com.dat.erp.constants.Messages;
import com.dat.erp.constants.RoleType;
import com.dat.erp.dto.request.CreateEmployeeRequest;
import com.dat.erp.dto.request.EmailRequest;
import com.dat.erp.dto.request.UserProfileCreateRequest;
import com.dat.erp.dto.response.EmployeeResponse;
import com.dat.erp.entities.Account;
import com.dat.erp.entities.Role;
import com.dat.erp.entities.UserProfile;
import com.dat.erp.exceptions.BadRequestException;
import com.dat.erp.exceptions.ConflictException;
import com.dat.erp.mapper.interfaces.EmployeeAccountMapper;
import com.dat.erp.mapper.interfaces.UserProfileMapper;
import com.dat.erp.repositories.customrepositories.AccountRepository;
import com.dat.erp.repositories.customrepositories.RoleRepository;
import com.dat.erp.services.CodeGenerator;
import com.dat.erp.services.EmailService;
import com.dat.erp.services.EmployeeAccountService;
import com.dat.erp.services.PasswordGenerator;
import com.dat.erp.services.SecurityContextService;
import com.dat.erp.services.UserProfileService;
import com.dat.erp.services.base.AbstractAuditableService;
import com.dat.erp.systemconfigs.CustomUserDetails;
import com.dat.erp.utils.CryptoUtils;

@Service
public class EmployeeAccountServiceImpl extends AbstractAuditableService implements EmployeeAccountService {
    private static final Logger log = LoggerFactory.getLogger(EmployeeAccountServiceImpl.class);

    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;
    private final UserProfileService userProfileService;
    private final EmailService emailService;
    private final PasswordGenerator passwordGenerator;
    private final EmployeeAccountMapper employeeAccountMapper;
    private final UserProfileMapper userProfileMapper;

    public EmployeeAccountServiceImpl(
            AccountRepository accountRepository,
            RoleRepository roleRepository,
            UserProfileService userProfileService,
            EmailService emailService,
            PasswordGenerator passwordGenerator,
            CodeGenerator codeGenerator,
            SecurityContextService securityContextService,
            EmployeeAccountMapper employeeAccountMapper,
            UserProfileMapper userProfileMapper) {
        this.accountRepository = accountRepository;
        this.roleRepository = roleRepository;
        this.userProfileService = userProfileService;
        this.emailService = emailService;
        this.passwordGenerator = passwordGenerator;
        this.codeGenerator = codeGenerator;
        this.securityContextService = securityContextService;
        this.employeeAccountMapper = employeeAccountMapper;
        this.userProfileMapper = userProfileMapper;
    }

    @Transactional
    @Override
    public EmployeeResponse createEmployee(CreateEmployeeRequest request) {
        CustomUserDetails currentUser = securityContextService.getCurrentUser();

        String actorCode = currentUser.getCode();
        String companyCode = currentUser.getAccount() == null ? null : currentUser.getAccount().getCompanyCode();
        if (companyCode == null || companyCode.isBlank()) {
            throw new BadRequestException(Messages.ERROR_CURRENT_USER_COMPANY_MISSING);
        }
        if (RoleType.ROLE_ADMIN.equals(request.getRoleCode())
                || RoleType.ROLE_SYSTEM_ADMIN.equals(request.getRoleCode())) {
            throw new BadRequestException(Messages.ERROR_CANNOT_CREATE_ADMIN_OR_MANAGER_EMPLOYEE);
        }
        accountRepository.findByEmail(request.getEmail())
                .ifPresent(existing -> {
                    throw new ConflictException(Messages.ERROR_ACCOUNT_EMAIL_EXISTS);
                });

        Role role = resolveRole(request.getRoleCode());

        String rawPassword = passwordGenerator.generate();

        Account account = employeeAccountMapper.toAccount(request);
        account.setPasswordHash(CryptoUtils.hash(rawPassword));
        account.setIsActive(true);
        account.setRole(role);
        generateCodeIfMissing(account, CodePrefixes.ACCOUNT);
        applyInsertAudit(account);
        accountRepository.save(account);

        UserProfileCreateRequest profileCreateRequest = employeeAccountMapper.toUserProfileCreateRequest(request,
                account.getCode());
        UserProfile profile = userProfileService.createUserProfile(profileCreateRequest);

        sendWelcomeEmailAsync(profile, account, rawPassword);

        EmployeeResponse response = userProfileMapper.toEmployeeResponse(profile);

        log.info("AUDIT action=CREATE_EMPLOYEE actor={} companyCode={} result=SUCCESS employeeCode={} email={}",
                actorCode, companyCode, profile.getCode(), account.getEmail());

        return response;
    }

    private Role resolveRole(String roleCodeOrName) {
        return roleRepository.findByType(roleCodeOrName)
                .or(() -> roleRepository.findByName(roleCodeOrName))
                .orElseThrow(() -> new BadRequestException(
                        String.format(Messages.ERROR_ROLE_NOT_FOUND_WITH_CODE, roleCodeOrName)));
    }

    private void sendWelcomeEmailAsync(UserProfile profile, Account account, String rawPassword) {
        try {
            EmailRequest emailRequest = new EmailRequest();
            emailRequest.setFrom(null);
            emailRequest.setTo(account.getEmail());
            emailRequest.setFullName(userProfileMapper.buildFullName(profile.getFirstName(), profile.getLastName()));
            emailRequest.setGender(null);
            emailRequest.setHtmlFilePath("email/create-account.html");

            Map<String, Object> vars = new HashMap<>();
            vars.put("username", account.getEmail());
            vars.put("password", rawPassword);
            vars.put("firstLoginInstruction", "Please log in and change your password after the first login.");
            emailRequest.setTemplateVariables(vars);

            emailService.sendCreateAccountMail(emailRequest);
        } catch (Exception e) {
            log.warn("AUDIT action=SEND_ACCOUNT_EMAIL result=FAILED to={} error={}", account.getEmail(),
                    e.getMessage());
        }
    }
}
