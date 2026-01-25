package com.dat.erp.repositories.customrepositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dat.erp.entities.DailyWork;

@Repository
public interface DailyWorkRepository extends JpaRepository<DailyWork, Long> {
    boolean existsByUserProfile_CodeAndWorkingDateAndIsDeletedFalse(String userProfileCode,
            java.time.LocalDate workingDate);
}

