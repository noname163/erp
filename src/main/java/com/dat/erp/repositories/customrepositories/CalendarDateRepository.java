package com.dat.erp.repositories.customrepositories;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dat.erp.entities.CalendarDate;

@Repository
public interface CalendarDateRepository extends JpaRepository<CalendarDate, Long> {
    List<CalendarDate> findByCalendar_CodeAndIsDeletedFalseOrderByCalDateAsc(String calendarCode);

    List<CalendarDate> findByCalendar_CodeAndCalDateBetweenAndIsDeletedFalse(String calendarCode, LocalDate startDate,
            LocalDate endDate);
}
