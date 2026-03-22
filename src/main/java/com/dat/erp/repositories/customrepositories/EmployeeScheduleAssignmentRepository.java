package com.dat.erp.repositories.customrepositories;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.dat.erp.entities.EmployeeScheduleAssignment;

@Repository
public interface EmployeeScheduleAssignmentRepository extends JpaRepository<EmployeeScheduleAssignment, Long> {
    @Query("""
            select a
            from EmployeeScheduleAssignment a
            join fetch a.workSchedule ws
            where a.companyCode = :companyCode
              and a.isDeleted = false
              and (a.userProfile.code = :userProfileCode or (a.userProfile is null and a.departmentCode = :departmentCode))
              and a.effectiveFrom <= :periodEnd
              and a.effectiveTo >= :periodStart
            order by a.priority asc, a.effectiveFrom asc
            """)
    List<EmployeeScheduleAssignment> findApplicableAssignments(
            @Param("companyCode") String companyCode,
            @Param("userProfileCode") String userProfileCode,
            @Param("departmentCode") String departmentCode,
            @Param("periodStart") LocalDate periodStart,
            @Param("periodEnd") LocalDate periodEnd);
}
