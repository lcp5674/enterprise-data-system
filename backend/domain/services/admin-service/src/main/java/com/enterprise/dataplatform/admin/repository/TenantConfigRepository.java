package com.enterprise.dataplatform.admin.repository;

import com.enterprise.dataplatform.admin.entity.TenantConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Tenant Config Repository
 */
@Repository
public interface TenantConfigRepository extends JpaRepository<TenantConfig, Long> {

    Optional<TenantConfig> findByTenantId(String tenantId);

    List<TenantConfig> findByStatus(TenantConfig.TenantStatus status);

    boolean existsByTenantId(String tenantId);
}
