package com.dat.erp.repositories.customrepositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dat.erp.entities.Company;
import com.dat.erp.entities.Department;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    Optional<Department> findByCode(String code);

    boolean existsByNameAndCompany(String name, Company company);
}
