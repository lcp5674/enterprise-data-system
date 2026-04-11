package com.edams.sla.repository;

import com.edams.sla.entity.SlaReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SlaReportRepository extends JpaRepository<SlaReport, Long>, JpaSpecificationExecutor<SlaReport> {
    
    List<SlaReport> findByAgreementId(Long agreementId);
    
    List<SlaReport> findByReportPeriod(String reportPeriod);
    
    List<SlaReport> findByAnalysisResult(String analysisResult);
    
    @Query("SELECT r FROM SlaReport r WHERE r.agreementId = :agreementId AND r.periodStart >= :startDate AND r.periodEnd <= :endDate ORDER BY r.periodEnd DESC")
    List<SlaReport> findReportsByAgreementAndDateRange(
            @Param("agreementId") Long agreementId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT r FROM SlaReport r WHERE r.generatedTime >= :startTime AND r.generatedTime <= :endTime")
    List<SlaReport> findReportsByGenerationTime(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);
}