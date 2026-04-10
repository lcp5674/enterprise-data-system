package com.enterprise.dataplatform.standard.repository;

import com.enterprise.dataplatform.standard.domain.entity.ComplianceCheck;
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
 * 合规检查Repository
 */
@Repository
public interface ComplianceCheckRepository extends JpaRepository<ComplianceCheck, Long>, JpaSpecificationExecutor<ComplianceCheck> {

    /**
     * 根据批次号查询
     */
    List<ComplianceCheck> findByBatchNo(String batchNo);

    /**
     * 根据数据标准ID查询
     */
    List<ComplianceCheck> findByDataStandardId(Long standardId);

    /**
     * 根据数据资产ID查询
     */
    List<ComplianceCheck> findByAssetId(String assetId);

    /**
     * 根据检查结果查询
     */
    List<ComplianceCheck> findByCheckResult(String checkResult);

    /**
     * 根据检查类型查询
     */
    List<ComplianceCheck> findByCheckType(String checkType);

    /**
     * 根据时间范围查询
     */
    List<ComplianceCheck> findByCheckTimeBetween(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 分页查询检查记录
     */
    @Query("SELECT cc FROM ComplianceCheck cc WHERE " +
           "(:standardId IS NULL OR cc.dataStandard.id = :standardId) AND " +
           "(:assetId IS NULL OR cc.assetId = :assetId) AND " +
           "(:checkResult IS NULL OR cc.checkResult = :checkResult) AND " +
           "(:startTime IS NULL OR cc.checkTime >= :startTime) AND " +
           "(:endTime IS NULL OR cc.checkTime <= :endTime)")
    Page<ComplianceCheck> searchChecks(
            @Param("standardId") Long standardId,
            @Param("assetId") String assetId,
            @Param("checkResult") String checkResult,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            Pageable pageable);

    /**
     * 统计各检查结果的数量
     */
    @Query("SELECT cc.checkResult, COUNT(cc) FROM ComplianceCheck cc GROUP BY cc.checkResult")
    List<Object[]> countByCheckResult();

    /**
     * 计算平均合规率
     */
    @Query("SELECT AVG(cc.complianceRate) FROM ComplianceCheck cc WHERE cc.checkResult = 'PASS'")
    Double calculateAverageComplianceRate();

    /**
     * 根据数据标准ID和检查结果查询最近检查
     */
    @Query("SELECT cc FROM ComplianceCheck cc WHERE cc.dataStandard.id = :standardId ORDER BY cc.checkTime DESC")
    Page<ComplianceCheck> findRecentChecksByStandardId(@Param("standardId") Long standardId, Pageable pageable);

    /**
     * 查询失败的检查记录
     */
    List<ComplianceCheck> findByCheckResultIn(List<String> checkResults);

    /**
     * 统计违规总数
     */
    @Query("SELECT SUM(cc.violationCount) FROM ComplianceCheck cc WHERE cc.checkResult = 'FAIL'")
    Long sumViolationCount();
}
