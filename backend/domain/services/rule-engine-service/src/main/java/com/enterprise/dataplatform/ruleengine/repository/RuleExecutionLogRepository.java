package com.enterprise.dataplatform.ruleengine.repository;

import com.enterprise.dataplatform.ruleengine.domain.entity.RuleExecutionLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RuleExecutionLogRepository extends JpaRepository<RuleExecutionLog, Long> {

    List<RuleExecutionLog> findByRuleCode(String ruleCode);

    List<RuleExecutionLog> findByAssetId(String assetId);

    List<RuleExecutionLog> findByCategory(String category);

    @Query("SELECT l FROM RuleExecutionLog l WHERE l.createdTime BETWEEN :start AND :end ORDER BY l.createdTime DESC")
    List<RuleExecutionLog> findByTimeRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT l FROM RuleExecutionLog l WHERE l.createdTime BETWEEN :start AND :end ORDER BY l.createdTime DESC")
    Page<RuleExecutionLog> findByTimeRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, Pageable pageable);

    long countByStatus(String status);

    long countByCategoryAndStatus(String category, String status);

    @Query("SELECT AVG(l.executionTimeMs) FROM RuleExecutionLog l WHERE l.status = 'SUCCESS'")
    Double getAverageExecutionTime();
}
