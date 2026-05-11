package com.enterprise.dataplatform.masking.repository;

import com.enterprise.dataplatform.masking.domain.entity.MaskingConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MaskingConfigRepository extends JpaRepository<MaskingConfig, Long> {
    
    List<MaskingConfig> findByIsActiveTrue();
    
    List<MaskingConfig> findByMaskingType(String maskingType);
    
    MaskingConfig findByConfigName(String configName);
}
