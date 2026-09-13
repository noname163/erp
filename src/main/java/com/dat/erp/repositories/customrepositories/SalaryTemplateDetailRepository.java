package com.dat.erp.repositories.customrepositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import com.dat.erp.entities.SalaryTemplateDetail;


public interface SalaryTemplateDetailRepository extends JpaRepository<SalaryTemplateDetail, Long> {
    @Query("""
            select std
            from SalaryTemplateDetail std
            join fetch std.salary s
            left join fetch std.dependenceCode dc
            left join fetch std.unit u
            where std.companyCode = :companyCode
              and std.isDeleted = false
              and std.salaryTemplate.code = :salaryTemplateCode
              and std.salaryTemplate.isDeleted = false
            order by std.sequenceOrder asc
            """)
    List<SalaryTemplateDetail> findBySalaryTemplateCodeAndCompanyCode(
            @Param("salaryTemplateCode") String salaryTemplateCode,
            @Param("companyCode") String companyCode);
}

