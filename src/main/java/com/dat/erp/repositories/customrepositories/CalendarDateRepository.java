package com.dat.erp.repositories.customrepositories;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.dat.erp.entities.CalendarDate;

@Repository
public interface CalendarDateRepository extends JpaRepository<CalendarDate, Long> {
    @Query("""
            select cd
            from CalendarDate cd
            join fetch cd.calendar cc
            where cc.companyCode = :companyCode
              and cc.isDeleted = false
              and cd.isDeleted = false
              and cc.code = :calendarCode
              and cd.calDate >= :fromDate
              and cd.calDate <= :toDate
            order by cd.calDate asc
            """)
    List<CalendarDate> findByCalendarCodeAndCompanyCodeAndDateRange(
            @Param("calendarCode") String calendarCode,
            @Param("companyCode") String companyCode,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate);
}
