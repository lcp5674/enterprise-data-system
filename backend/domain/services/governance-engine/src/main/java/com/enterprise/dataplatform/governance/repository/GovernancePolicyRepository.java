package com.enterprise.dataplatform.governance.repository;

import com.enterprise.dataplatform.governance.domain.entity.GovernancePolicy;
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
 * 治理策略Repository
 */
@Repository
public interface GovernancePolicyRepository extends JpaRepository<GovernancePolicy, Long>, JpaSpecificationExecutor<GovernancePolicy> {

    Optional<GovernancePolicy> findByPolicyCode(String policyCode);

    boolean existsByPolicyCode(String policyCode);

    List<GovernancePolicy> findByPolicyType(String policyType);

    List<GovernancePolicy> findByStatus(String status);

    List<GovernancePolicy> findByEnabled(Boolean enabled);

    @Query("SELECT gp FROM GovernancePolicy gp WHERE " +
           "(:policyType IS NULL OR gp.policyType = :policyType) AND " +
           "(:status IS NULL OR gp.status = :status) AND " +
           "(:keyword IS NULL OR gp.policyName LIKE %:keyword% OR gp.policyCode LIKE %:keyword%)")
    Page<GovernancePolicy> searchPolicies(
            @Param("policyType") String policyType,
            @Param("status") String status,
            @Param("keyword") String keyword,
            Pageable pageable);

    @Query("SELECT gp FROM GovernancePolicy gp WHERE gp.enabled = true ORDER BY gp.priority")
    List<GovernancePolicy> findEnabledPolicies();
}
