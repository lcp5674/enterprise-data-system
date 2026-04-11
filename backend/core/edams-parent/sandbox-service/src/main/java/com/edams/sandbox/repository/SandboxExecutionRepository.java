package com.edams.sandbox.repository;

import com.edams.sandbox.entity.SandboxExecution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SandboxExecutionRepository extends JpaRepository<SandboxExecution, Long>, JpaSpecificationExecutor<SandboxExecution> {
    
    List<SandboxExecution> findBySandboxId(Long sandboxId);
    
    List<SandboxExecution> findByExecutedBy(Long executedBy);
    
    List<SandboxExecution> findByStatus(String status);
    
    List<SandboxExecution> findByExecutionType(String executionType);
    
    @Query("SELECT e FROM SandboxExecution e WHERE e.sandboxId = :sandboxId AND e.status = 'SUCCESS' ORDER BY e.endTime DESC")
    List<SandboxExecution> findSuccessfulExecutionsBySandbox(@Param("sandboxId") Long sandboxId);
    
    @Query("SELECT e FROM SandboxExecution e WHERE e.createdTime >= :startTime AND e.createdTime <= :endTime")
    List<SandboxExecution> findExecutionsByTimeRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
}