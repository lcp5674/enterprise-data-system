package com.enterprise.edams.lifecycle.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.enterprise.edams.lifecycle.dto.ArchivePolicyCreateRequest;
import com.enterprise.edams.lifecycle.dto.ArchivePolicyDTO;
import com.enterprise.edams.lifecycle.entity.ArchivePolicy;
import com.enterprise.edams.lifecycle.repository.ArchivePolicyRepository;
import com.enterprise.edams.lifecycle.service.ArchivePolicyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 归档策略服务实现
 *
 * @author EDAMS Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ArchivePolicyServiceImpl extends ServiceImpl<ArchivePolicyRepository, ArchivePolicy>
        implements ArchivePolicyService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ArchivePolicyDTO createPolicy(ArchivePolicyCreateRequest request) {
        ArchivePolicy policy = new ArchivePolicy();
        BeanUtils.copyProperties(request, policy);
        policy.setEnabled(true);
        policy.setExecuteCount(0);
        policy.setSuccessCount(0);
        policy.setFailCount(0);

        save(policy);
        return convertToDTO(policy);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ArchivePolicyDTO updatePolicy(String id, ArchivePolicyCreateRequest request) {
        ArchivePolicy policy = getById(id);
        if (policy == null) {
            throw new RuntimeException("归档策略不存在");
        }

        BeanUtils.copyProperties(request, policy);
        updateById(policy);
        return convertToDTO(policy);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePolicy(String id) {
        removeById(id);
    }

    @Override
    public ArchivePolicyDTO getPolicy(String id) {
        ArchivePolicy policy = getById(id);
        if (policy == null) {
            throw new RuntimeException("归档策略不存在");
        }
        return convertToDTO(policy);
    }

    @Override
    public Page<ArchivePolicyDTO> listPolicies(Page<ArchivePolicy> page, String keyword, String businessType, Boolean enabled) {
        LambdaQueryWrapper<ArchivePolicy> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(ArchivePolicy::getName, keyword)
                    .or()
                    .like(ArchivePolicy::getCode, keyword));
        }

        if (StringUtils.hasText(businessType)) {
            wrapper.eq(ArchivePolicy::getBusinessType, businessType);
        }

        if (enabled != null) {
            wrapper.eq(ArchivePolicy::getEnabled, enabled);
        }

        wrapper.orderByDesc(ArchivePolicy::getCreatedTime);
        Page<ArchivePolicy> resultPage = page(page, wrapper);

        List<ArchivePolicyDTO> records = resultPage.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        Page<ArchivePolicyDTO> dtoPage = new Page<>();
        BeanUtils.copyProperties(resultPage, dtoPage);
        dtoPage.setRecords(records);
        return dtoPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void enablePolicy(String id) {
        ArchivePolicy policy = getById(id);
        if (policy == null) {
            throw new RuntimeException("归档策略不存在");
        }
        policy.setEnabled(true);
        updateById(policy);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void disablePolicy(String id) {
        ArchivePolicy policy = getById(id);
        if (policy == null) {
            throw new RuntimeException("归档策略不存在");
        }
        policy.setEnabled(false);
        updateById(policy);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void executePolicy(String id) {
        ArchivePolicy policy = getById(id);
        if (policy == null) {
            throw new RuntimeException("归档策略不存在");
        }

        // TODO: 执行归档逻辑
        policy.setLastExecuteTime(LocalDateTime.now());
        policy.setExecuteCount(policy.getExecuteCount() + 1);
        updateById(policy);

        log.info("归档策略执行完成: {}", id);
    }

    @Override
    public List<ArchivePolicyDTO> getEnabledPolicies() {
        return baseMapper.findEnabledPolicies().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private ArchivePolicyDTO convertToDTO(ArchivePolicy policy) {
        ArchivePolicyDTO dto = new ArchivePolicyDTO();
        BeanUtils.copyProperties(policy, dto);
        return dto;
    }
}
