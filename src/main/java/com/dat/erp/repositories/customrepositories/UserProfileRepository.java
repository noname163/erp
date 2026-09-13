package com.dat.erp.repositories.customrepositories;

import java.util.Collection;
import java.util.Optional;
import java.util.List;

import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import com.dat.erp.entities.UserProfile;

import jakarta.persistence.LockModeType;


public interface UserProfileRepository extends JpaRepository<UserProfile, Long>, JpaSpecificationExecutor<UserProfile> {
    Optional<UserProfile> findByAccount_Code(String accountCode);

    Optional<UserProfile> findByCode(String code);

    Optional<UserProfile> findByCodeAndIsDeletedFalse(String code);

    @Query("""
            select distinct up
            from UserProfile up
            left join fetch up.account acc
            where up.code in :codes
              and up.isDeleted = false
            """)
    List<UserProfile> findAllByCodeInAndIsDeletedFalseWithAccount(@Param("codes") Collection<String> codes);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select up
            from UserProfile up
            where up.code = :code
              and up.isDeleted = false
            """)
    Optional<UserProfile> findByCodeAndIsDeletedFalseForUpdate(@Param("code") String code);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select up
            from UserProfile up
            where up.code in :codes
              and up.isDeleted = false
            """)
    List<UserProfile> findAllByCodeInAndIsDeletedFalseForUpdate(@Param("codes") Collection<String> codes);

    @Query("""
            select up
            from UserProfile up
            where up.companyCode = :companyCode
              and up.isDeleted = false
              and up.isActive = true
              and (coalesce(trim(:firstName), '') = ''
                   or lower(up.firstName) like lower(concat('%', trim(coalesce(:firstName, '')), '%')))
            """)
    Page<UserProfile> findOptionsByFilters(@Param("companyCode") String companyCode, @Param("firstName") String firstName,
            Pageable pageable);

    @Query("""
            select up.code
            from UserProfile up
            where up.companyCode = :companyCode
              and up.isDeleted = false
              and up.isActive = true
            order by up.code asc
            """)
    List<String> findActiveCodesByCompanyCode(@Param("companyCode") String companyCode);

    @Override
    @EntityGraph(attributePaths = { "account", "department" })
    org.springframework.data.domain.Page<UserProfile> findAll(
            org.springframework.data.jpa.domain.Specification<UserProfile> spec,
            org.springframework.data.domain.Pageable pageable);
}
