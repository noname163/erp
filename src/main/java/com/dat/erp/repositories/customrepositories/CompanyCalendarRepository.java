package com.dat.erp.repositories.customrepositories;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.dat.erp.entities.CompanyCalendar;

@Repository
public interface CompanyCalendarRepository extends JpaRepository<CompanyCalendar, Long> {
    @Query("""
            select cc
            from CompanyCalendar cc
            where cc.companyCode = :companyCode
              and cc.isDeleted = false
              and (coalesce(trim(:name), '') = ''
                   or lower(cc.name) like lower(concat('%', trim(coalesce(:name, '')), '%')))
              and (coalesce(trim(:region), '') = ''
                   or lower(cc.region) like lower(concat('%', trim(coalesce(:region, '')), '%')))
              and (coalesce(trim(:timeZone), '') = ''
                   or lower(cc.timeZone) = lower(trim(coalesce(:timeZone, ''))))
            """)
    Page<CompanyCalendar> searchByConditions(
            @Param("companyCode") String companyCode,
            @Param("name") String name,
            @Param("region") String region,
            @Param("timeZone") String timeZone,
            Pageable pageable);

    Optional<CompanyCalendar> findByCodeAndCompanyCodeAndIsDeletedFalse(String code, String companyCode);
}
