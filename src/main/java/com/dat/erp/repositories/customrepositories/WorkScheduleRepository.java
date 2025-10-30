package com.dat.erp.repositories.customrepositories;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dat.erp.constants.ShiftType;
import com.dat.erp.entities.Company;
import com.dat.erp.entities.WorkSchedule;

@Repository
public interface WorkScheduleRepository extends JpaRepository<WorkSchedule, Long> {
    Optional<WorkSchedule> findByCode(String code);

    List<WorkSchedule> findByCodeIn(List<String> codes);

    Optional<WorkSchedule> findByShiftDateAndShiftTypeAndQuantityAndCompany(LocalDate shiftDate, ShiftType shiftType,
            Integer quantity, Company company);
}
