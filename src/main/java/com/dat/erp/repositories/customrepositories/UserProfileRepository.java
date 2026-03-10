package com.dat.erp.repositories.customrepositories;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.dat.erp.entities.UserProfile;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long>, JpaSpecificationExecutor<UserProfile> {
    Optional<UserProfile> findByAccount_Code(String accountCode);

    Optional<UserProfile> findByCode(String code);

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

    @Override
    @EntityGraph(attributePaths = { "account", "department" })
    org.springframework.data.domain.Page<UserProfile> findAll(
            org.springframework.data.jpa.domain.Specification<UserProfile> spec,
            org.springframework.data.domain.Pageable pageable);
}
