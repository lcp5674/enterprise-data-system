package com.enterprise.edams.sla.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.enterprise.edams.sla.dto.*;
import com.enterprise.edams.sla.entity.*;
import com.enterprise.edams.sla.mapper.SlaDefinitionMapper;
import com.enterprise.edams.sla.mapper.SlaMetricMapper;
import com.enterprise.edams.sla.mapper.SlaViolationMapper;
import com.enterprise.edams.sla.service.SlaDefinitionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * SLA定义服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class SlaDefinitionServiceImpl extends ServiceImpl<SlaDefinitionMapper, SlaDefinition>
        implements SlaDefinitionService {
    
    private final SlaViolationMapper violationMapper;
    private final SlaMetricMapper metricMapper;
    
    @Override
    public SlaDefinition createSla(SlaCreateRequest request) {
        log.info("创建SLA定义: {}", request.getSlaName());
        
        SlaDefinition sla = SlaDefinition.builder()
                .slaCode(generateSlaCode())
                .slaName(request.getSlaName())
                .assetId(request.getAssetId())
                .slaType(request.getSlaType())
                .targetValue(request.getTargetValue())
                .unit(request.getUnit())
                .operator(request.getOperator())
                .windowType(request.getWindowType())
                .windowSize(request.getWindowSize())
                .severity(request.getSeverity())
                .contact(request.getContact())
                .alertMethods(request.getAlertMethods())
                .status(SlaStatus.ACTIVE)
                .description(request.getDescription())
                .build();
        
        this.save(sla);
        return sla;
    }
    
    @Override
    public SlaDefinition updateSla(Long id, SlaUpdateRequest request) {
        SlaDefinition sla = this.getById(id);
        if (sla == null) {
            throw new RuntimeException("SLA不存在: " + id);
        }
        
        if (request.getSlaName() != null) sla.setSlaName(request.getSlaName());
        if (request.getTargetValue() != null) sla.setTargetValue(request.getTargetValue());
        if (request.getUnit() != null) sla.setUnit(request.getUnit());
        if (request.getWindowSize() != null) sla.setWindowSize(request.getWindowSize());
        if (request.getContact() != null) sla.setContact(request.getContact());
        if (request.getAlertMethods() != null) sla.setAlertMethods(request.getAlertMethods());
        if (request.getDescription() != null) sla.setDescription(request.getDescription());
        
        this.updateById(sla);
        return sla;
    }
    
    @Override
    public void deleteSla(Long id) {
        this.removeById(id);
    }
    
    @Override
    public SlaDefinition getSlaById(Long id) {
        return this.getById(id);
    }
    
    @Override
    public Page<SlaDefinition> searchSlas(SlaSearchRequest request) {
        Page<SlaDefinition> page = new Page<>(request.getPageNum(), request.getPageSize());
        
        LambdaQueryWrapper<SlaDefinition> wrapper = new LambdaQueryWrapper<>();
        
        if (request.getSlaCode() != null) {
            wrapper.like(SlaDefinition::getSlaCode, request.getSlaCode());
        }
        if (request.getSlaName() != null) {
            wrapper.like(SlaDefinition::getSlaName, request.getSlaName());
        }
        if (request.getAssetId() != null) {
            wrapper.eq(SlaDefinition::getAssetId, request.getAssetId());
        }
        if (request.getSlaType() != null) {
            wrapper.eq(SlaDefinition::getSlaType, request.getSlaType());
        }
        if (request.getStatus() != null) {
            wrapper.eq(SlaDefinition::getStatus, request.getStatus());
        }
        if (request.getSeverity() != null) {
            wrapper.eq(SlaDefinition::getSeverity, request.getSeverity());
        }
        
        wrapper.orderByDesc(SlaDefinition::getCreatedTime);
        return this.page(page, wrapper);
    }
    
    @Override
    public void enableSla(Long id) {
        SlaDefinition sla = this.getById(id);
        if (sla != null) {
            sla.setStatus(SlaStatus.ACTIVE);
            this.updateById(sla);
        }
    }
    
    @Override
    public void disableSla(Long id) {
        SlaDefinition sla = this.getById(id);
        if (sla != null) {
            sla.setStatus(SlaStatus.INACTIVE);
            this.updateById(sla);
        }
    }
    
    @Override
    public SlaStatistics getSlaStatistics() {
        long total = this.count();
        long active = this.count(new LambdaQueryWrapper<SlaDefinition>()
                .eq(SlaDefinition::getStatus, SlaStatus.ACTIVE));
        long breached = this.count(new LambdaQueryWrapper<SlaDefinition>()
                .eq(SlaDefinition::getStatus, SlaStatus.BREACHED));
        long violationCount = violationMapper.selectCount(null);
        
        return SlaStatistics.builder()
                .totalSlas(total)
                .activeSlas(active)
                .breachedSlas(breached)
                .totalViolations(violationCount)
                .complianceRate(total > 0 ? (double) (total - breached) / total * 100 : 100)
                .build();
    }
    
    @Override
    public SlaCompliance getSlaCompliance(Long slaId) {
        SlaDefinition sla = this.getById(slaId);
        if (sla == null) {
            throw new RuntimeException("SLA不存在: " + slaId);
        }
        
        LambdaQueryWrapper<SlaMetric> wrapper = new LambdaQueryWrapper<SlaMetric>()
                .eq(SlaMetric::getSlaDefinitionId, slaId)
                .orderByDesc(SlaMetric::getTimestamp)
                .last("LIMIT 100");
        
        List<SlaMetric> metrics = metricMapper.selectList(wrapper);
        if (metrics.isEmpty()) {
            return SlaCompliance.builder()
                    .slaId(slaId)
                    .slaCode(sla.getSlaCode())
                    .complianceRate(BigDecimal.valueOf(100))
                    .totalChecks(0)
                    .passedChecks(0)
                    .failedChecks(0)
                    .build();
        }
        
        long passed = metrics.stream().filter(m -> m.getMeetsTarget() != null && m.getMeetsTarget()).count();
        BigDecimal rate = BigDecimal.valueOf((double) passed / metrics.size() * 100)
                .setScale(2, RoundingMode.HALF_UP);
        
        return SlaCompliance.builder()
                .slaId(slaId)
                .slaCode(sla.getSlaCode())
                .complianceRate(rate)
                .totalChecks(metrics.size())
                .passedChecks((int) passed)
                .failedChecks((int) (metrics.size() - passed))
                .build();
    }
    
    private String generateSlaCode() {
        return "SLA-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
