package com.enterprise.dataplatform.quality.repository;

import com.enterprise.dataplatform.quality.domain.entity.QualityRule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 质量规则Repository
 */
@Repository
public interface QualityRuleRepository extends JpaRepository<QualityRule, Long>, JpaSpecificationExecutor<QualityRule> {

    Optional<QualityRule> findByRuleCode(String ruleCode);

    boolean existsByRuleCode(String ruleCode);

    List<QualityRule> findByRuleType(String ruleType);

    List<QualityRule> findByStatus(String status);

    List<QualityRule> findByEnabled(Boolean enabled);

    @Query("SELECT qr FROM QualityRule qr WHERE " +
           "(:ruleType IS NULL OR qr.ruleType = :ruleType) AND " +
           "(:status IS NULL OR qr.status = :status) AND " +
           "(:keyword IS NULL OR qr.ruleName LIKE %:keyword% OR qr.ruleCode LIKE %:keyword%)")
    Page<QualityRule> searchRules(
            @Param("ruleType") String ruleType,
            @Param("status") String status,
            @Param("keyword") String keyword,
            Pageable pageable);

    @Query("SELECT qr FROM QualityRule qr WHERE qr.assetId = :assetId AND qr.enabled = true")
    List<QualityRule> findEnabledRulesByAssetId(@Param("assetId") String assetId);

    @Query("SELECT qr.ruleType, COUNT(qr) FROM QualityRule qr GROUP BY qr.ruleType")
    List<Object[]> countByRuleType();

    @Query("SELECT qr.status, COUNT(qr) FROM QualityRule qr GROUP BY qr.status")
    List<Object[]> countByStatus();
}
