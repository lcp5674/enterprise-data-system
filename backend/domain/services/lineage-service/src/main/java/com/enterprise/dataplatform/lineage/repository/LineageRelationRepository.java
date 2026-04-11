package com.enterprise.dataplatform.lineage.repository;

import com.enterprise.dataplatform.lineage.domain.entity.LineageRelation;
import com.enterprise.dataplatform.lineage.domain.enums.LineageType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for LineageRelation entity
 */
@Repository
public interface LineageRelationRepository extends JpaRepository<LineageRelation, String> {

    /**
     * Find by source asset ID
     */
    List<LineageRelation> findBySourceAssetIdAndIsDeletedFalse(String sourceAssetId);

    /**
     * Find by target asset ID
     */
    List<LineageRelation> findByTargetAssetIdAndIsDeletedFalse(String targetAssetId);

    /**
     * Find all relations for an asset (both source and target)
     */
    @Query("SELECT lr FROM LineageRelation lr WHERE " +
           "(lr.sourceAssetId = :assetId OR lr.targetAssetId = :assetId) " +
           "AND lr.isDeleted = false AND lr.isActive = true")
    List<LineageRelation> findByAssetId(@Param("assetId") String assetId);

    /**
     * Find by source and target
     */
    Optional<LineageRelation> findBySourceAssetIdAndTargetAssetIdAndIsDeletedFalse(
            String sourceAssetId, String targetAssetId);

    /**
     * Find by lineage type
     */
    List<LineageRelation> findByLineageTypeAndIsDeletedFalse(LineageType lineageType);

    /**
     * Count relations for an asset
     */
    @Query("SELECT COUNT(lr) FROM LineageRelation lr WHERE " +
           "(lr.sourceAssetId = :assetId OR lr.targetAssetId = :assetId) " +
           "AND lr.isDeleted = false")
    long countByAssetId(@Param("assetId") String assetId);

    /**
     * Count upstream relations
     */
    long countByTargetAssetIdAndIsDeletedFalse(String targetAssetId);

    /**
     * Count downstream relations
     */
    long countBySourceAssetIdAndIsDeletedFalse(String sourceAssetId);

    /**
     * Find unverified relations
     */
    @Query("SELECT lr FROM LineageRelation lr WHERE lr.isVerified = false AND lr.isDeleted = false")
    Page<LineageRelation> findUnverified(Pageable pageable);

    /**
     * Find relations by job ID
     */
    List<LineageRelation> findByJobIdAndIsDeletedFalse(String jobId);

    /**
     * Find relations by task name
     */
    List<LineageRelation> findByTaskNameAndIsDeletedFalse(String taskName);
}
