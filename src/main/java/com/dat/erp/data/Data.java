package com.dat.erp.data;

import java.time.LocalDate;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.dat.erp.constants.CodePrefixes;
import com.dat.erp.constants.Defaults;
import com.dat.erp.constants.RoleType;
import com.dat.erp.entities.Account;
import com.dat.erp.entities.Company;
import com.dat.erp.entities.Department;
import com.dat.erp.entities.Role;
import com.dat.erp.entities.UserProfile;
import com.dat.erp.repositories.customrepositories.CompanyRepository;
import com.dat.erp.repositories.customrepositories.DepartmentRepository;
import com.dat.erp.repositories.customrepositories.AccountRepository;
import com.dat.erp.repositories.customrepositories.RoleRepository;
import com.dat.erp.repositories.customrepositories.UserProfileRepository;
import com.dat.erp.utils.CryptoUtils;

@Configuration
public class Data {

    private static final String ADMIN_EMAIL = "admin@gmail.com";
    private static final String ADMIN_PASSWORD = "admin";

    private static final String COMPANY_CODE = CodePrefixes.COMPANY + "DEFAULT";
    private static final String DEPARTMENT_CODE = CodePrefixes.DEPARTMENT + "GENERAL";
    private static final String ROLE_CODE = CodePrefixes.ROLE + "ADMIN";
    private static final String HR_ROLE_CODE = CodePrefixes.ROLE + "HR";
    private static final String HR_ROLE_EMPLOYEE = CodePrefixes.ROLE + "EMPL";
    private static final String ROLE_NAME = "ADMIN";

    @Bean
    CommandLineRunner initData(
            AccountRepository accountRepository,
            DepartmentRepository departmentRepository,
            RoleRepository roleRepository,
            CompanyRepository companyRepository,
            UserProfileRepository userProfileRepository) {
        return args -> {
            companyRepository.findByCode(COMPANY_CODE)
                    .orElseGet(() -> {
                        Company newCompany = new Company();
                        newCompany.setCode(COMPANY_CODE);
                        newCompany.setName("Demo Company");
                        newCompany.setEmail("info@demo.local");
                        newCompany.setPhoneNumber("000-000-0000");
                        return companyRepository.save(newCompany);
                    });

            Department department = departmentRepository.findByCode(DEPARTMENT_CODE)
                    .orElseGet(() -> {
                        Department newDepartment = new Department();
                        newDepartment.setCode(DEPARTMENT_CODE);
                        newDepartment.setName("General");
                        newDepartment.setCompanyCode(COMPANY_CODE);
                        return departmentRepository.save(newDepartment);
                    });

            Role adminRole = roleRepository.findByCode(ROLE_CODE)
                    .orElseGet(() -> {
                        Role newRole = new Role();
                        newRole.setCode(ROLE_CODE);
                        newRole.setName(ROLE_NAME);
                        newRole.setType(RoleType.ROLE_ADMIN);
                        newRole.setDescription("System administrator role with full access");
                        return roleRepository.save(newRole);
                    });

            roleRepository.findByCode(HR_ROLE_EMPLOYEE)
                    .orElseGet(() -> {
                        Role newRole = new Role();
                        newRole.setCode(HR_ROLE_EMPLOYEE);
                        newRole.setName("EMPLOYEE");
                        newRole.setType(RoleType.ROLE_EMPLOYEE);
                        newRole.setDescription("Employee role with limited access");
                        return roleRepository.save(newRole);
                    });
            roleRepository.findByCode(HR_ROLE_CODE)
                    .orElseGet(() -> {
                        Role newRole = new Role();
                        newRole.setCode(HR_ROLE_CODE);
                        newRole.setName("Human Resources");
                        newRole.setType(RoleType.ROLE_HUMAN_RESOURCES);
                        newRole.setDescription("Human resources role with limited access");
                        return roleRepository.save(newRole);
                    });

            accountRepository.findByEmail(ADMIN_EMAIL).orElseGet(() -> {
                Account account = new Account();
                account.setCode("ACC-ADMIN");
                account.setEmail(ADMIN_EMAIL);
                account.setPasswordHash(CryptoUtils.hash(ADMIN_PASSWORD));
                account.setIsActive(true);
                account.setRole(adminRole);
                account.setUserProfile(null);
                account.setCompanyCode(COMPANY_CODE);
                return accountRepository.save(account);
            });

            userProfileRepository.findByAccount_Code("ACC-ADMIN").orElseGet(() -> {
                UserProfile profile = new UserProfile();
                profile.setCode("USR-ADMIN");
                profile.setAccount(accountRepository.findByEmail(ADMIN_EMAIL).orElseThrow());
                profile.setFirstName("System");
                profile.setLastName("Administrator");
                profile.setDepartment(department);
                profile.setHireDate(LocalDate.now());
                profile.setIsActive(true);
                return userProfileRepository.save(profile);
            });
        };
    }
}
