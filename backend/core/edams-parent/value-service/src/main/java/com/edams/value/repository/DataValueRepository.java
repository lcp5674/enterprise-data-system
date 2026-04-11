package com.edams.value.repository;

import com.edams.value.entity.DataValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DataValueRepository extends JpaRepository<DataValue, Long>, JpaSpecificationExecutor<DataValue> {
    
    List<DataValue> findByAssetId(Long assetId);
    
    List<DataValue> findByAssetType(String assetType);
    
    List<DataValue> findByValueCategory(String valueCategory);
    
    List<DataValue> findByAssessorId(Long assessorId);
    
    List<DataValue> findByStatus(String status);
    
    @Query("SELECT v FROM DataValue v WHERE v.assetId = :assetId AND v.assessmentDate = (SELECT MAX(v2.assessmentDate) FROM DataValue v2 WHERE v2.assetId = :assetId)")
    DataValue findLatestValueByAsset(@Param("assetId") Long assetId);
    
    @Query("SELECT v FROM DataValue v WHERE v.assessmentDate >= :startDate AND v.assessmentDate <= :endDate")
    List<DataValue> findValuesByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT v FROM DataValue v WHERE v.valueScore >= :minScore AND v.valueScore <= :maxScore")
    List<DataValue> findValuesByScoreRange(
            @Param("minScore") Double minScore,
            @Param("maxScore") Double maxScore);
}