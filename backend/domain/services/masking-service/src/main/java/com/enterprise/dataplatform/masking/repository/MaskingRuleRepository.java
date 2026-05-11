package com.enterprise.dataplatform.masking.repository;

import com.enterprise.dataplatform.masking.domain.entity.MaskingRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MaskingRuleRepository extends JpaRepository<MaskingRule, Long> {
    
    List<MaskingRule> findByAssetId(String assetId);
    
    List<MaskingRule> findByAssetIdAndIsActiveTrue(String assetId);
    
    List<MaskingRule> findByIsActiveTrue();
    
    List<MaskingRule> findByColumnName(String columnName);
    
    List<MaskingRule> findByMaskingType(String maskingType);
    
    List<MaskingRule> findByPriorityGreaterThanEqual(Integer priority);
    
    long countByAssetId(String assetId);
}
