package com.enterprise.dataplatform.admin.service;

import com.enterprise.dataplatform.admin.dto.request.TenantRequest;
import com.enterprise.dataplatform.admin.dto.response.TenantResponse;
import com.enterprise.dataplatform.admin.entity.TenantConfig;
import com.enterprise.dataplatform.admin.repository.TenantConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Tenant Service
 * 租户管理服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TenantService {

    private final TenantConfigRepository tenantConfigRepository;

    /**
     * Create a new tenant
     */
    @Transactional
    public TenantResponse createTenant(TenantRequest request) {
        if (tenantConfigRepository.existsByTenantId(request.getTenantId())) {
            throw new IllegalArgumentException("Tenant ID already exists: " + request.getTenantId());
        }

        TenantConfig tenant = new TenantConfig();
        tenant.setTenantId(request.getTenantId());
        tenant.setTenantName(request.getTenantName());
        tenant.setDescription(request.getDescription());
        tenant.setMaxUsers(request.getMaxUsers() != null ? request.getMaxUsers() : 100);
        tenant.setMaxStorage(request.getMaxStorage() != null ? request.getMaxStorage() : 1073741824L);
        tenant.setFeatures(request.getFeatures());
        tenant.setStatus(TenantConfig.TenantStatus.ACTIVE);

        if (request.getExpiredAt() != null) {
            tenant.setExpiredAt(LocalDateTime.parse(request.getExpiredAt()));
        }

        TenantConfig saved = tenantConfigRepository.save(tenant);
        return toTenantResponse(saved);
    }

    /**
     * Update tenant configuration
     */
    @Transactional
    public TenantResponse updateTenant(String tenantId, TenantRequest request) {
        TenantConfig tenant = tenantConfigRepository.findByTenantId(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + tenantId));

        if (request.getTenantName() != null) {
            tenant.setTenantName(request.getTenantName());
        }
        if (request.getDescription() != null) {
            tenant.setDescription(request.getDescription());
        }
        if (request.getMaxUsers() != null) {
            tenant.setMaxUsers(request.getMaxUsers());
        }
        if (request.getMaxStorage() != null) {
            tenant.setMaxStorage(request.getMaxStorage());
        }
        if (request.getFeatures() != null) {
            tenant.setFeatures(request.getFeatures());
        }
        if (request.getExpiredAt() != null) {
            tenant.setExpiredAt(LocalDateTime.parse(request.getExpiredAt()));
        }

        TenantConfig saved = tenantConfigRepository.save(tenant);
        return toTenantResponse(saved);
    }

    /**
     * Suspend a tenant
     */
    @Transactional
    public TenantResponse suspendTenant(String tenantId) {
        TenantConfig tenant = tenantConfigRepository.findByTenantId(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + tenantId));

        tenant.setStatus(TenantConfig.TenantStatus.SUSPENDED);
        TenantConfig saved = tenantConfigRepository.save(tenant);
        return toTenantResponse(saved);
    }

    /**
     * Get tenant by ID
     */
    public TenantResponse getTenant(String tenantId) {
        TenantConfig tenant = tenantConfigRepository.findByTenantId(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + tenantId));
        return toTenantResponse(tenant);
    }

    /**
     * List all tenants
     */
    public List<TenantResponse> listTenants(String status) {
        List<TenantConfig> tenants;
        if (status != null) {
            tenants = tenantConfigRepository.findByStatus(TenantConfig.TenantStatus.valueOf(status.toUpperCase()));
        } else {
            tenants = tenantConfigRepository.findAll();
        }

        return tenants.stream()
                .map(this::toTenantResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get tenant statistics
     */
    public TenantResponse.TenantStats getTenantStats(String tenantId) {
        TenantConfig tenant = tenantConfigRepository.findByTenantId(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + tenantId));

        TenantResponse.TenantStats stats = new TenantResponse.TenantStats();
        // In a real implementation, these values would be queried from other services
        stats.setCurrentUsers(0L);
        stats.setTotalAssets(0L);
        stats.setUsedStorage(0L);

        return stats;
    }

    private TenantResponse toTenantResponse(TenantConfig tenant) {
        TenantResponse response = new TenantResponse();
        response.setId(tenant.getId());
        response.setTenantId(tenant.getTenantId());
        response.setTenantName(tenant.getTenantName());
        response.setDescription(tenant.getDescription());
        response.setMaxUsers(tenant.getMaxUsers());
        response.setMaxStorage(tenant.getMaxStorage());
        response.setFeatures(tenant.getFeatures());
        response.setStatus(tenant.getStatus().name());
        response.setCreatedAt(tenant.getCreatedAt());
        response.setExpiredAt(tenant.getExpiredAt());

        // Get stats
        TenantResponse.TenantStats stats = new TenantResponse.TenantStats();
        stats.setCurrentUsers(0L);
        stats.setTotalAssets(0L);
        stats.setUsedStorage(0L);
        response.setStats(stats);

        return response;
    }
}
