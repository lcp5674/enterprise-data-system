package com.enterprise.dataplatform.quality.repository;

import com.enterprise.dataplatform.quality.domain.entity.QualityCheckResult;
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
 * 质量检查结果Repository
 */
@Repository
public interface QualityCheckResultRepository extends JpaRepository<QualityCheckResult, Long>, JpaSpecificationExecutor<QualityCheckResult> {

    List<QualityCheckResult> findByBatchNo(String batchNo);

    List<QualityCheckResult> findByTaskId(Long taskId);

    List<QualityCheckResult> findByRuleId(Long ruleId);

    List<QualityCheckResult> findByAssetId(String assetId);

    List<QualityCheckResult> findByCheckResult(String checkResult);

    @Query("SELECT r FROM QualityCheckResult r WHERE " +
           "(:ruleId IS NULL OR r.rule.id = :ruleId) AND " +
           "(:assetId IS NULL OR r.assetId = :assetId) AND " +
           "(:checkResult IS NULL OR r.checkResult = :checkResult) AND " +
           "(:startTime IS NULL OR r.checkTime >= :startTime) AND " +
           "(:endTime IS NULL OR r.checkTime <= :endTime)")
    Page<QualityCheckResult> searchResults(
            @Param("ruleId") Long ruleId,
            @Param("assetId") String assetId,
            @Param("checkResult") String checkResult,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            Pageable pageable);

    @Query("SELECT r.checkResult, COUNT(r) FROM QualityCheckResult r GROUP BY r.checkResult")
    List<Object[]> countByCheckResult();

    @Query("SELECT AVG(r.qualityScore) FROM QualityCheckResult r WHERE r.checkResult = 'PASS'")
    Double calculateAverageScore();

    @Query("SELECT SUM(r.failedRecords) FROM QualityCheckResult r WHERE r.checkResult = 'FAIL'")
    Long sumFailedRecords();

    @Query("SELECT r FROM QualityCheckResult r WHERE r.assetId = :assetId ORDER BY r.checkTime DESC")
    Page<QualityCheckResult> findLatestResultsByAssetId(@Param("assetId") String assetId, Pageable pageable);
}
