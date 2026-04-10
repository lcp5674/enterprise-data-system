package com.enterprise.dataplatform.quality.repository;

import com.enterprise.dataplatform.quality.domain.entity.QualityCheckTask;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 质量检查任务Repository
 */
@Repository
public interface QualityCheckTaskRepository extends JpaRepository<QualityCheckTask, Long>, JpaSpecificationExecutor<QualityCheckTask> {

    Optional<QualityCheckTask> findByTaskCode(String taskCode);

    boolean existsByTaskCode(String taskCode);

    List<QualityCheckTask> findByRuleId(Long ruleId);

    List<QualityCheckTask> findByAssetId(String assetId);

    List<QualityCheckTask> findByTaskStatus(String taskStatus);

    @Query("SELECT t FROM QualityCheckTask t WHERE t.scheduleType = :scheduleType AND t.taskStatus = 'RUNNING'")
    List<QualityCheckTask> findScheduledTasks(@Param("scheduleType") String scheduleType);

    @Query("SELECT t FROM QualityCheckTask t WHERE t.nextExecutionTime <= :time AND t.taskStatus = 'PENDING'")
    List<QualityCheckTask> findTasksToExecute(@Param("time") LocalDateTime time);

    Page<QualityCheckTask> findByRuleIdOrderByCreateTimeDesc(Long ruleId, Pageable pageable);

    @Query("SELECT t.taskStatus, COUNT(t) FROM QualityCheckTask t GROUP BY t.taskStatus")
    List<Object[]> countByTaskStatus();
}
