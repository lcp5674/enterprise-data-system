package com.enterprise.edams.lifecycle.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.enterprise.edams.lifecycle.entity.LifecyclePolicy;

/**
 * 生命周期策略服务接口
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
public interface LifecyclePolicyService {

    /**
     * 创建生命周期策略
     */
    LifecyclePolicy createPolicy(LifecyclePolicy policy);

    /**
     * 更新生命周期策略
     */
    LifecyclePolicy updatePolicy(Long id, LifecyclePolicy policy);

    /**
     * 获取生命周期策略
     */
    LifecyclePolicy getPolicy(Long id);

    /**
     * 根据策略编码获取生命周期策略
     */
    LifecyclePolicy getPolicyByCode(String policyCode);

    /**
     * 删除生命周期策略
     */
    void deletePolicy(Long id);

    /**
     * 分页查询生命周期策略
     */
    IPage<LifecyclePolicy> listPolicies(Integer pageNum, Integer pageSize);

    /**
     * 根据数据资产类型查询生命周期策略
     */
    IPage<LifecyclePolicy> listPoliciesByAssetType(String assetType, Integer pageNum, Integer pageSize);

    /**
     * 启用生命周期策略
     */
    LifecyclePolicy enablePolicy(Long id);

    /**
     * 禁用生命周期策略
     */
    LifecyclePolicy disablePolicy(Long id);

    /**
     * 获取适用的生命周期策略
     */
    LifecyclePolicy getApplicablePolicy(String assetType);
}