package com.dat.erp.repositories.customrepositories;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dat.erp.entities.EmployeeSalaryDetail;

@Repository
public interface EmployeeSalaryDetailRepository extends JpaRepository<EmployeeSalaryDetail, Long> {
    boolean existsByEmployeeSalary_CodeAndSalary_CodeAndIsDeletedFalse(String employeeSalaryCode, String salaryCode);

    @Query("""
            select d.salary.code
            from EmployeeSalaryDetail d
            where d.employeeSalary.code = :employeeSalaryCode
              and d.isDeleted = false
              and d.salary.code in :salaryCodes
            """)
    List<String> findExistingSalaryCodes(@Param("employeeSalaryCode") String employeeSalaryCode,
            @Param("salaryCodes") Collection<String> salaryCodes);
}
