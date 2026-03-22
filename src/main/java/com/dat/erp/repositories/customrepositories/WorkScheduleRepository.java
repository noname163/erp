package com.dat.erp.repositories.customrepositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dat.erp.entities.WorkSchedule;

@Repository
public interface WorkScheduleRepository extends JpaRepository<WorkSchedule, Long> {
    Optional<WorkSchedule> findByCodeAndIsDeletedFalse(String code);

    List<WorkSchedule> findByCompanyCodeAndIsDeletedFalseOrderByEffectiveFromDesc(String companyCode);
}
