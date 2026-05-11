package com.enterprise.edams.analysis.repository;

import com.enterprise.edams.analysis.entity.LocalModelConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LocalModelConfigRepository extends JpaRepository<LocalModelConfig, Long> {

    Optional<LocalModelConfig> findByConfigCode(String configCode);

    Optional<LocalModelConfig> findByIdAndEnabledTrue(Long id);

    List<LocalModelConfig> findByEnabledTrue();

    List<LocalModelConfig> findByModelType(String modelType);

    Optional<LocalModelConfig> findByIsDefaultTrue();

    boolean existsByConfigCode(String configCode);

    @Modifying
    @Query("UPDATE LocalModelConfig c SET c.usageCount = c.usageCount + 1 WHERE c.id = :id")
    void incrementUsageCount(@Param("id") Long id);

    @Modifying
    @Query("UPDATE LocalModelConfig c SET c.successCount = c.successCount + 1 WHERE c.id = :id")
    void incrementSuccessCount(@Param("id") Long id);

    @Modifying
    @Query("UPDATE LocalModelConfig c SET c.failureCount = c.failureCount + 1 WHERE c.id = :id")
    void incrementFailureCount(@Param("id") Long id);
}
