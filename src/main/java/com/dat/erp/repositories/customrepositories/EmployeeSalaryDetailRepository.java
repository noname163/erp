package com.dat.erp.repositories.customrepositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dat.erp.entities.EmployeeSalaryDetail;

@Repository
public interface EmployeeSalaryDetailRepository extends JpaRepository<EmployeeSalaryDetail, Long> {
    boolean existsByEmployeeSalary_CodeAndSalary_CodeAndIsDeletedFalse(String employeeSalaryCode, String salaryCode);
}

