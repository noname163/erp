package com.dat.erp.services.impl;

import java.time.Duration;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dat.erp.constants.CodePrefixes;
import com.dat.erp.constants.Messages;
import com.dat.erp.constants.RoleType;
import com.dat.erp.dto.request.CreateEmployeeRequest;
import com.dat.erp.dto.request.EmailRequest;
import com.dat.erp.dto.request.EmployeeListRequest;
import com.dat.erp.dto.request.UserProfileCreateRequest;
import com.dat.erp.dto.request.enums.EmployeeStatusFilter;
import com.dat.erp.dto.request.enums.SortType;
import com.dat.erp.dto.response.EmployeeResponse;
import com.dat.erp.dto.response.PaginationResponse;
import com.dat.erp.dto.response.employee.EmployeeListItem;
import com.dat.erp.entities.Account;
import com.dat.erp.entities.Role;
import com.dat.erp.entities.UserProfile;
import com.dat.erp.exceptions.BadRequestException;
import com.dat.erp.exceptions.ConflictException;
import com.dat.erp.exceptions.ForbiddenException;
import com.dat.erp.mapper.interfaces.EmployeeAccountMapper;
import com.dat.erp.mapper.interfaces.UserProfileMapper;
import com.dat.erp.repositories.customrepositories.AccountRepository;
import com.dat.erp.repositories.customrepositories.RoleRepository;
import com.dat.erp.repositories.customrepositories.UserProfileRepository;
import com.dat.erp.repositories.customrepositories.UserSkillRepository;
import com.dat.erp.repositories.projections.EmployeeSkillRow;
import com.dat.erp.services.CodeGenerator;
import com.dat.erp.services.EmailService;
import com.dat.erp.services.EmployeeAccountService;
import com.dat.erp.services.PasswordGenerator;
import com.dat.erp.services.SecurityContextService;
import com.dat.erp.services.UserProfileService;
import com.dat.erp.systemconfigs.CustomUserDetails;
import com.dat.erp.utils.CryptoUtils;

import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Subquery;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class EmployeeAccountServiceImpl implements EmployeeAccountService {
    private static final Logger log = LoggerFactory.getLogger(EmployeeAccountServiceImpl.class);

    private static final int DEFAULT_PAGE_NO = 0;
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

    private static final Set<String> ALLOWED_ROLE_TYPES = Set.of(
            RoleType.ROLE_ADMIN,
            RoleType.ROLE_COMPANY_MANAGER,
            RoleType.DEFAULT_DEPARTMENT_MANAGER_NAME,
            RoleType.ROLE_HUMAN_RESOURCES,
            RoleType.ROLE_EMPLOYEE);

    private static final Set<String> ALLOWED_ROLE_NAMES = Set.of(
            "ADMIN",
            "MANAGER",
            "COMPANY_MANAGER",
            "HUMAN_RESOURCES",
            "Human Resources",
            "HR",
            "EMPLOYEE",
            "STAFF");

    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;
    private final UserProfileService userProfileService;
    private final EmailService emailService;
    private final PasswordGenerator passwordGenerator;
    private final CodeGenerator codeGenerator;
    private final EmployeeAccountMapper employeeAccountMapper;
    private final UserProfileMapper userProfileMapper;
    private final UserProfileRepository userProfileRepository;
    private final UserSkillRepository userSkillRepository;
    private final SecurityContextService securityContextService;

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
        account.initializeCode(codeGenerator.nextCode(CodePrefixes.ACCOUNT));
        account.assignCompanyCode(companyCode);
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

    @Transactional(readOnly = true)
    @Override
    public PaginationResponse<EmployeeListItem> getEmployees(EmployeeListRequest request) {
        validate(request);

        CustomUserDetails currentUser = securityContextService.getCurrentUser();
        Account requester = currentUser.getAccount();
        String roleName = requester == null || requester.getRole() == null ? null : requester.getRole().getName();
        String roleType = requester == null || requester.getRole() == null ? null : requester.getRole().getType();
        String effectiveRole = (roleType == null || roleType.isBlank()) ? roleName : roleType;
        if (effectiveRole == null
                || (!ALLOWED_ROLE_TYPES.contains(effectiveRole) && !ALLOWED_ROLE_NAMES.contains(effectiveRole))) {
            throw new ForbiddenException("AUTH_403_001: Forbidden role");
        }

        String companyCode = requester.getCompanyCode();
        if (companyCode == null || companyCode.isBlank()) {
            throw new BadRequestException("EMP_400_008: Current user company missing");
        }

        Pageable pageable = PageRequest.of(
                request.getPageNo(),
                request.getPageSize(),
                resolveSort(request.getOrderBy(), request.getSortType()));

        Specification<UserProfile> spec = buildSpecification(companyCode, currentUser, effectiveRole, request);

        long pageStart = System.nanoTime();
        Page<UserProfile> page = userProfileRepository.findAll(spec, pageable);
        logIfSlow("employeeList.pageQuery", pageStart);

        List<UserProfile> profiles = page.getContent();
        List<Long> profileIds = profiles.stream().map(UserProfile::getId).toList();

        Map<Long, List<String>> skillsByProfileId = fetchSkillsByProfileIds(profileIds);
        Map<String, String> createdByAccountIdByCode = resolveCreatedByAccountIdsIfNeeded(effectiveRole, profiles);

        boolean isAdmin = RoleType.ROLE_ADMIN.equals(effectiveRole) || "ADMIN".equalsIgnoreCase(effectiveRole);
        List<EmployeeListItem> items = profiles.stream()
                .map(profile -> toListItem(profile, skillsByProfileId.get(profile.getId()), isAdmin,
                        createdByAccountIdByCode))
                .toList();

        log.info(
                "AUDIT action=EMPLOYEE_LIST_VIEW requesterId={} companyCode={} role={} filters=\"{}\" totalRow={}",
                requester.getId(),
                companyCode,
                effectiveRole,
                buildFilterSummary(request),
                page.getTotalElements());

        return PaginationResponse.<EmployeeListItem>builder()
                .totalPage(page.getTotalPages())
                .totalRow(page.getTotalElements())
                .data(items)
                .build();
    }

    private Role resolveRole(String roleCodeOrName) {
        return roleRepository.findByCode(roleCodeOrName)
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

    private void validate(EmployeeListRequest request) {
        if (request == null) {
            throw new BadRequestException("EMP_400_000: Request is required");
        }

        int pageNo = request.getPageNo() == null ? DEFAULT_PAGE_NO : request.getPageNo();
        int pageSize = request.getPageSize() == null ? DEFAULT_PAGE_SIZE : request.getPageSize();

        if (pageNo < 0) {
            throw new BadRequestException("EMP_400_009: pageNo must be >= 0");
        }
        if (pageSize < 1 || pageSize > MAX_PAGE_SIZE) {
            throw new BadRequestException("EMP_400_001: Invalid pageSize");
        }

        request.setPageNo(pageNo);
        request.setPageSize(pageSize);

        if (request.getSortType() == null) {
            request.setSortType(SortType.ASC);
        }
        if (request.getOrderBy() == null || request.getOrderBy().isBlank()) {
            request.setOrderBy("name");
        }

        if (request.getKeyword() != null && request.getKeyword().length() > 100) {
            throw new BadRequestException("EMP_400_010: keyword max length is 100");
        }

        Integer minAge = request.getMinAge();
        Integer maxAge = request.getMaxAge();
        if (minAge != null && minAge < 0) {
            throw new BadRequestException("EMP_400_011: minAge must be >= 0");
        }
        if (maxAge != null && maxAge < 0) {
            throw new BadRequestException("EMP_400_012: maxAge must be >= 0");
        }
        if (minAge != null && maxAge != null && minAge > maxAge) {
            throw new BadRequestException("EMP_400_003: minAge greater than maxAge");
        }
    }

    private static Sort resolveSort(String orderByRaw, SortType sortType) {
        String orderBy = orderByRaw == null ? "name" : orderByRaw.trim();
        boolean desc = sortType == SortType.DESC;

        return switch (orderBy) {
            case "name" -> Sort.by(desc ? Sort.Order.desc("lastName") : Sort.Order.asc("lastName"),
                    desc ? Sort.Order.desc("firstName") : Sort.Order.asc("firstName"));
            case "code" -> Sort.by(desc ? Sort.Direction.DESC : Sort.Direction.ASC, "employeeNumber");
            case "email" -> Sort.by(desc ? Sort.Direction.DESC : Sort.Direction.ASC, "account.email");
            case "age" -> {
                Sort.Direction birthDateDirection = desc ? Sort.Direction.ASC : Sort.Direction.DESC;
                yield Sort.by(birthDateDirection, "birthDate");
            }
            case "departmentName" -> Sort.by(desc ? Sort.Direction.DESC : Sort.Direction.ASC, "department.name");
            case "createdAt" -> Sort.by(desc ? Sort.Direction.DESC : Sort.Direction.ASC, "createdAt");
            default -> throw new BadRequestException("EMP_400_002: orderBy not allowed");
        };
    }

    private Specification<UserProfile> buildSpecification(
            String companyCode,
            CustomUserDetails currentUser,
            String requesterRole,
            EmployeeListRequest request) {

        return (root, query, cb) -> {
            Join<?, ?> accountJoin = root.join("account", JoinType.LEFT);
            Join<?, ?> departmentJoin = root.join("department", JoinType.LEFT);

            Predicate base = cb.and(
                    cb.isFalse(root.get("isDeleted")),
                    cb.equal(accountJoin.get("companyCode"), companyCode));

            Predicate roleScope = buildRoleScopePredicate(cb, root, currentUser, requesterRole);
            Predicate filters = buildFiltersPredicate(cb, query, root, accountJoin, departmentJoin, request);

            return cb.and(base, roleScope, filters);
        };
    }

    private Predicate buildRoleScopePredicate(
            jakarta.persistence.criteria.CriteriaBuilder cb,
            jakarta.persistence.criteria.Root<UserProfile> root,
            CustomUserDetails currentUser,
            String requesterRole) {

        if (isAdminOrManager(requesterRole)) {
            return cb.conjunction();
        }

        if (isHumanResources(requesterRole)) {
            Predicate createdByCurrentUser = cb.equal(root.get("createdBy"), currentUser.getCode());
            UserProfile self = currentUser.getUserProfile();
            if (self == null || self.getId() == null) {
                return createdByCurrentUser;
            }
            return cb.or(createdByCurrentUser, cb.equal(root.get("id"), self.getId()));
        }

        if (isStaff(requesterRole)) {
            UserProfile self = currentUser.getUserProfile();
            if (self == null || self.getId() == null) {
                return cb.disjunction();
            }
            return cb.equal(root.get("id"), self.getId());
        }

        return cb.disjunction();
    }

    private Predicate buildFiltersPredicate(
            jakarta.persistence.criteria.CriteriaBuilder cb,
            jakarta.persistence.criteria.CriteriaQuery<?> query,
            jakarta.persistence.criteria.Root<UserProfile> root,
            Join<?, ?> accountJoin,
            Join<?, ?> departmentJoin,
            EmployeeListRequest request) {

        Predicate predicate = cb.conjunction();

        if (request.getKeyword() != null && !request.getKeyword().isBlank()) {
            String keyword = request.getKeyword().trim().toLowerCase(Locale.ROOT);
            String like = "%" + keyword + "%";

            Expression<String> firstName = cb.lower(cb.coalesce(root.get("firstName"), ""));
            Expression<String> lastName = cb.lower(cb.coalesce(root.get("lastName"), ""));
            Expression<String> fullName = cb.lower(
                    cb.concat(cb.concat(cb.coalesce(root.get("firstName"), ""), " "), cb.coalesce(root.get("lastName"), "")));

            Expression<String> employeeNumber = cb.lower(cb.coalesce(root.get("employeeNumber"), ""));
            Expression<String> email = cb.lower(cb.coalesce(accountJoin.get("email"), ""));

            predicate = cb.and(predicate, cb.or(
                    cb.like(firstName, like),
                    cb.like(lastName, like),
                    cb.like(fullName, like),
                    cb.like(employeeNumber, like),
                    cb.like(email, like)));
        }

        if (request.getDepartmentIds() != null && !request.getDepartmentIds().isEmpty()) {
            predicate = cb.and(predicate, departmentJoin.get("id").in(request.getDepartmentIds()));
        }

        if (request.getSkillIds() != null && !request.getSkillIds().isEmpty()) {
            predicate = cb.and(predicate, buildSkillAndPredicate(cb, query, root, request.getSkillIds()));
        }

        if (request.getStatus() != null) {
            boolean isActive = request.getStatus() == EmployeeStatusFilter.ACTIVE;
            predicate = cb.and(predicate, cb.equal(root.get("isActive"), isActive));
        }

        Predicate agePredicate = buildAgePredicate(cb, root, request.getMinAge(), request.getMaxAge());
        if (agePredicate != null) {
            predicate = cb.and(predicate, agePredicate);
        }

        return predicate;
    }

    private Predicate buildSkillAndPredicate(
            jakarta.persistence.criteria.CriteriaBuilder cb,
            jakarta.persistence.criteria.CriteriaQuery<?> query,
            jakarta.persistence.criteria.Root<UserProfile> root,
            List<Long> skillIds) {

        Subquery<Long> subquery = query.subquery(Long.class);
        var us = subquery.from(com.dat.erp.entities.UserSkill.class);
        var skill = us.join("skill", JoinType.INNER);
        subquery.select(us.get("userProfile").get("id"));
        subquery.where(
                cb.isFalse(us.get("isDeleted")),
                cb.isFalse(skill.get("isDeleted")),
                skill.get("id").in(skillIds));
        subquery.groupBy(us.get("userProfile").get("id"));
        subquery.having(cb.equal(cb.countDistinct(skill.get("id")), (long) skillIds.size()));

        return root.get("id").in(subquery);
    }

    private static Predicate buildAgePredicate(
            jakarta.persistence.criteria.CriteriaBuilder cb,
            jakarta.persistence.criteria.Root<UserProfile> root,
            Integer minAge,
            Integer maxAge) {

        if (minAge == null && maxAge == null) {
            return null;
        }

        LocalDate today = LocalDate.now();
        LocalDate upperBirthDate = minAge == null ? null : today.minusYears(minAge);
        LocalDate lowerBirthDate = maxAge == null ? null : today.minusYears(maxAge + 1L).plusDays(1);

        if (lowerBirthDate != null && upperBirthDate != null) {
            return cb.between(root.get("birthDate"), lowerBirthDate, upperBirthDate);
        }
        if (lowerBirthDate != null) {
            return cb.greaterThanOrEqualTo(root.get("birthDate"), lowerBirthDate);
        }
        return cb.lessThanOrEqualTo(root.get("birthDate"), upperBirthDate);
    }

    private Map<Long, List<String>> fetchSkillsByProfileIds(List<Long> profileIds) {
        if (profileIds == null || profileIds.isEmpty()) {
            return Map.of();
        }

        long start = System.nanoTime();
        List<EmployeeSkillRow> rows = userSkillRepository.findSkillRowsByUserProfileIds(profileIds);
        logIfSlow("employeeList.skillsQuery", start);

        Map<Long, List<String>> result = new HashMap<>();
        for (EmployeeSkillRow row : rows) {
            result.computeIfAbsent(row.getUserProfileId(), ignored -> new java.util.ArrayList<>())
                    .add(row.getSkillName());
        }
        return result;
    }

    private Map<String, String> resolveCreatedByAccountIdsIfNeeded(String requesterRole, List<UserProfile> profiles) {
        boolean isAdmin = RoleType.ROLE_ADMIN.equals(requesterRole) || "ADMIN".equalsIgnoreCase(requesterRole);
        if (!isAdmin || profiles == null || profiles.isEmpty()) {
            return Map.of();
        }

        Set<String> creatorCodes = profiles.stream()
                .map(UserProfile::getCreatedBy)
                .filter(code -> code != null && !code.isBlank())
                .collect(Collectors.toSet());
        if (creatorCodes.isEmpty()) {
            return Map.of();
        }

        List<Account> accounts = accountRepository.findAllByCodeInWithUserProfile(creatorCodes);
        return accounts.stream().collect(Collectors.toMap(Account::getCode, account -> {
            UserProfile userProfile = account.getUserProfile();
            return userProfile == null ? null : userProfile.getFirstName() + " " + userProfile.getLastName();
        }, (a, b) -> a));
    }

    private static EmployeeListItem toListItem(
            UserProfile profile,
            List<String> skills,
            boolean isAdmin,
            Map<String, String> createdByAccountIdByCode) {

        Account account = profile.getAccount();
        String email = account == null ? null : account.getEmail();
        String name = buildFullName(profile.getFirstName(), profile.getLastName());
        Integer age = calculateAge(profile.getBirthDate(), LocalDate.now());

        String department = null;
        if (profile.getDepartment() != null) {
            department = profile.getDepartment().getName();
        }

        String createdBy = null;
        if (isAdmin && profile.getCreatedBy() != null) {
            createdBy = createdByAccountIdByCode.get(profile.getCreatedBy());
        }

        return EmployeeListItem.builder()
                .id(profile.getId())
                .code(profile.getEmployeeNumber())
                .name(name)
                .email(email)
                .age(age)
                .department(department)
                .skills(skills == null ? List.of() : skills)
                .status(Boolean.TRUE.equals(profile.getIsActive()) ? "ACTIVE" : "INACTIVE")
                .createdAt(profile.getCreatedAt())
                .createdBy(createdBy)
                .build();
    }

    private static String buildFullName(String firstName, String lastName) {
        String fn = firstName == null ? "" : firstName.trim();
        String ln = lastName == null ? "" : lastName.trim();
        String full = (fn + " " + ln).trim();
        return full.isBlank() ? null : full;
    }

    private static Integer calculateAge(LocalDate birthDate, LocalDate today) {
        if (birthDate == null) {
            return null;
        }
        if (today == null) {
            today = LocalDate.now();
        }
        if (birthDate.isAfter(today)) {
            return 0;
        }
        return Math.toIntExact(java.time.Period.between(birthDate, today).getYears());
    }

    private static void logIfSlow(String name, long startNano) {
        long elapsedMs = Duration.ofNanos(System.nanoTime() - startNano).toMillis();
        if (elapsedMs > 200) {
            log.warn("SLOW_DB_QUERY name={} durationMs={}", name, elapsedMs);
        } else {
            log.debug("DB_QUERY name={} durationMs={}", name, elapsedMs);
        }
    }

    private static String buildFilterSummary(EmployeeListRequest request) {
        Map<String, Object> summary = new HashMap<>();
        summary.put("pageNo", request.getPageNo());
        summary.put("pageSize", request.getPageSize());
        summary.put("orderBy", request.getOrderBy());
        summary.put("sortType", request.getSortType());
        summary.put("keyword", request.getKeyword());
        summary.put("minAge", request.getMinAge());
        summary.put("maxAge", request.getMaxAge());
        summary.put("departmentIds", request.getDepartmentIds());
        summary.put("skillIds", request.getSkillIds());
        summary.put("status", request.getStatus());
        return summary.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining(","));
    }

    private static boolean isAdminOrManager(String role) {
        if (role == null) {
            return false;
        }
        return RoleType.ROLE_ADMIN.equals(role)
                || RoleType.ROLE_COMPANY_MANAGER.equals(role)
                || RoleType.DEFAULT_DEPARTMENT_MANAGER_NAME.equals(role)
                || "MANAGER".equalsIgnoreCase(role);
    }

    private static boolean isHumanResources(String role) {
        if (role == null) {
            return false;
        }
        return RoleType.ROLE_HUMAN_RESOURCES.equals(role)
                || "HUMAN_RESOURCES".equalsIgnoreCase(role)
                || "Human Resources".equalsIgnoreCase(role)
                || "HR".equalsIgnoreCase(role);
    }

    private static boolean isStaff(String role) {
        if (role == null) {
            return false;
        }
        return RoleType.ROLE_EMPLOYEE.equals(role) || "STAFF".equalsIgnoreCase(role);
    }
}
