package com.dat.erp.data;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.dat.erp.entities.*;
import com.dat.erp.repositories.*;
import com.dat.erp.repositories.customrepositories.CompanyRepository;
import com.dat.erp.repositories.customrepositories.DepartmentRepository;
import com.dat.erp.repositories.customrepositories.EmployeeInformationRepository;
import com.dat.erp.repositories.customrepositories.RoleRepository;
import com.dat.erp.repositories.customrepositories.UserInformationRepository;

@Configuration
public class Data {

    @Bean
    CommandLineRunner initData(
            UserInformationRepository userRepo,
            DepartmentRepository departmentRepo,
            RoleRepository roleRepo,
            EmployeeInformationRepository employeeRepo,
            CompanyRepository companyRepo) {
        return args -> {
            // --- Create User ---
            UserInformation user = new UserInformation();
            user.setCode("USR001");
            user.setFirstName("Dat");
            user.setLastName("Huu");
            user.setEmail("datpersonal@example.com");
            user.setDateOfBirth("1995-01-01");
            user.setGender("Male");
            user.setPhoneNumber("0123456789");
            userRepo.save(user);

            // --- Create Company ---
            Company company = new Company();
            company.setCode("COMP001");
            company.setName("NashTech Vietnam");
            company.setCreatedAt(LocalDateTime.now());
            companyRepo.save(company);

            // --- Create Department ---
            Department department = new Department();
            department.setCode("DEP001");
            department.setName("IT Department");
            department.setStatus("ACTIVE");
            department.setCreatedAt(LocalDateTime.now());
            departmentRepo.save(department);

            // --- Create Role ---
            Role role = new Role();
            role.setCode("ROLE001");
            role.setName("ADMIN");
            role.setPermission(10);
            role.setLevel(1);
            role.setCreatedAt(LocalDateTime.now());
            roleRepo.save(role);

            // --- Create Employee ---
            EmployeeInformation emp = new EmployeeInformation();
            emp.setCode("EMP001");
            emp.setUser(user);
            emp.setEmail("dat@example.com");
            emp.setPassword("123456"); // ⚠️ normally encode it
            emp.setDepartment(department);
            emp.setCompany(company);
            emp.setRole(role);
            emp.setJobTitle("Java Developer");
            emp.setEmploymentStatus("FULL_TIME");
            emp.setHireDate(LocalDate.now());
            emp.setCreatedAt(LocalDateTime.now());
            employeeRepo.save(emp);

            System.out.println("✅ Sample data inserted!");
        };
    }
}
