package com.enterprise.edams.lifecycle.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.edams.common.exception.BusinessException;
import com.enterprise.edams.lifecycle.entity.LifecyclePolicy;
import com.enterprise.edams.lifecycle.repository.LifecyclePolicyMapper;
import com.enterprise.edams.lifecycle.service.LifecyclePolicyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 生命周期策略服务实现
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LifecyclePolicyServiceImpl implements LifecyclePolicyService {

    private final LifecyclePolicyMapper lifecyclePolicyMapper;

    /**
     * 创建生命周期策略
     */
    @Override
    @Transactional
    public LifecyclePolicy createPolicy(LifecyclePolicy policy) {
        if (lifecyclePolicyMapper.findByPolicyCode(policy.getPolicyCode()) != null) {
            throw new BusinessException("策略编码已存在");
        }

        policy.setEnabled(1);
        lifecyclePolicyMapper.insert(policy);
        return policy;
    }

    /**
     * 更新生命周期策略
     */
    @Override
    @Transactional
    public LifecyclePolicy updatePolicy(Long id, LifecyclePolicy policy) {
        LifecyclePolicy existing = lifecyclePolicyMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("生命周期策略不存在");
        }

        lifecyclePolicyMapper.updateById(policy);
        return lifecyclePolicyMapper.selectById(id);
    }

    /**
     * 获取生命周期策略
     */
    @Override
    public LifecyclePolicy getPolicy(Long id) {
        return lifecyclePolicyMapper.selectById(id);
    }

    /**
     * 根据策略编码获取生命周期策略
     */
    @Override
    public LifecyclePolicy getPolicyByCode(String policyCode) {
        return lifecyclePolicyMapper.findByPolicyCode(policyCode);
    }

    /**
     * 删除生命周期策略
     */
    @Override
    @Transactional
    public void deletePolicy(Long id) {
        lifecyclePolicyMapper.deleteById(id);
    }

    /**
     * 分页查询生命周期策略
     */
    @Override
    public IPage<LifecyclePolicy> listPolicies(Integer pageNum, Integer pageSize) {
        Page<LifecyclePolicy> page = new Page<>(pageNum, pageSize);
        QueryWrapper<LifecyclePolicy> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("create_time");
        return lifecyclePolicyMapper.selectPage(page, wrapper);
    }

    /**
     * 根据数据资产类型查询生命周期策略
     */
    @Override
    public IPage<LifecyclePolicy> listPoliciesByAssetType(String assetType, Integer pageNum, Integer pageSize) {
        Page<LifecyclePolicy> page = new Page<>(pageNum, pageSize);
        return lifecyclePolicyMapper.findByAssetType(page, assetType);
    }

    /**
     * 启用生命周期策略
     */
    @Override
    @Transactional
    public LifecyclePolicy enablePolicy(Long id) {
        LifecyclePolicy policy = lifecyclePolicyMapper.selectById(id);
        if (policy == null) {
            throw new BusinessException("生命周期策略不存在");
        }

        policy.setEnabled(1);
        lifecyclePolicyMapper.updateById(policy);
        return policy;
    }

    /**
     * 禁用生命周期策略
     */
    @Override
    @Transactional
    public LifecyclePolicy disablePolicy(Long id) {
        LifecyclePolicy policy = lifecyclePolicyMapper.selectById(id);
        if (policy == null) {
            throw new BusinessException("生命周期策略不存在");
        }

        policy.setEnabled(0);
        lifecyclePolicyMapper.updateById(policy);
        return policy;
    }

    /**
     * 获取适用的生命周期策略
     */
    @Override
    public LifecyclePolicy getApplicablePolicy(String assetType) {
        QueryWrapper<LifecyclePolicy> wrapper = new QueryWrapper<>();
        wrapper.eq("data_asset_type", assetType);
        wrapper.eq("enabled", 1);
        wrapper.orderByDesc("create_time");
        wrapper.last("LIMIT 1");
        return lifecyclePolicyMapper.selectOne(wrapper);
    }
}