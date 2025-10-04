package com.dat.erp.repositories.customrepositories;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.dat.erp.entities.EmployeeInformation;

@Repository
public interface EmployeeInformationRepository
        extends JpaRepository<EmployeeInformation, Long>, JpaSpecificationExecutor<EmployeeInformation> {

    // Basic fetch (role, company, department, user)
    @EntityGraph(value = "Employee.basic")
    Optional<EmployeeInformation> findByCode(String code);

    @EntityGraph(value = "Employee.basic")
    Optional<EmployeeInformation> findByEmail(String email);

    // Full fetch (all associations)
    @EntityGraph(value = "Employee.full")
    Optional<EmployeeInformation> findDetailedByCode(String code);

    @EntityGraph(value = "Employee.full")
    Optional<EmployeeInformation> findDetailedByEmail(String email);

    @Query("SELECT e.code FROM EmployeeInformation e WHERE e.managerCode = :managerCode")
    List<String> findAllCodesByManagerCode(String managerCode);

    // Delete single employee by code
    @Modifying
    @Transactional
    @Query("DELETE FROM EmployeeInformation e WHERE e.code = :code")
    void deleteByCode(@Param("code") String code);

    // Delete multiple employees by codes
    @Modifying
    @Transactional
    @Query("DELETE FROM EmployeeInformation e WHERE e.code IN :codes")
    void deleteByCodes(@Param("codes") Collection<String> codes);
}
