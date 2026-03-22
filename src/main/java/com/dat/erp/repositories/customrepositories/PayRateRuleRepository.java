package com.dat.erp.repositories.customrepositories;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dat.erp.entities.PayRateRule;

@Repository
public interface PayRateRuleRepository extends JpaRepository<PayRateRule, Long> {
    List<PayRateRule> findByPolicy_CodeAndIsDeletedFalseOrderByPriorityAscIdAsc(String policyCode);

    List<PayRateRule> findByPolicy_CodeInAndIsDeletedFalseOrderByPriorityAsc(Collection<String> policyCodes);
}
