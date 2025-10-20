package com.dat.erp.repositories.customrepositories;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.dat.erp.entities.EmployeeHasWorkSchedule;
import com.dat.erp.entities.WorkSchedule;

public interface EmployeeHasWorkScheduleRepository extends JpaRepository<EmployeeHasWorkSchedule, Long> {

    @EntityGraph(value = "EmployeeHasWorkSchedule.full")
    List<EmployeeHasWorkSchedule> findByWorkSchedule(WorkSchedule workSchedule);

}
