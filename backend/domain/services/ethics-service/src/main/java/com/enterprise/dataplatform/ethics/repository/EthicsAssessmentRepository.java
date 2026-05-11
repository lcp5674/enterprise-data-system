package com.enterprise.dataplatform.ethics.repository;

import com.enterprise.dataplatform.ethics.domain.entity.EthicsAssessment;
import com.enterprise.dataplatform.ethics.domain.enums.RiskLevel;
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

@Repository
public interface EthicsAssessmentRepository extends JpaRepository<EthicsAssessment, Long>, JpaSpecificationExecutor<EthicsAssessment> {

    Optional<EthicsAssessment> findByAssessmentCode(String assessmentCode);

    boolean existsByAssessmentCode(String assessmentCode);

    List<EthicsAssessment> findByAssetId(String assetId);

    Page<EthicsAssessment> findByAssetId(String assetId, Pageable pageable);

    List<EthicsAssessment> findByFrameworkId(Long frameworkId);

    List<EthicsAssessment> findByOverallRisk(RiskLevel riskLevel);

    @Query("SELECT ea FROM EthicsAssessment ea WHERE " +
           "(:assetId IS NULL OR ea.assetId = :assetId) AND " +
           "(:frameworkId IS NULL OR ea.frameworkId = :frameworkId) AND " +
           "(:riskLevel IS NULL OR ea.overallRisk = :riskLevel) AND " +
           "(:status IS NULL OR ea.status = :status) " +
           "ORDER BY ea.assessedAt DESC")
    Page<EthicsAssessment> searchAssessments(
            @Param("assetId") String assetId,
            @Param("frameworkId") Long frameworkId,
            @Param("riskLevel") RiskLevel riskLevel,
            @Param("status") String status,
            Pageable pageable);

    @Query("SELECT ea FROM EthicsAssessment ea WHERE ea.assetId = :assetId ORDER BY ea.assessedAt DESC")
    Page<EthicsAssessment> findAssetAssessmentHistory(@Param("assetId") String assetId, Pageable pageable);

    @Query("SELECT ea FROM EthicsAssessment ea WHERE ea.assessedAt BETWEEN :startDate AND :endDate ORDER BY ea.assessedAt DESC")
    List<EthicsAssessment> findByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(ea) FROM EthicsAssessment ea WHERE ea.overallRisk = :riskLevel")
    long countByRiskLevel(@Param("riskLevel") RiskLevel riskLevel);

    @Query("SELECT AVG(ea.overallScore) FROM EthicsAssessment ea WHERE ea.assetId = :assetId")
    Double calculateAverageScoreByAsset(@Param("assetId") String assetId);
}
