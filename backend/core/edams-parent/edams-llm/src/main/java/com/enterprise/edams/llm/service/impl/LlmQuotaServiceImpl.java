package com.enterprise.edams.llm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.enterprise.edams.llm.dto.LlmQuotaDTO;
import com.enterprise.edams.llm.entity.LlmQuota;
import com.enterprise.edams.llm.repository.LlmQuotaMapper;
import com.enterprise.edams.llm.service.LlmQuotaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 配额服务实现
 */
@Slf4j
@Service
public class LlmQuotaServiceImpl extends ServiceImpl<LlmQuotaMapper, LlmQuota> implements LlmQuotaService {

    private final LlmQuotaMapper quotaMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    
    @Autowired(required = false)
    private RestTemplate restTemplate;
    
    @Value("${edams.tenant.default-id:1}")
    private Long defaultTenantId;

    private static final String QUOTA_KEY_PREFIX = "llm:quota:";

    public LlmQuotaServiceImpl(LlmQuotaMapper quotaMapper, RedisTemplate<String, Object> redisTemplate) {
        this.quotaMapper = quotaMapper;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Page<LlmQuota> selectPage(Long tenantId, Long userId, int pageNum, int pageSize) {
        Page<LlmQuota> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<LlmQuota> wrapper = new LambdaQueryWrapper<>();
        if (tenantId != null) {
            wrapper.eq(LlmQuota::getTenantId, tenantId);
        }
        if (userId != null) {
            wrapper.eq(LlmQuota::getUserId, userId);
        }
        wrapper.orderByDesc(LlmQuota::getUpdateTime);
        return page(page, wrapper);
    }

    @Override
    public LlmQuotaDTO getById(Long id) {
        LlmQuota quota = baseMapper.selectById(id);
        if (quota == null) {
            return null;
        }
        return convertToDTO(quota);
    }

    @Override
    @Transactional
    public LlmQuotaDTO create(LlmQuotaDTO dto) {
        LlmQuota quota = new LlmQuota();
        BeanUtils.copyProperties(dto, quota);
        quota.setStatus("ACTIVE");
        quota.setEnabled(true);
        quota.setQuotaUsed(0L);
        quota.setCostUsed(BigDecimal.ZERO);
        quota.setRequestUsed(0);
        baseMapper.insert(quota);
        log.info("Created quota: {}", quota.getId());
        return convertToDTO(quota);
    }

    @Override
    @Transactional
    public LlmQuotaDTO update(Long id, LlmQuotaDTO dto) {
        LlmQuota quota = baseMapper.selectById(id);
        if (quota == null) {
            throw new RuntimeException("Quota not found: " + id);
        }
        BeanUtils.copyProperties(dto, quota, "id", "createTime", "quotaUsed", "costUsed", "requestUsed");
        baseMapper.updateById(quota);
        log.info("Updated quota: {}", id);
        return convertToDTO(quota);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        baseMapper.deleteById(id);
        log.info("Deleted quota: {}", id);
    }

    @Override
    public LlmQuotaDTO getActiveQuota(Long userId, Long modelId) {
        LlmQuota quota = quotaMapper.selectActiveQuota(userId, modelId);
        if (quota == null) {
            return null;
        }
        return convertToDTO(quota);
    }

    @Override
    public LlmQuotaDTO getTenantQuota(Long tenantId, Long modelId) {
        LlmQuota quota = quotaMapper.selectTenantQuota(tenantId, modelId);
        if (quota == null) {
            return null;
        }
        return convertToDTO(quota);
    }

    @Override
    @Transactional
    public void consumeQuota(Long userId, Long modelId, Long tokens, BigDecimal cost) {
        // 先尝试用户配额
        LlmQuota userQuota = quotaMapper.selectActiveQuota(userId, modelId);
        if (userQuota != null) {
            quotaMapper.updateUsedQuota(userQuota.getId(), tokens, cost);
            invalidateQuotaCache(userId, modelId);
            return;
        }

        // 尝试租户配额 - 获取真实的租户ID
        if (userId != null) {
            Long tenantId = getTenantIdFromUser(userId, modelId);
            LlmQuota tenantQuota = quotaMapper.selectTenantQuota(tenantId, modelId);
            if (tenantQuota != null) {
                quotaMapper.updateUsedQuota(tenantQuota.getId(), tokens, cost);
            }
        }
    }
    
    /**
     * 从用户服务获取租户ID
     */
    private Long getTenantIdFromUser(Long userId, Long modelId) {
        try {
            // 优先从Redis缓存获取
            String cacheKey = "user:tenant:" + userId;
            Object cachedTenantId = redisTemplate.opsForValue().get(cacheKey);
            if (cachedTenantId != null) {
                return Long.valueOf(cachedTenantId.toString());
            }
            
            // 如果有用户服务，则调用获取租户ID
            if (restTemplate != null) {
                String url = "http://edams-user/api/v1/users/" + userId + "/tenant";
                try {
                    @SuppressWarnings("unchecked")
                    java.util.Map<String, Object> response = restTemplate.getForObject(url, java.util.Map.class);
                    if (response != null && response.get("tenantId") != null) {
                        Long tenantId = Long.valueOf(response.get("tenantId").toString());
                        // 缓存到Redis（1小时过期）
                        redisTemplate.opsForValue().set(cacheKey, tenantId.toString());
                        return tenantId;
                    }
                } catch (Exception e) {
                    log.warn("获取用户租户ID失败，使用默认租户: {}", e.getMessage());
                }
            }
            
            // 从当前用户配额查询租户ID
            LlmQuota existingQuota = quotaMapper.selectActiveQuota(userId, modelId);
            if (existingQuota != null && existingQuota.getTenantId() != null) {
                return existingQuota.getTenantId();
            }
            
            return defaultTenantId;
        } catch (Exception e) {
            log.warn("获取租户ID异常，使用默认值: {}", e.getMessage());
            return defaultTenantId;
        }
    }

    @Override
    public boolean checkQuota(Long userId, Long modelId, int estimatedTokens) {
        String cacheKey = QUOTA_KEY_PREFIX + userId + ":" + modelId;
        Long cachedTokens = (Long) redisTemplate.opsForValue().get(cacheKey);
        
        if (cachedTokens != null) {
            return cachedTokens + estimatedTokens <= 1000000; // 默认100万token限制
        }
        
        LlmQuota quota = quotaMapper.selectActiveQuota(userId, modelId);
        if (quota == null) {
            return true; // 无限制
        }
        
        return quota.getQuotaLimit() - quota.getQuotaUsed() >= estimatedTokens;
    }

    @Override
    @Transactional
    public void resetExpiredQuotas() {
        List<LlmQuota> expiredQuotas = quotaMapper.selectExpiredQuotas("DAILY");
        for (LlmQuota quota : expiredQuotas) {
            quota.setQuotaUsed(0L);
            quota.setCostUsed(BigDecimal.ZERO);
            quota.setRequestUsed(0);
            quota.setPeriodStart(LocalDateTime.now());
            quota.setPeriodEnd(LocalDateTime.now().plusDays(1));
            baseMapper.updateById(quota);
            log.info("Reset expired quota: {}", quota.getId());
        }
    }

    @Override
    public List<LlmQuotaDTO> getUserQuotas(Long userId) {
        LambdaQueryWrapper<LlmQuota> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LlmQuota::getUserId, userId)
               .eq(LlmQuota::getStatus, "ACTIVE")
               .orderByDesc(LlmQuota::getUpdateTime);
        List<LlmQuota> quotas = baseMapper.selectList(wrapper);
        return quotas.stream().map(this::convertToDTO).toList();
    }

    private void invalidateQuotaCache(Long userId, Long modelId) {
        String cacheKey = QUOTA_KEY_PREFIX + userId + ":" + modelId;
        redisTemplate.delete(cacheKey);
    }

    private LlmQuotaDTO convertToDTO(LlmQuota quota) {
        LlmQuotaDTO dto = new LlmQuotaDTO();
        BeanUtils.copyProperties(quota, dto);
        
        // 计算使用百分比
        if (quota.getQuotaLimit() != null && quota.getQuotaLimit() > 0) {
            dto.setUsagePercent((double) quota.getQuotaUsed() / quota.getQuotaLimit() * 100);
        }
        if (quota.getCostLimit() != null && quota.getCostLimit().compareTo(BigDecimal.ZERO) > 0) {
            dto.setCostPercent(quota.getCostUsed().divide(quota.getCostLimit(), 4, java.math.RoundingMode.HALF_UP).doubleValue() * 100);
        }
        
        return dto;
    }
}
