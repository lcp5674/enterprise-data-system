package com.enterprise.edams.analysis.repository;

import com.enterprise.edams.analysis.entity.AnalysisTask;
import com.enterprise.edams.analysis.entity.ExecutionMode;
import com.enterprise.edams.analysis.entity.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AnalysisTaskRepository extends JpaRepository<AnalysisTask, Long>, JpaSpecificationExecutor<AnalysisTask> {

    Optional<AnalysisTask> findByTaskCode(String taskCode);

    Page<AnalysisTask> findByStatus(TaskStatus status, Pageable pageable);

    Page<AnalysisTask> findByDatasourceId(Long datasourceId, Pageable pageable);

    Page<AnalysisTask> findByExecutor(String executor, Pageable pageable);

    List<AnalysisTask> findByExecutionModeAndStatus(ExecutionMode mode, TaskStatus status);

    @Query("SELECT t FROM AnalysisTask t WHERE t.executionMode = :mode AND t.status = :status")
    List<AnalysisTask> findScheduledTasksReadyToExecute(
            @Param("mode") ExecutionMode mode,
            @Param("status") TaskStatus status
    );

    @Modifying
    @Query("UPDATE AnalysisTask t SET t.status = :status, t.updatedAt = :now WHERE t.id = :id")
    void updateStatus(@Param("id") Long id, @Param("status") TaskStatus status, @Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE AnalysisTask t SET t.progress = :progress, t.updatedAt = :now WHERE t.id = :id")
    void updateProgress(@Param("id") Long id, @Param("progress") Integer progress, @Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE AnalysisTask t SET t.status = 'RUNNING', t.startTime = :startTime, t.updatedAt = :now WHERE t.id = :id")
    void markAsRunning(@Param("id") Long id, @Param("startTime") LocalDateTime startTime, @Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE AnalysisTask t SET t.status = :status, t.endTime = :endTime, t.executionTimeMs = :executionTime, t.updatedAt = :now WHERE t.id = :id")
    void markAsCompleted(
            @Param("id") Long id,
            @Param("status") TaskStatus status,
            @Param("endTime") LocalDateTime endTime,
            @Param("executionTime") Long executionTime,
            @Param("now") LocalDateTime now
    );

    @Query("SELECT COUNT(t) FROM AnalysisTask t WHERE t.status = :status")
    long countByStatus(@Param("status") TaskStatus status);

    @Query("SELECT t FROM AnalysisTask t WHERE t.executionMode = 'SCHEDULED' AND t.cronExpression IS NOT NULL")
    List<AnalysisTask> findAllScheduledTasks();

    default List<AnalysisTask> findTasksReadyToExecute() {
        return findScheduledTasksReadyToExecute(ExecutionMode.SCHEDULED, TaskStatus.PENDING);
    }
}
