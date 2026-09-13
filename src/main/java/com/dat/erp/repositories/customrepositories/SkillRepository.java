package com.dat.erp.repositories.customrepositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;


import com.dat.erp.entities.Skill;


public interface SkillRepository extends JpaRepository<Skill, Long> {
    Optional<Skill> findByCode(String code);
}

