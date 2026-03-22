package com.dat.erp.repositories.customrepositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dat.erp.entities.WorkScheduleDetail;

@Repository
public interface WorkScheduleDetailRepository extends JpaRepository<WorkScheduleDetail, Long> {
    List<WorkScheduleDetail> findByWorkSchedule_CodeAndIsDeletedFalseOrderByDayOfWeekAsc(String workScheduleCode);
}
