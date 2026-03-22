package com.dat.erp.repositories.customrepositories;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.dat.erp.entities.EmployeePto;

@Repository
public interface EmployeePtoRepository extends JpaRepository<EmployeePto, Long> {
    @Query("""
            select p
            from EmployeePto p
            where p.companyCode = :companyCode
              and p.userProfile.code = :userProfileCode
              and p.isDeleted = false
              and p.startDate <= :periodEnd
              and p.endDate >= :periodStart
            """)
    List<EmployeePto> findOverlappingPto(
            @Param("companyCode") String companyCode,
            @Param("userProfileCode") String userProfileCode,
            @Param("periodStart") LocalDate periodStart,
            @Param("periodEnd") LocalDate periodEnd);
}
