package com.enterprise.edams.sandbox.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.enterprise.edams.sandbox.dto.*;
import com.enterprise.edams.sandbox.entity.*;
import com.enterprise.edams.sandbox.mapper.*;
import com.enterprise.edams.sandbox.service.SandboxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 沙箱服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class SandboxServiceImpl extends ServiceImpl<SandboxInstanceMapper, SandboxInstance>
        implements SandboxService {
    
    private final SampleDataRequestMapper sampleRequestMapper;
    private final DesensitizationRuleMapper desensitizationRuleMapper;
    
    @Value("${sandbox.default-timeout:3600000}")
    private long defaultTimeout;
    
    @Override
    public SandboxInstance createSandbox(SandboxCreateRequest request) {
        log.info("创建沙箱实例: name={}, type={}", request.getInstanceName(), request.getSandboxType());
        
        LocalDateTime expireTime = LocalDateTime.now().plusHours(request.getValidHours() != null ? 
                request.getValidHours() : 24);
        
        SandboxInstance instance = SandboxInstance.builder()
                .instanceCode(generateInstanceCode())
                .instanceName(request.getInstanceName())
                .sandboxType(request.getSandboxType())
                .status(InstanceStatus.CREATING)
                .userId(request.getUserId())
                .userName(request.getUserName())
                .assetIds(request.getAssetIds())
                .description(request.getDescription())
                .expireTime(expireTime)
                .resourceConfig(request.getResourceConfig())
                .build();
        
        this.save(instance);
        
        // 模拟创建成功
        instance.setStatus(InstanceStatus.RUNNING);
        this.updateById(instance);
        
        log.info("沙箱实例创建成功: instanceCode={}", instance.getInstanceCode());
        return instance;
    }
    
    @Override
    public SandboxInstance getInstance(Long id) {
        return this.getById(id);
    }
    
    @Override
    public Page<SandboxInstance> searchInstances(InstanceSearchRequest request) {
        Page<SandboxInstance> page = new Page<>(request.getPageNum(), request.getPageSize());
        
        LambdaQueryWrapper<SandboxInstance> wrapper = new LambdaQueryWrapper<>();
        
        if (request.getUserId() != null) {
            wrapper.eq(SandboxInstance::getUserId, request.getUserId());
        }
        if (request.getInstanceName() != null) {
            wrapper.like(SandboxInstance::getInstanceName, request.getInstanceName());
        }
        if (request.getSandboxType() != null) {
            wrapper.eq(SandboxInstance::getSandboxType, request.getSandboxType());
        }
        if (request.getStatus() != null) {
            wrapper.eq(SandboxInstance::getStatus, request.getStatus());
        }
        
        wrapper.ne(SandboxInstance::getStatus, InstanceStatus.DELETED);
        wrapper.orderByDesc(SandboxInstance::getCreatedTime);
        
        return this.page(page, wrapper);
    }
    
    @Override
    public SandboxInstance startInstance(Long id) {
        SandboxInstance instance = this.getById(id);
        if (instance == null) {
            throw new RuntimeException("实例不存在: " + id);
        }
        
        if (instance.getStatus() == InstanceStatus.EXPIRED) {
            throw new RuntimeException("实例已过期，无法启动");
        }
        
        instance.setStatus(InstanceStatus.RUNNING);
        this.updateById(instance);
        
        log.info("沙箱实例启动: instanceCode={}", instance.getInstanceCode());
        return instance;
    }
    
    @Override
    public SandboxInstance stopInstance(Long id) {
        SandboxInstance instance = this.getById(id);
        if (instance == null) {
            throw new RuntimeException("实例不存在: " + id);
        }
        
        instance.setStatus(InstanceStatus.STOPPED);
        this.updateById(instance);
        
        log.info("沙箱实例停止: instanceCode={}", instance.getInstanceCode());
        return instance;
    }
    
    @Override
    public void deleteInstance(Long id) {
        SandboxInstance instance = this.getById(id);
        if (instance == null) {
            throw new RuntimeException("实例不存在: " + id);
        }
        
        instance.setStatus(InstanceStatus.DELETED);
        instance.setDeletedTime(LocalDateTime.now());
        this.updateById(instance);
        
        log.info("沙箱实例删除: instanceCode={}", instance.getInstanceCode());
    }
    
    @Override
    public SampleDataRequest requestSampleData(SampleDataRequestDto request) {
        log.info("申请样本数据: assetId={}, type={}", request.getAssetId(), request.getSampleType());
        
        SampleDataRequest sampleRequest = SampleDataRequest.builder()
                .requestNo(generateRequestNo())
                .assetId(request.getAssetId())
                .assetName(request.getAssetName())
                .userId(request.getUserId())
                .userName(request.getUserName())
                .sampleType(request.getSampleType())
                .sampleCount(request.getSampleCount())
                .sizeLimit(request.getSizeLimit())
                .desensitizationRuleId(request.getDesensitizationRuleId())
                .purpose(request.getPurpose())
                .status(SampleRequestStatus.PENDING)
                .remark(request.getRemark())
                .build();
        
        sampleRequestMapper.insert(sampleRequest);
        
        // 模拟处理
        sampleRequest.setStatus(SampleRequestStatus.PROCESSING);
        sampleRequest.setProcessTime(LocalDateTime.now());
        sampleRequestMapper.updateById(sampleRequest);
        
        // 模拟完成
        sampleRequest.setStatus(SampleRequestStatus.COMPLETED);
        sampleRequest.setCompleteTime(LocalDateTime.now());
        sampleRequest.setDownloadUrl("sandbox/sample/" + sampleRequest.getRequestNo() + ".zip");
        sampleRequestMapper.updateById(sampleRequest);
        
        log.info("样本数据申请完成: requestNo={}", sampleRequest.getRequestNo());
        return sampleRequest;
    }
    
    @Override
    public Page<SampleDataRequest> searchSampleRequests(SampleSearchRequest request) {
        Page<SampleDataRequest> page = new Page<>(request.getPageNum(), request.getPageSize());
        
        LambdaQueryWrapper<SampleDataRequest> wrapper = new LambdaQueryWrapper<>();
        
        if (request.getUserId() != null) {
            wrapper.eq(SampleDataRequest::getUserId, request.getUserId());
        }
        if (request.getAssetId() != null) {
            wrapper.eq(SampleDataRequest::getAssetId, request.getAssetId());
        }
        if (request.getStatus() != null) {
            wrapper.eq(SampleDataRequest::getStatus, request.getStatus());
        }
        
        wrapper.orderByDesc(SampleDataRequest::getCreatedTime);
        return sampleRequestMapper.selectPage(page, wrapper);
    }
    
    @Override
    public DesensitizationRule createDesensitizationRule(DesensitizationRuleDto request) {
        DesensitizationRule rule = DesensitizationRule.builder()
                .ruleCode(generateRuleCode())
                .ruleName(request.getRuleName())
                .dataType(request.getDataType())
                .method(request.getMethod())
                .params(request.getParams())
                .priority(request.getPriority() != null ? request.getPriority() : 0)
                .status(RuleStatus.ACTIVE)
                .description(request.getDescription())
                .build();
        
        desensitizationRuleMapper.insert(rule);
        return rule;
    }
    
    @Override
    public List<DesensitizationRule> getDesensitizationRules() {
        return desensitizationRuleMapper.selectList(
                new LambdaQueryWrapper<DesensitizationRule>()
                        .eq(DesensitizationRule::getStatus, RuleStatus.ACTIVE)
                        .orderByAsc(DesensitizationRule::getPriority));
    }
    
    @Override
    public String previewDesensitization(Long ruleId, String originalValue) {
        DesensitizationRule rule = desensitizationRuleMapper.selectById(ruleId);
        if (rule == null) {
            throw new RuntimeException("脱敏规则不存在: " + ruleId);
        }
        
        // 简单实现，实际应该根据脱敏方法处理
        switch (rule.getMethod()) {
            case MASK:
                return maskValue(originalValue);
            case HASH:
                return String.valueOf(originalValue.hashCode());
            case REPLACE:
                return "***";
            default:
                return originalValue;
        }
    }
    
    // ============ 私有方法 ============
    
    private String maskValue(String value) {
        if (value == null || value.length() < 4) {
            return "****";
        }
        int visible = Math.min(3, value.length() / 4);
        return value.substring(0, visible) + "****" + value.substring(value.length() - visible);
    }
    
    private String generateInstanceCode() {
        return "SBX" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) 
                + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }
    
    private String generateRequestNo() {
        return "SAMPLE" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) 
                + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }
    
    private String generateRuleCode() {
        return "RULE" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
