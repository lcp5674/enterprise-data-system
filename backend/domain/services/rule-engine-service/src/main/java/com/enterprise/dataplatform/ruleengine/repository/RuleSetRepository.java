package com.enterprise.dataplatform.ruleengine.repository;

import com.enterprise.dataplatform.ruleengine.domain.entity.RuleSet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RuleSetRepository extends JpaRepository<RuleSet, Long>, JpaSpecificationExecutor<RuleSet> {

    List<RuleSet> findByCategory(String category);

    List<RuleSet> findByStatus(String status);

    boolean existsBySetCode(String setCode);
}
