package com.dat.erp.repositories.customrepositories;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;


import com.dat.erp.entities.EmployeeSalaryDetail;


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

    @Query("""
            select d
            from EmployeeSalaryDetail d
            join fetch d.salary s
            left join fetch d.dependenceCode dc
            where d.companyCode = :companyCode
              and d.isDeleted = false
              and d.employeeSalary.code = :employeeSalaryCode
            """)
    List<EmployeeSalaryDetail> findForPayrollByEmployeeSalaryCodeAndCompanyCode(
            @Param("employeeSalaryCode") String employeeSalaryCode,
            @Param("companyCode") String companyCode);
}
