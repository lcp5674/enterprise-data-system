package com.enterprise.edams.sandbox.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.enterprise.edams.sandbox.dto.*;
import com.enterprise.edams.sandbox.entity.*;

import java.util.List;

/**
 * 沙箱服务接口
 */
public interface SandboxService extends IService<SandboxInstance> {
    
    /**
     * 创建沙箱实例
     */
    SandboxInstance createSandbox(SandboxCreateRequest request);
    
    /**
     * 获取实例详情
     */
    SandboxInstance getInstance(Long id);
    
    /**
     * 查询实例列表
     */
    Page<SandboxInstance> searchInstances(InstanceSearchRequest request);
    
    /**
     * 启动实例
     */
    SandboxInstance startInstance(Long id);
    
    /**
     * 停止实例
     */
    SandboxInstance stopInstance(Long id);
    
    /**
     * 删除实例
     */
    void deleteInstance(Long id);
    
    /**
     * 申请样本数据
     */
    SampleDataRequest requestSampleData(SampleDataRequestDto request);
    
    /**
     * 查询样本申请
     */
    Page<SampleDataRequest> searchSampleRequests(SampleSearchRequest request);
    
    /**
     * 创建脱敏规则
     */
    DesensitizationRule createDesensitizationRule(DesensitizationRuleDto request);
    
    /**
     * 获取脱敏规则
     */
    List<DesensitizationRule> getDesensitizationRules();
    
    /**
     * 预览脱敏效果
     */
    String previewDesensitization(Long ruleId, String originalValue);
}
