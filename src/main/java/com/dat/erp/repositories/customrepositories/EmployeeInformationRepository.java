package com.dat.erp.repositories.customrepositories;

import java.util.Optional;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.dat.erp.entities.EmployeeInformation;

@Repository
public interface EmployeeInformationRepository extends JpaRepository<EmployeeInformation, Long> {

    @Query("SELECT emp FROM EmployeeInformation emp " +
            "JOIN FETCH emp.role " +
            "JOIN FETCH emp.company " +
            "JOIN FETCH emp.department " +
            "JOIN FETCH emp.user " +
            "WHERE emp.code = :employeeCode")
    Optional<EmployeeInformation> findBasicByEmployeeCode(@Param("employeeCode") String employeeCode);

    @EntityGraph(attributePaths = {
            "role",
            "company",
            "department",
            "user",
            "documents",
            "salaries",
            "shifts",
            "identifications",
            "dependents",
            "attendanceRecords"
    })
    @Query("SELECT emp FROM EmployeeInformation emp WHERE emp.code = :employeeCode")
    Optional<EmployeeInformation> findFullByEmployeeCode(@Param("employeeCode") String employeeCode);
}
