package com.enterprise.dataplatform.governance.repository;

import com.enterprise.dataplatform.governance.domain.entity.AIRecommendation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * AI推荐Repository
 */
@Repository
public interface AIRecommendationRepository extends JpaRepository<AIRecommendation, Long> {

    Optional<AIRecommendation> findByRecommendationCode(String recommendationCode);

    List<AIRecommendation> findByRecommendationType(String recommendationType);

    List<AIRecommendation> findByStatus(String status);

    @Query("SELECT r FROM AIRecommendation r WHERE r.status = 'PENDING' ORDER BY r.recommendationTime DESC")
    Page<AIRecommendation> findPendingRecommendations(Pageable pageable);

    @Query("SELECT r FROM AIRecommendation r WHERE r.assetId = :assetId ORDER BY r.recommendationTime DESC")
    List<AIRecommendation> findByAssetId(@Param("assetId") String assetId);

    @Query("SELECT r FROM AIRecommendation r WHERE " +
           "(:recommendationType IS NULL OR r.recommendationType = :recommendationType) AND " +
           "(:status IS NULL OR r.status = :status) AND " +
           "(:startTime IS NULL OR r.recommendationTime >= :startTime) AND " +
           "(:endTime IS NULL OR r.recommendationTime <= :endTime)")
    Page<AIRecommendation> searchRecommendations(
            @Param("recommendationType") String recommendationType,
            @Param("status") String status,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            Pageable pageable);

    @Query("SELECT r.status, COUNT(r) FROM AIRecommendation r GROUP BY r.status")
    List<Object[]> countByStatus();
}
