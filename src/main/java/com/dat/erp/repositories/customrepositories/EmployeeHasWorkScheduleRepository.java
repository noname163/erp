package com.dat.erp.repositories.customrepositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.dat.erp.entities.EmployeeHasWorkSchedule;
import com.dat.erp.entities.WorkSchedule;

public interface EmployeeHasWorkScheduleRepository
        extends JpaRepository<EmployeeHasWorkSchedule, Long>,
        JpaSpecificationExecutor<EmployeeHasWorkSchedule> {

    @EntityGraph(value = "EmployeeHasWorkSchedule.full")
    List<EmployeeHasWorkSchedule> findByWorkSchedule(WorkSchedule workSchedule);

    // Use spec + entity graph (eager fetch as defined in your graph)
    @Override
    @EntityGraph(value = "EmployeeHasWorkSchedule.full")
    List<EmployeeHasWorkSchedule> findAll(Specification<EmployeeHasWorkSchedule> spec);

    @Override
    @EntityGraph(value = "EmployeeHasWorkSchedule.full")
    Page<EmployeeHasWorkSchedule> findAll(Specification<EmployeeHasWorkSchedule> spec, Pageable pageable);

    boolean existsByEmployee_CodeAndWorkSchedule_Code(String employeeCode, String scheduleCode);
}
