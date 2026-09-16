package com.dat.erp.repositories.customrepositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import com.dat.erp.entities.UserSkill;
import com.dat.erp.repositories.projections.EmployeeSkillRow;


public interface UserSkillRepository extends JpaRepository<UserSkill, Long> {

    @Query("""
            select us.userProfile.id as userProfileId, s.id as skillId, s.name as skillName
            from UserSkill us
            join us.skill s
            where us.isDeleted = false
              and s.isDeleted = false
              and us.userProfile.id in :userProfileIds
            """)
    List<EmployeeSkillRow> findSkillRowsByUserProfileIds(@Param("userProfileIds") List<Long> userProfileIds);
}

