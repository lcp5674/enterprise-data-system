package com.enterprise.dataplatform.ruleengine.repository;

import com.enterprise.dataplatform.ruleengine.domain.entity.RuleDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RuleDefinitionRepository extends JpaRepository<RuleDefinition, Long>, JpaSpecificationExecutor<RuleDefinition> {

    List<RuleDefinition> findByCategory(String category);

    List<RuleDefinition> findByStatus(String status);

    List<RuleDefinition> findByCategoryAndStatus(String category, String status);

    boolean existsByRuleCode(String ruleCode);

    @Modifying
    @Query("UPDATE RuleDefinition r SET r.status = :status WHERE r.id = :id")
    void updateStatus(@Param("id") Long id, @Param("status") String status);

    long countByCategory(String category);

    long countByStatus(String status);
}
