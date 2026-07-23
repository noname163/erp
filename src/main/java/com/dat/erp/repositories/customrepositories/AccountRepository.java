package com.dat.erp.repositories.customrepositories;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.dat.erp.entities.Account;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByEmail(String email);

    Optional<Account> findByCode(String code);

    List<Account> findAllByCodeIn(Collection<String> codes);

    @Query("""
            select a
            from Account a
            left join fetch a.role
            left join fetch a.userProfile
            where a.email = :email
            """)
    Optional<Account> findByEmailWithRoleAndUserProfile(@Param("email") String email);

    @Query("""
            select a
            from Account a
            left join fetch a.role
            left join fetch a.userProfile up
            left join fetch up.department
            where a.code = :code
            """)
    Optional<Account> findByCodeWithRoleAndUserProfileAndDepartment(@Param("code") String code);

    @Query("""
            select distinct a
            from Account a
            left join fetch a.userProfile up
            where a.code in :codes
            """)
    List<Account> findAllByCodeInWithUserProfile(@Param("codes") Collection<String> codes);
}
