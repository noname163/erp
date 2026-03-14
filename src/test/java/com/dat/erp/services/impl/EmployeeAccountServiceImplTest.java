package com.dat.erp.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mapstruct.factory.Mappers;

import com.dat.erp.constants.CodePrefixes;
import com.dat.erp.dto.request.CreateEmployeeRequest;
import com.dat.erp.dto.request.EmailRequest;
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
    private SecurityContextService securityContextService;
    @Mock
    private CodeGenerator codeGenerator;
    @Mock
    private UserProfileRepository userProfileRepository;
    @Mock
    private UserSkillRepository userSkillRepository;

    private EmployeeAccountServiceImpl service;

    private CreateEmployeeRequest request;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        EmployeeAccountMapper mapper = Mappers.getMapper(EmployeeAccountMapper.class);
        UserProfileMapper userProfileMapper = Mappers.getMapper(UserProfileMapper.class);
        service = new EmployeeAccountServiceImpl(
                accountRepository,
                roleRepository,
                userProfileService,
                emailService,
                passwordGenerator,
                codeGenerator,
                securityContextService,
                mapper,
                userProfileMapper,
                userProfileRepository,
                userSkillRepository);
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
        actorAcc.setCode("ACC-ADMIN");
        actorAcc.setCompanyCode("CMP-1");
        Role adminRole = new Role();
        adminRole.setName("ADMIN");
        actorAcc.setRole(adminRole);
        when(securityContextService.getCurrentUser()).thenReturn(new CustomUserDetails(actorAcc, null));

        Department department = new Department();
        department.setCode("DPM-IT");
        department.setName("IT");
        department.setCompanyCode("CMP-1");
        when(departmentRepository.findByCodeAndCompanyCode("DPM-IT", "CMP-1")).thenReturn(Optional.of(department));

        when(accountRepository.findByEmail("employee@company.com")).thenReturn(Optional.empty());

        Role employeeRole = new Role();
        employeeRole.setName("EMPLOYEE");
        when(roleRepository.findByCode("EMPLOYEE")).thenReturn(Optional.empty());
        when(roleRepository.findByName("EMPLOYEE")).thenReturn(Optional.of(employeeRole));

        when(passwordGenerator.generate()).thenReturn("P@ssw0rd!");
        when(codeGenerator.nextCode(CodePrefixes.ACCOUNT)).thenReturn("ACC-000001");

        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));
        UserProfile savedProfile = new UserProfile();
        savedProfile.setCode("USR-000001");
        savedProfile.setFirstName("Nguyen");
        savedProfile.setLastName("Van A");
        savedProfile.setAccount(new Account());
        savedProfile.getAccount().setEmail("employee@company.com");
        savedProfile.getAccount().setRole(employeeRole);
        savedProfile.setDepartment(department);
        when(userProfileService.createUserProfile(any(UserProfileCreateRequest.class))).thenReturn(savedProfile);

        EmployeeResponse resp = service.createEmployee(request);

        assertNotNull(resp);
        assertEquals("USR-000001", resp.getCode());
        assertEquals("employee@company.com", resp.getEmail());
        assertEquals("Nguyen Van A", resp.getFullName());
        assertEquals("IT", resp.getDepartment());
        assertEquals("EMPLOYEE", resp.getRole());

        verify(accountRepository).save(any(Account.class));
        verify(userProfileService).createUserProfile(any(UserProfileCreateRequest.class));
        verify(emailService).sendCreateAccountMail(any(EmailRequest.class));
    }

    @Test
    void createEmployee_conflict_whenEmailExists() {
        Account actorAcc = new Account();
        actorAcc.setCode("ACC-ADMIN");
        actorAcc.setCompanyCode("CMP-1");
        Role role = new Role();
        role.setName("ADMIN");
        actorAcc.setRole(role);
        when(securityContextService.getCurrentUser()).thenReturn(new CustomUserDetails(actorAcc, null));

        Department department = new Department();
        department.setCode("DPM-IT");
        department.setName("IT");
        department.setCompanyCode("CMP-1");
        when(departmentRepository.findByCodeAndCompanyCode("DPM-IT", "CMP-1")).thenReturn(Optional.of(department));

        when(accountRepository.findByEmail("employee@company.com")).thenReturn(Optional.of(new Account()));

        assertThrows(ConflictException.class, () -> service.createEmployee(request));
        verify(accountRepository, never()).save(any());
        verify(userProfileService, never()).createUserProfile(any());
    }

    @Test
    void createEmployee_badRequest_whenRoleInvalid() {
        Account actorAcc = new Account();
        actorAcc.setCode("ACC-ADMIN");
        actorAcc.setCompanyCode("CMP-1");
        Role role = new Role();
        role.setName("ADMIN");
        actorAcc.setRole(role);
        when(securityContextService.getCurrentUser()).thenReturn(new CustomUserDetails(actorAcc, null));

        Department department = new Department();
        department.setCode("DPM-IT");
        department.setName("IT");
        department.setCompanyCode("CMP-1");
        when(departmentRepository.findByCodeAndCompanyCode("DPM-IT", "CMP-1")).thenReturn(Optional.of(department));

        when(accountRepository.findByEmail("employee@company.com")).thenReturn(Optional.empty());
        when(roleRepository.findByCode("EMPLOYEE")).thenReturn(Optional.empty());
        when(roleRepository.findByName("EMPLOYEE")).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> service.createEmployee(request));
        verify(accountRepository, never()).save(any());
    }
}
