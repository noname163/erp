package com.dat.erp.repositories.customrepositories;

import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.dat.erp.entities.EmployeePayrollInputVersion;

public interface EmployeePayrollInputVersionRepository extends JpaRepository<EmployeePayrollInputVersion, Long> {
    Optional<EmployeePayrollInputVersion> findFirstByCompanyCodeAndEmployeeCodeAndEffectiveFromLessThanEqualAndIsDeletedFalseOrderByEffectiveFromDescIdDesc(
        String companyCode, String employeeCode, LocalDate date);
    boolean existsByCompanyCodeAndEmployeeCodeAndEffectiveFrom(String companyCode, String employeeCode, LocalDate date);
}
