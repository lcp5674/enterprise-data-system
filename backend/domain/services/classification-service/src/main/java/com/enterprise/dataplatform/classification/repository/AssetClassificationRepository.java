package com.enterprise.dataplatform.classification.repository;

import com.enterprise.dataplatform.classification.domain.entity.AssetClassification;
import com.enterprise.dataplatform.classification.domain.enums.SensitivityLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssetClassificationRepository extends JpaRepository<AssetClassification, Long> {
    
    Optional<AssetClassification> findByAssetId(String assetId);
    
    List<AssetClassification> findBySensitivityLevel(SensitivityLevel level);
    
    List<AssetClassification> findByClassificationStatus(String status);
    
    List<AssetClassification> findByApprovalStatus(String approvalStatus);
    
    List<AssetClassification> findByManualOverride(Boolean manualOverride);
    
    List<AssetClassification> findByClassificationMethod(String method);
    
    List<AssetClassification> findByAssetType(String assetType);
    
    long countBySensitivityLevel(SensitivityLevel level);
    
    long countByApprovalStatus(String approvalStatus);
}
