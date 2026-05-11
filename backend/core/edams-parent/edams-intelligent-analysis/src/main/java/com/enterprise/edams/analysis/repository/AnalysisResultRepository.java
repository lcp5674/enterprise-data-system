package com.enterprise.edams.analysis.repository;

import com.enterprise.edams.analysis.entity.AnalysisResult;
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

@Repository
public interface AnalysisResultRepository extends JpaRepository<AnalysisResult, Long>, JpaSpecificationExecutor<AnalysisResult> {

    Page<AnalysisResult> findByTaskId(Long taskId, Pageable pageable);

    List<AnalysisResult> findByTaskIdAndSuccess(Long taskId, Boolean success);

    List<AnalysisResult> findByTaskIdAndBatchNumber(Long taskId, Integer batchNumber);

    @Query("SELECT r FROM AnalysisResult r WHERE r.taskId = :taskId AND r.success = true ORDER BY r.analyzedAt DESC")
    List<AnalysisResult> findSuccessfulResultsByTaskId(@Param("taskId") Long taskId);

    @Query("SELECT COUNT(r) FROM AnalysisResult r WHERE r.taskId = :taskId AND r.success = true")
    long countSuccessByTaskId(@Param("taskId") Long taskId);

    @Query("SELECT COUNT(r) FROM AnalysisResult r WHERE r.taskId = :taskId AND r.success = false")
    long countFailureByTaskId(@Param("taskId") Long taskId);

    @Query("SELECT r FROM AnalysisResult r WHERE r.taskId = :taskId AND r.subjectDomain = :subjectDomain")
    List<AnalysisResult> findByTaskIdAndSubjectDomain(@Param("taskId") Long taskId, @Param("subjectDomain") String subjectDomain);

    @Query("SELECT AVG(r.confidence) FROM AnalysisResult r WHERE r.taskId = :taskId AND r.success = true")
    Double calculateAverageConfidenceByTaskId(@Param("taskId") Long taskId);

    @Query("SELECT SUM(r.inputTokens + r.outputTokens) FROM AnalysisResult r WHERE r.taskId = :taskId")
    Long calculateTotalTokensByTaskId(@Param("taskId") Long taskId);

    @Modifying
    @Query("UPDATE AnalysisResult r SET r.metadataRegistered = true, r.registrationDetails = :details WHERE r.id = :id")
    void markMetadataRegistered(@Param("id") Long id, @Param("details") String details);

    @Modifying
    @Query("UPDATE AnalysisResult r SET r.lineageRegistered = true, r.registrationDetails = :details WHERE r.id = :id")
    void markLineageRegistered(@Param("id") Long id, @Param("details") String details);

    @Modifying
    @Query("UPDATE AnalysisResult r SET r.indicatorRegistered = true, r.registrationDetails = :details WHERE r.id = :id")
    void markIndicatorRegistered(@Param("id") Long id, @Param("details") String details);

    void deleteByTaskId(Long taskId);

    @Query("SELECT r FROM AnalysisResult r WHERE r.taskId = :taskId AND r.tableName IN :tableNames")
    List<AnalysisResult> findByTaskIdAndTableNames(@Param("taskId") Long taskId, @Param("tableNames") List<String> tableNames);
}
