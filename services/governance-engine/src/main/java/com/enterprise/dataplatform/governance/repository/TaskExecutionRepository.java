package com.enterprise.dataplatform.governance.repository;

import com.enterprise.dataplatform.governance.domain.entity.TaskExecution;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 任务执行记录Repository
 */
@Repository
public interface TaskExecutionRepository extends JpaRepository<TaskExecution, Long> {

    List<TaskExecution> findByBatchNo(String batchNo);

    List<TaskExecution> findByTaskId(Long taskId);

    @Query("SELECT e FROM TaskExecution e WHERE " +
           "(:taskId IS NULL OR e.task.id = :taskId) AND " +
           "(:executionStatus IS NULL OR e.executionStatus = :executionStatus) AND " +
           "(:startTime IS NULL OR e.startTime >= :startTime) AND " +
           "(:endTime IS NULL OR e.startTime <= :endTime)")
    Page<TaskExecution> searchExecutions(
            @Param("taskId") Long taskId,
            @Param("executionStatus") String executionStatus,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            Pageable pageable);

    @Query("SELECT e.executionStatus, COUNT(e) FROM TaskExecution e GROUP BY e.executionStatus")
    List<Object[]> countByExecutionStatus();
}
