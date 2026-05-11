package com.enterprise.dataplatform.ethics.repository;

import com.enterprise.dataplatform.ethics.domain.entity.SocialImpactReport;
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
public interface SocialImpactReportRepository extends JpaRepository<SocialImpactReport, Long>, JpaSpecificationExecutor<SocialImpactReport> {

    Optional<SocialImpactReport> findByReportCode(String reportCode);

    boolean existsByReportCode(String reportCode);

    List<SocialImpactReport> findByAssetId(String assetId);

    Page<SocialImpactReport> findByAssetId(String assetId, Pageable pageable);

    @Query("SELECT sr FROM SocialImpactReport sr WHERE " +
           "(:assetId IS NULL OR sr.assetId = :assetId) AND " +
           "(:riskLevel IS NULL OR sr.overallRisk = :riskLevel) AND " +
           "(:status IS NULL OR sr.status = :status) " +
           "ORDER BY sr.reportDate DESC")
    Page<SocialImpactReport> searchReports(
            @Param("assetId") String assetId,
            @Param("riskLevel") RiskLevel riskLevel,
            @Param("status") String status,
            Pageable pageable);

    @Query("SELECT sr FROM SocialImpactReport sr WHERE sr.reportDate BETWEEN :startDate AND :endDate ORDER BY sr.reportDate DESC")
    List<SocialImpactReport> findByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT sr FROM SocialImpactReport sr WHERE sr.approvalStatus = :approvalStatus ORDER BY sr.createTime DESC")
    List<SocialImpactReport> findByApprovalStatus(@Param("approvalStatus") String approvalStatus);

    @Query("SELECT COUNT(sr) FROM SocialImpactReport sr WHERE sr.overallRisk = :riskLevel")
    long countByRiskLevel(@Param("riskLevel") RiskLevel riskLevel);

    @Query("SELECT AVG(sr.overallImpactScore) FROM SocialImpactReport sr WHERE sr.assetId = :assetId")
    Double calculateAverageImpactScoreByAsset(@Param("assetId") String assetId);
}
