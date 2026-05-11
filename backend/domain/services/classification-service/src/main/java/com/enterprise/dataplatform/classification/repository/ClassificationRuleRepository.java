package com.enterprise.dataplatform.classification.repository;

import com.enterprise.dataplatform.classification.domain.entity.ClassificationRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClassificationRuleRepository extends JpaRepository<ClassificationRule, Long> {
    
    List<ClassificationRule> findByIsActiveTrue();
    
    List<ClassificationRule> findByRuleType(String ruleType);
    
    List<ClassificationRule> findBySensitivityLevel(String sensitivityLevel);
    
    ClassificationRule findByRuleName(String ruleName);
    
    List<ClassificationRule> findByPriorityGreaterThanEqual(Integer priority);
}
