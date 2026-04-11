package com.enterprise.dataplatform.standard.repository;

import com.enterprise.dataplatform.standard.domain.entity.StandardExecution;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 标准执行记录Repository
 */
@Repository
public interface StandardExecutionRepository extends JpaRepository<StandardExecution, Long>, JpaSpecificationExecutor<StandardExecution> {

    /**
     * 根据批次号查询
     */
    List<StandardExecution> findByBatchNo(String batchNo);

    /**
     * 根据标准映射ID查询
     */
    List<StandardExecution> findByMappingId(Long mappingId);

    /**
     * 根据数据资产ID查询
     */
    List<StandardExecution> findByAssetId(String assetId);

    /**
     * 根据执行状态查询
     */
    List<StandardExecution> findByExecutionStatus(String executionStatus);

    /**
     * 根据执行类型查询
     */
    List<StandardExecution> findByExecutionType(String executionType);

    /**
     * 根据时间范围查询
     */
    List<StandardExecution> findByCreateTimeBetween(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 分页查询执行记录
     */
    @Query("SELECT se FROM StandardExecution se WHERE " +
           "(:mappingId IS NULL OR se.mapping.id = :mappingId) AND " +
           "(:assetId IS NULL OR se.assetId = :assetId) AND " +
           "(:executionStatus IS NULL OR se.executionStatus = :executionStatus)")
    Page<StandardExecution> searchExecutions(
            @Param("mappingId") Long mappingId,
            @Param("assetId") String assetId,
            @Param("executionStatus") String executionStatus,
            Pageable pageable);

    /**
     * 统计各执行状态的数量
     */
    @Query("SELECT se.executionStatus, COUNT(se) FROM StandardExecution se GROUP BY se.executionStatus")
    List<Object[]> countByExecutionStatus();

    /**
     * 统计成功率
     */
    @Query("SELECT (SUM(CASE WHEN se.resultStatus = 'SUCCESS' THEN 1 ELSE 0 END) * 100.0 / COUNT(se)) FROM StandardExecution se WHERE se.resultStatus IS NOT NULL")
    Double calculateSuccessRate();

    /**
     * 查询最近的执行记录
     */
    Page<StandardExecution> findAllByOrderByCreateTimeDesc(Pageable pageable);
}
