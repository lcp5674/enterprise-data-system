package com.enterprise.dataplatform.ethics.repository;

import com.enterprise.dataplatform.ethics.domain.entity.EthicsReview;
import com.enterprise.dataplatform.ethics.domain.enums.ReviewStatus;
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
public interface EthicsReviewRepository extends JpaRepository<EthicsReview, Long>, JpaSpecificationExecutor<EthicsReview> {

    Optional<EthicsReview> findByReviewCode(String reviewCode);

    boolean existsByReviewCode(String reviewCode);

    List<EthicsReview> findByAssetId(String assetId);

    Page<EthicsReview> findByAssetId(String assetId, Pageable pageable);

    List<EthicsReview> findByStatus(ReviewStatus status);

    List<EthicsReview> findByReviewer(String reviewer);

    List<EthicsReview> findByRequester(String requester);

    @Query("SELECT er FROM EthicsReview er WHERE " +
           "(:status IS NULL OR er.status = :status) AND " +
           "(:reviewType IS NULL OR er.reviewType = :reviewType) AND " +
           "(:priority IS NULL OR er.priority = :priority) AND " +
           "(:assetId IS NULL OR er.assetId = :assetId) " +
           "ORDER BY er.createTime DESC")
    Page<EthicsReview> searchReviews(
            @Param("status") ReviewStatus status,
            @Param("reviewType") String reviewType,
            @Param("priority") String priority,
            @Param("assetId") String assetId,
            Pageable pageable);

    @Query("SELECT er FROM EthicsReview er WHERE er.status IN :statuses ORDER BY er.createTime DESC")
    List<EthicsReview> findByStatusIn(@Param("statuses") List<ReviewStatus> statuses);

    @Query("SELECT er FROM EthicsReview er WHERE er.expiryDate < :date AND er.status = :status")
    List<EthicsReview> findExpiredReviews(
            @Param("date") LocalDateTime date,
            @Param("status") ReviewStatus status);

    @Query("SELECT COUNT(er) FROM EthicsReview er WHERE er.status = :status")
    long countByStatus(@Param("status") ReviewStatus status);

    @Query("SELECT er FROM EthicsReview er WHERE er.reviewer = :reviewer AND er.status IN :pendingStatuses")
    List<EthicsReview> findPendingReviewsByReviewer(
            @Param("reviewer") String reviewer,
            @Param("pendingStatuses") List<ReviewStatus> pendingStatuses);
}
