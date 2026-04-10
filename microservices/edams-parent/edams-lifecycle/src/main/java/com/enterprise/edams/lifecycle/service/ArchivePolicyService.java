package com.enterprise.edams.lifecycle.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.enterprise.edams.lifecycle.dto.ArchivePolicyCreateRequest;
import com.enterprise.edams.lifecycle.dto.ArchivePolicyDTO;
import com.enterprise.edams.lifecycle.entity.ArchivePolicy;

import java.util.List;

/**
 * 归档策略服务接口
 * 
 * @author EDAMS Team
 */
public interface ArchivePolicyService extends IService<ArchivePolicy> {

    /**
     * 创建归档策略
     */
    ArchivePolicyDTO createPolicy(ArchivePolicyCreateRequest request);

    /**
     * 更新归档策略
     */
    ArchivePolicyDTO updatePolicy(String id, ArchivePolicyCreateRequest request);

    /**
     * 删除归档策略
     */
    void deletePolicy(String id);

    /**
     * 获取归档策略详情
     */
    ArchivePolicyDTO getPolicy(String id);

    /**
     * 分页查询归档策略
     */
    Page<ArchivePolicyDTO> listPolicies(Page<ArchivePolicy> page, String keyword, String businessType, Boolean enabled);

    /**
     * 启用归档策略
     */
    void enablePolicy(String id);

    /**
     * 停用归档策略
     */
    void disablePolicy(String id);

    /**
     * 执行归档策略
     */
    void executePolicy(String id);

    /**
     * 获取启用的策略列表
     */
    List<ArchivePolicyDTO> getEnabledPolicies();
}
