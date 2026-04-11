package com.enterprise.dataplatform.governance.repository;

import com.enterprise.dataplatform.governance.domain.entity.GovernanceTask;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 治理任务Repository
 */
@Repository
public interface GovernanceTaskRepository extends JpaRepository<GovernanceTask, Long>, JpaSpecificationExecutor<GovernanceTask> {

    Optional<GovernanceTask> findByTaskCode(String taskCode);

    boolean existsByTaskCode(String taskCode);

    List<GovernanceTask> findByTaskStatus(String taskStatus);

    List<GovernanceTask> findByTaskType(String taskType);

    @Query("SELECT t FROM GovernanceTask t WHERE t.policy.id = :policyId")
    List<GovernanceTask> findByPolicyId(@Param("policyId") Long policyId);

    @Query("SELECT t FROM GovernanceTask t WHERE t.taskStatus = 'PENDING' ORDER BY t.executionOrder")
    List<GovernanceTask> findPendingTasks();

    Page<GovernanceTask> findByTaskStatusOrderByCreateTimeDesc(String taskStatus, Pageable pageable);

    @Query("SELECT t.taskStatus, COUNT(t) FROM GovernanceTask t GROUP BY t.taskStatus")
    List<Object[]> countByTaskStatus();
}
