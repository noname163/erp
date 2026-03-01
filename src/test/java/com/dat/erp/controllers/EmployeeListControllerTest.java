package com.dat.erp.controllers;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import com.dat.erp.entities.Account;
import com.dat.erp.entities.Department;
import com.dat.erp.entities.Role;
import com.dat.erp.entities.Skill;
import com.dat.erp.entities.UserProfile;
import com.dat.erp.entities.UserSkill;
import com.dat.erp.repositories.customrepositories.AccountRepository;
import com.dat.erp.repositories.customrepositories.DepartmentRepository;
import com.dat.erp.repositories.customrepositories.RoleRepository;
import com.dat.erp.repositories.customrepositories.SkillRepository;
import com.dat.erp.repositories.customrepositories.UserProfileRepository;
import com.dat.erp.repositories.customrepositories.UserSkillRepository;
import com.dat.erp.services.SecurityContextService;
import com.dat.erp.utils.JwtUtils;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EmployeeListControllerTest {

    private static final String COMPANY_CODE = "CMP-DEFAULT";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private UserSkillRepository userSkillRepository;

    @Autowired
    private SkillRepository skillRepository;

    @Autowired
    private SecurityContextService securityContextService;

    private Account adminAccount;
    private Account hrAccount;
    private Account staffAccount;

    private UserProfile adminProfile;
    private UserProfile hrProfile;
    private UserProfile staffProfile;
    private UserProfile hrCreatedEmployee;

    private Skill skillJava;
    private Skill skillPostgres;

    @BeforeAll
    void setUp() {
        Role hrRole = roleRepository.findByCode("ROLE-HR").orElseThrow();
        Role employeeRole = roleRepository.findByCode("ROLE-EMPL").orElseThrow();

        Department department = departmentRepository.findByCode("DPM-GENERAL").orElseThrow();

        adminAccount = accountRepository.findByCode("ACC-ADMIN").orElseThrow();
        adminProfile = userProfileRepository.findByAccount_Code(adminAccount.getCode()).orElseThrow();

        hrAccount = accountRepository.save(account("ACC-HR1", "hr1@example.com", hrRole));
        staffAccount = accountRepository.save(account("ACC-EMP1", "emp1@example.com", employeeRole));

        securityContextService.setCurrentUser(adminAccount.getCode());
        hrProfile = userProfileRepository.save(profile("USR-HR1", hrAccount, department, "EMP0100", "Hr",
                "User", LocalDate.of(1995, 1, 1), LocalDateTime.of(2026, 2, 1, 10, 0, 1)));

        securityContextService.setCurrentUser(adminAccount.getCode());
        staffProfile = userProfileRepository.save(profile("USR-EMP1", staffAccount, department, "EMP0001", "Dang",
                "Huu Dat", LocalDate.of(2000, 2, 10), LocalDateTime.of(2026, 2, 1, 10, 0, 2)));

        // HR-created employee
        Account anotherEmpAccount = accountRepository.save(account("ACC-EMP2", "emp2@example.com", employeeRole));
        securityContextService.setCurrentUser(hrAccount.getCode());
        hrCreatedEmployee = profile("USR-EMP2", anotherEmpAccount, department, "EMP0002", "Alice",
                "Nguyen", LocalDate.of(1998, 5, 1), LocalDateTime.of(2026, 2, 1, 10, 0, 3));
        hrCreatedEmployee = userProfileRepository.save(hrCreatedEmployee);

        // Skills
        skillJava = skillRepository.save(skill("SKL-JAVA", "Java"));
        skillPostgres = skillRepository.save(skill("SKL-PG", "PostgreSQL"));

        userSkillRepository.save(userSkill("USK-1", staffProfile, skillJava));
        userSkillRepository.save(userSkill("USK-2", staffProfile, skillPostgres));
        userSkillRepository.save(userSkill("USK-3", hrCreatedEmployee, skillJava));
    }

    @Test
    void missingJwt_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/employees"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("AUTH_401_001: Unauthorized"));
    }

    @Test
    void admin_seesCompanyEmployees_andCreatedByVisible() throws Exception {
        String token = jwtUtils.generateToken(adminAccount.getEmail(), adminAccount.getCode());

        mockMvc.perform(get("/api/v1/employees")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRow").value(4))
                .andExpect(jsonPath("$.data", hasSize(4)))
                .andExpect(jsonPath("$.data[*].createdBy").value(hasItem(hrAccount.getId().intValue())));
    }

    @Test
    void hr_seesOnlyEmployeesCreatedBySelf_andCreatedByMasked() throws Exception {
        // Ensure auditing does not overwrite createdBy with SYSTEM
        securityContextService.setCurrentUser(hrAccount.getCode());
        hrCreatedEmployee.setUpdatedAt(LocalDateTime.now());
        userProfileRepository.save(hrCreatedEmployee);

        String token = jwtUtils.generateToken(hrAccount.getEmail(), hrAccount.getCode());

        mockMvc.perform(get("/api/v1/employees")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRow").value(1))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].code").value("EMP0002"))
                .andExpect(jsonPath("$.data[0].createdBy").value(nullValue()));
    }

    @Test
    void staff_seesOnlySelf_andSkillFilterUsesAndLogic() throws Exception {
        String token = jwtUtils.generateToken(staffAccount.getEmail(), staffAccount.getCode());

        mockMvc.perform(get("/api/v1/employees")
                .param("skillIds", skillJava.getId() + "," + skillPostgres.getId())
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRow").value(1))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].code").value("EMP0001"))
                .andExpect(jsonPath("$.data[0].skills", hasSize(2)));
    }

    @Test
    void invalidPageSize_returns400WithCode() throws Exception {
        String token = jwtUtils.generateToken(adminAccount.getEmail(), adminAccount.getCode());

        mockMvc.perform(get("/api/v1/employees")
                .param("pageSize", "101")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("EMP_400_001: Invalid pageSize"));
    }

    private static Account account(String code, String email, Role role) {
        Account account = new Account();
        account.setCode(code);
        account.setEmail(email);
        account.setPasswordHash("hash");
        account.setIsActive(true);
        account.setRole(role);
        account.setCompanyCode(COMPANY_CODE);
        account.setIsDeleted(false);
        return account;
    }

    private static Department department(String code, String name) {
        Department department = new Department();
        department.setCode(code);
        department.setName(name);
        department.setCompanyCode(COMPANY_CODE);
        department.setIsDeleted(false);
        return department;
    }

    private static UserProfile profile(
            String code,
            Account account,
            Department department,
            String employeeNumber,
            String firstName,
            String lastName,
            LocalDate birthDate,
            LocalDateTime createdAt) {
        UserProfile profile = new UserProfile();
        profile.setCode(code);
        profile.setAccount(account);
        profile.setDepartment(department);
        profile.setEmployeeNumber(employeeNumber);
        profile.setFirstName(firstName);
        profile.setLastName(lastName);
        profile.setBirthDate(birthDate);
        profile.setIsActive(true);
        profile.setCompanyCode(COMPANY_CODE);
        profile.setIsDeleted(false);
        profile.setCreatedAt(createdAt);
        profile.setUpdatedAt(createdAt);
        profile.setCreatedBy(account.getCode());
        profile.setUpdatedBy(account.getCode());
        return profile;
    }

    private static Skill skill(String code, String name) {
        Skill skill = new Skill();
        skill.setCode(code);
        skill.setName(name);
        skill.setCompanyCode(COMPANY_CODE);
        skill.setIsDeleted(false);
        return skill;
    }

    private static UserSkill userSkill(String code, UserProfile userProfile, Skill skill) {
        UserSkill userSkill = new UserSkill();
        userSkill.setCode(code);
        userSkill.setUserProfile(userProfile);
        userSkill.setSkill(skill);
        userSkill.setCompanyCode(COMPANY_CODE);
        userSkill.setIsDeleted(false);
        return userSkill;
    }
}
