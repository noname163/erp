package com.dat.erp.services.impl;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dat.erp.constants.RoleType;
import com.dat.erp.dto.request.AccountRequest;
import com.dat.erp.dto.request.EmailRequest;
import com.dat.erp.entities.Account;
import com.dat.erp.entities.Role;
import com.dat.erp.repositories.customrepositories.AccountRepository;
import com.dat.erp.repositories.customrepositories.RoleRepository;
import com.dat.erp.services.AccountService;
import com.dat.erp.services.EmailService;
import com.dat.erp.services.PasswordGenerator;
import com.dat.erp.utils.CryptoUtils;

@Service
public class AccountServiceImpl implements AccountService {
    private static final Logger log = LoggerFactory.getLogger(AccountServiceImpl.class);

    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;
    private final EmailService emailService;
    private final PasswordGenerator passwordGenerator;

    public AccountServiceImpl(
            AccountRepository accountRepository,
            RoleRepository roleRepository,
            EmailService emailService,
            PasswordGenerator passwordGenerator) {
        this.accountRepository = accountRepository;
        this.roleRepository = roleRepository;
        this.emailService = emailService;
        this.passwordGenerator = passwordGenerator;
    }

    @Transactional
    @Override
    public String createDefaultAccount(AccountRequest request, String actorCode) {
        if (request == null) {
            throw new IllegalArgumentException("AccountRequest cannot be null");
        }
        String username = request.getUsername();
        String companyCode = request.getCompanyCode();

        Account existing = accountRepository.findByEmail(username).orElse(null);
        if (existing != null) {
            log.info("AUDIT action=CREATE_DEFAULT_ACCOUNT actor={} companyCode={} result=ALREADY_EXISTS username={}",
                    actorCode, companyCode, username);
            return existing.getCode();
        }

        Role role = resolveOrCreateRole(request.getRoleType());

        String rawPassword = request.getPassword();
        if (rawPassword == null || rawPassword.isBlank()) {
            rawPassword = passwordGenerator.generate();
        }

        Account account = new Account();
        account.setEmail(username);
        account.setPasswordHash(CryptoUtils.hash(rawPassword));
        account.setIsActive(true);
        account.setRole(role);
        accountRepository.save(account);

        log.info(
                "AUDIT action=CREATE_DEFAULT_ACCOUNT actor={} companyCode={} result=SUCCESS username={} accountCode={}",
                actorCode, companyCode, username, account.getCode());

        sendCreateAccountEmail(username, rawPassword, request.getFullName(), actorCode, companyCode);
        return account.getCode();
    }

    private Role resolveOrCreateRole(String roleType) {
        String normalized = (roleType == null || roleType.isBlank()) ? RoleType.ROLE_COMPANY_MANAGER : roleType.trim();
        return roleRepository.findByType(normalized).orElseGet(() -> {
            Role role = new Role();
            role.setName(normalized);
            role.setType(normalized);
            role.setDescription("Auto-created role");
            return roleRepository.save(role);
        });
    }

    private void sendCreateAccountEmail(
            String toEmail,
            String rawPassword,
            String fullName,
            String actorCode,
            String companyCode) {
        try {
            EmailRequest emailRequest = new EmailRequest();
            emailRequest.setFrom(null);
            emailRequest.setTo(toEmail);
            emailRequest.setFullName((fullName == null || fullName.isBlank()) ? "Company Admin" : fullName);
            emailRequest.setGender(null);
            emailRequest.setHtmlFilePath("email/create-account.html");

            Map<String, Object> vars = new HashMap<>();
            vars.put("username", toEmail);
            vars.put("password", rawPassword);
            vars.put("firstLoginInstruction", "Please log in and change your password after the first login.");
            emailRequest.setTemplateVariables(vars);

            emailService.sendCreateAccountMail(emailRequest);
            log.info("AUDIT action=SEND_ACCOUNT_EMAIL actor={} companyCode={} result=QUEUED to={}",
                    actorCode, companyCode, toEmail);
        } catch (Exception e) {
            log.warn("AUDIT action=SEND_ACCOUNT_EMAIL actor={} companyCode={} result=FAILED to={} error={}",
                    actorCode, companyCode, toEmail, e.getMessage());
        }
    }
}
