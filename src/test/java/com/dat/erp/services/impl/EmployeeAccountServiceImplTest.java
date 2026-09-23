package com.dat.erp.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.jpa.domain.Specification;

import com.dat.erp.constants.CodePrefixes;
import com.dat.erp.dto.request.CreateEmployeeRequest;
import com.dat.erp.dto.request.EmailRequest;
import com.dat.erp.dto.request.EmployeeListRequest;
import com.dat.erp.dto.request.enums.SortType;
import com.dat.erp.dto.request.UserProfileCreateRequest;
import com.dat.erp.dto.response.EmployeeResponse;
import com.dat.erp.entities.Account;
import com.dat.erp.entities.Department;
import com.dat.erp.entities.Role;
import com.dat.erp.entities.UserProfile;
import com.dat.erp.exceptions.BadRequestException;
import com.dat.erp.exceptions.ConflictException;
import com.dat.erp.mapper.interfaces.EmployeeAccountMapper;
import com.dat.erp.mapper.interfaces.UserProfileMapper;
import com.dat.erp.repositories.customrepositories.AccountRepository;
import com.dat.erp.repositories.customrepositories.DepartmentRepository;
import com.dat.erp.repositories.customrepositories.RoleRepository;
import com.dat.erp.repositories.customrepositories.UserProfileRepository;
import com.dat.erp.repositories.customrepositories.UserSkillRepository;
import com.dat.erp.services.CodeGenerator;
import com.dat.erp.services.EmailService;
import com.dat.erp.services.PasswordGenerator;
import com.dat.erp.services.SecurityContextService;
import com.dat.erp.services.UserProfileService;
import com.dat.erp.systemconfigs.CustomUserDetails;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.JoinType;

class EmployeeAccountServiceImplTest {
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private DepartmentRepository departmentRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private UserProfileService userProfileService;
    @Mock
    private EmailService emailService;
    @Mock
    private PasswordGenerator passwordGenerator;
    @Mock
    private CodeGenerator codeGenerator;
    @Mock
    private SecurityContextService securityContextService;
    @Mock
    private UserProfileRepository userProfileRepository;
    @Mock
    private UserSkillRepository userSkillRepository;
    @Mock
    private EmployeeAccountMapper employeeAccountMapper;
    @Mock
    private UserProfileMapper userProfileMapper;

    private EmployeeAccountServiceImpl service;

    private CreateEmployeeRequest request;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new EmployeeAccountServiceImpl(
                accountRepository,
                roleRepository,
                userProfileService,
                emailService,
                passwordGenerator,
                codeGenerator,
                employeeAccountMapper,
                userProfileMapper,
                userProfileRepository,
                userSkillRepository,
                securityContextService);
        request = new CreateEmployeeRequest();
        request.setEmail("employee@company.com");
        request.setFirstName("Nguyen");
        request.setLastName("Van A");
        request.setDepartmentCode("DPM-IT");
        request.setRoleCode("EMPLOYEE");
        request.setGender("male");
        request.setPhone("0901234567");
    }

    @Test
    void createEmployee_success_persistsAllAndSendsEmail() {
        Account actorAcc = new Account();
        com.dat.erp.testutils.EntityTestData.setCode(actorAcc, "ACC-ADMIN");
        com.dat.erp.testutils.EntityTestData.setCompanyCode(actorAcc, "CMP-1");
        Role adminRole = new Role();
        adminRole.setName("ADMIN");
        actorAcc.setRole(adminRole);
        when(securityContextService.getCurrentUser()).thenReturn(new CustomUserDetails(actorAcc, null));
        when(securityContextService.getCurrentCompanyCode()).thenReturn(actorAcc.getCompanyCode());

        Department department = new Department();
        com.dat.erp.testutils.EntityTestData.setCode(department, "DPM-IT");
        department.setName("IT");
        com.dat.erp.testutils.EntityTestData.setCompanyCode(department, "CMP-1");
        when(departmentRepository.findByCodeAndCompanyCode("DPM-IT", "CMP-1")).thenReturn(Optional.of(department));

        when(accountRepository.findByEmail("employee@company.com")).thenReturn(Optional.empty());

        Role employeeRole = new Role();
        employeeRole.setName("EMPLOYEE");
        when(roleRepository.findByCode("EMPLOYEE")).thenReturn(Optional.empty());
        when(roleRepository.findByName("EMPLOYEE")).thenReturn(Optional.of(employeeRole));

        when(passwordGenerator.generate()).thenReturn("P@ssw0rd!");
        when(codeGenerator.nextCode(CodePrefixes.ACCOUNT)).thenReturn("ACC-000001");
        Account mappedAccount = new Account();
        mappedAccount.setEmail("employee@company.com");
        when(employeeAccountMapper.toAccount(request)).thenReturn(mappedAccount);
        when(employeeAccountMapper.toUserProfileCreateRequest(request, "ACC-000001")).thenReturn(new UserProfileCreateRequest());

        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));
        UserProfile savedProfile = new UserProfile();
        com.dat.erp.testutils.EntityTestData.setCode(savedProfile, "USR-000001");
        savedProfile.setFirstName("Nguyen");
        savedProfile.setLastName("Van A");
        savedProfile.setAccount(new Account());
        savedProfile.getAccount().setEmail("employee@company.com");
        savedProfile.getAccount().setRole(employeeRole);
        savedProfile.setDepartment(department);
        when(userProfileService.createUserProfile(any(UserProfileCreateRequest.class))).thenReturn(savedProfile);
        EmployeeResponse mappedResponse = new EmployeeResponse();
        mappedResponse.setCode("USR-000001");
        mappedResponse.setEmail("employee@company.com");
        mappedResponse.setFullName("Nguyen Van A");
        mappedResponse.setDepartment("IT");
        mappedResponse.setRole("EMPLOYEE");
        when(userProfileMapper.toEmployeeResponse(savedProfile)).thenReturn(mappedResponse);

        EmployeeResponse resp = service.createEmployee(request);

        assertNotNull(resp);
        assertEquals("USR-000001", resp.getCode());
        assertEquals("employee@company.com", resp.getEmail());
        assertEquals("Nguyen Van A", resp.getFullName());
        assertEquals("IT", resp.getDepartment());
        assertEquals("EMPLOYEE", resp.getRole());

        verify(accountRepository).save(any(Account.class));
        assertEquals("ACC-000001", mappedAccount.getCode());
        assertEquals("CMP-1", mappedAccount.getCompanyCode());
        verify(codeGenerator).nextCode(CodePrefixes.ACCOUNT);
        verify(userProfileService).createUserProfile(any(UserProfileCreateRequest.class));
        verify(emailService).sendCreateAccountMail(any(EmailRequest.class));
    }

    @Test
    void createEmployee_conflict_whenEmailExists() {
        Account actorAcc = new Account();
        com.dat.erp.testutils.EntityTestData.setCode(actorAcc, "ACC-ADMIN");
        com.dat.erp.testutils.EntityTestData.setCompanyCode(actorAcc, "CMP-1");
        Role role = new Role();
        role.setName("ADMIN");
        actorAcc.setRole(role);
        when(securityContextService.getCurrentUser()).thenReturn(new CustomUserDetails(actorAcc, null));
        when(securityContextService.getCurrentCompanyCode()).thenReturn(actorAcc.getCompanyCode());

        Department department = new Department();
        com.dat.erp.testutils.EntityTestData.setCode(department, "DPM-IT");
        department.setName("IT");
        com.dat.erp.testutils.EntityTestData.setCompanyCode(department, "CMP-1");
        when(departmentRepository.findByCodeAndCompanyCode("DPM-IT", "CMP-1")).thenReturn(Optional.of(department));

        when(accountRepository.findByEmail("employee@company.com")).thenReturn(Optional.of(new Account()));

        assertThrows(ConflictException.class, () -> service.createEmployee(request));
        verify(accountRepository, never()).save(any());
        verify(userProfileService, never()).createUserProfile(any());
    }

    @Test
    void createEmployee_badRequest_whenRoleInvalid() {
        Account actorAcc = new Account();
        com.dat.erp.testutils.EntityTestData.setCode(actorAcc, "ACC-ADMIN");
        com.dat.erp.testutils.EntityTestData.setCompanyCode(actorAcc, "CMP-1");
        Role role = new Role();
        role.setName("ADMIN");
        actorAcc.setRole(role);
        when(securityContextService.getCurrentUser()).thenReturn(new CustomUserDetails(actorAcc, null));
        when(securityContextService.getCurrentCompanyCode()).thenReturn(actorAcc.getCompanyCode());

        Department department = new Department();
        com.dat.erp.testutils.EntityTestData.setCode(department, "DPM-IT");
        department.setName("IT");
        com.dat.erp.testutils.EntityTestData.setCompanyCode(department, "CMP-1");
        when(departmentRepository.findByCodeAndCompanyCode("DPM-IT", "CMP-1")).thenReturn(Optional.of(department));

        when(accountRepository.findByEmail("employee@company.com")).thenReturn(Optional.empty());
        when(roleRepository.findByCode("EMPLOYEE")).thenReturn(Optional.empty());
        when(roleRepository.findByName("EMPLOYEE")).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> service.createEmployee(request));
        verify(accountRepository, never()).save(any());
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    void buildSpecification_humanResourcesScopeIncludesOwnProfile() throws Exception {
        Account hrAccount = new Account();
        com.dat.erp.testutils.EntityTestData.setCode(hrAccount, "ACC-HR");
        com.dat.erp.testutils.EntityTestData.setCompanyCode(hrAccount, "CMP-1");
        Role hrRole = new Role();
        hrRole.setName("HUMAN_RESOURCES");
        hrAccount.setRole(hrRole);

        UserProfile currentProfile = new UserProfile();
        com.dat.erp.testutils.EntityTestData.setId(currentProfile, 99L);
        com.dat.erp.testutils.EntityTestData.setCode(currentProfile, "USR-99");

        CustomUserDetails currentUser = new CustomUserDetails(hrAccount, currentProfile);
        EmployeeListRequest listRequest = EmployeeListRequest.builder()
                .pageNo(0)
                .pageSize(20)
                .orderBy("name")
                .sortType(SortType.ASC)
                .build();

        Method buildSpecification = EmployeeAccountServiceImpl.class.getDeclaredMethod(
                "buildSpecification",
                String.class,
                CustomUserDetails.class,
                String.class,
                EmployeeListRequest.class);
        buildSpecification.setAccessible(true);

        Specification<UserProfile> specification = (Specification<UserProfile>) buildSpecification.invoke(
                service,
                "CMP-1",
                currentUser,
                "HUMAN_RESOURCES",
                listRequest);

        CriteriaBuilder criteriaBuilder = org.mockito.Mockito.mock(CriteriaBuilder.class);
        CriteriaQuery criteriaQuery = org.mockito.Mockito.mock(CriteriaQuery.class);
        Root<UserProfile> root = org.mockito.Mockito.mock(Root.class);
        Join accountJoin = org.mockito.Mockito.mock(Join.class);
        Join departmentJoin = org.mockito.Mockito.mock(Join.class);
        Path isDeletedPath = org.mockito.Mockito.mock(Path.class);
        Path companyCodePath = org.mockito.Mockito.mock(Path.class);
        Path createdByPath = org.mockito.Mockito.mock(Path.class);
        Path idPath = org.mockito.Mockito.mock(Path.class);
        Predicate basePredicate = org.mockito.Mockito.mock(Predicate.class);
        Predicate filterPredicate = org.mockito.Mockito.mock(Predicate.class);
        Predicate createdByPredicate = org.mockito.Mockito.mock(Predicate.class);
        Predicate selfPredicate = org.mockito.Mockito.mock(Predicate.class);
        Predicate rolePredicate = org.mockito.Mockito.mock(Predicate.class);
        Predicate finalPredicate = org.mockito.Mockito.mock(Predicate.class);

        when(root.join("account", JoinType.LEFT)).thenReturn(accountJoin);
        when(root.join("department", JoinType.LEFT)).thenReturn(departmentJoin);
        when(root.get("isDeleted")).thenReturn(isDeletedPath);
        when(root.get("createdBy")).thenReturn(createdByPath);
        when(root.get("id")).thenReturn(idPath);
        when(accountJoin.get("companyCode")).thenReturn(companyCodePath);

        when(criteriaBuilder.conjunction()).thenReturn(filterPredicate);
        when(criteriaBuilder.isFalse(isDeletedPath)).thenReturn(org.mockito.Mockito.mock(Predicate.class));
        when(criteriaBuilder.equal(companyCodePath, "CMP-1")).thenReturn(org.mockito.Mockito.mock(Predicate.class));
        when(criteriaBuilder.equal(createdByPath, "ACC-HR")).thenReturn(createdByPredicate);
        when(criteriaBuilder.equal(idPath, 99L)).thenReturn(selfPredicate);
        when(criteriaBuilder.or(createdByPredicate, selfPredicate)).thenReturn(rolePredicate);
        when(criteriaBuilder.and(any(Predicate[].class))).thenReturn(basePredicate, finalPredicate);

        specification.toPredicate(root, criteriaQuery, criteriaBuilder);

        verify(criteriaBuilder, times(1)).or(createdByPredicate, selfPredicate);
    }
}
