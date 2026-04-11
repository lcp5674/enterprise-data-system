package com.enterprise.edams.lifecycle.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.edams.common.exception.BusinessException;
import com.enterprise.edams.lifecycle.entity.DataLifecycle;
import com.enterprise.edams.lifecycle.entity.LifecycleStage;
import com.enterprise.edams.lifecycle.repository.LifecycleMapper;
import com.enterprise.edams.lifecycle.repository.LifecycleStageMapper;
import com.enterprise.edams.lifecycle.service.LifecycleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 数据生命周期服务实现
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LifecycleServiceImpl implements LifecycleService {

    private final LifecycleMapper lifecycleMapper;
    private final LifecycleStageMapper lifecycleStageMapper;

    /**
     * 创建数据生命周期记录
     */
    @Override
    @Transactional
    public DataLifecycle createLifecycle(DataLifecycle lifecycle) {
        if (lifecycleMapper.findByDataAssetId(lifecycle.getDataAssetId()) != null) {
            throw new BusinessException("数据资产已存在生命周期记录");
        }

        LifecycleStage stage = lifecycleStageMapper.findByStageCode(lifecycle.getCurrentStage());
        if (stage == null) {
            throw new BusinessException("生命周期阶段不存在");
        }

        lifecycle.setStageChangedAt(LocalDateTime.now());
        lifecycle.setStatus(1);
        lifecycleMapper.insert(lifecycle);
        return lifecycle;
    }

    /**
     * 更新数据生命周期记录
     */
    @Override
    @Transactional
    public DataLifecycle updateLifecycle(Long id, DataLifecycle lifecycle) {
        DataLifecycle existing = lifecycleMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("生命周期记录不存在");
        }

        // 保存上一阶段信息
        lifecycle.setPreviousStage(existing.getCurrentStage());
        lifecycle.setStageChangedAt(LocalDateTime.now());

        lifecycleMapper.updateById(lifecycle);
        return lifecycleMapper.selectById(id);
    }

    /**
     * 获取数据生命周期记录
     */
    @Override
    public DataLifecycle getLifecycle(Long id) {
        return lifecycleMapper.selectById(id);
    }

    /**
     * 根据数据资产ID获取生命周期记录
     */
    @Override
    public DataLifecycle getLifecycleByDataAssetId(Long dataAssetId) {
        return lifecycleMapper.findByDataAssetId(dataAssetId);
    }

    /**
     * 删除数据生命周期记录
     */
    @Override
    @Transactional
    public void deleteLifecycle(Long id) {
        lifecycleMapper.deleteById(id);
    }

    /**
     * 分页查询数据生命周期记录
     */
    @Override
    public IPage<DataLifecycle> listLifecycles(Integer pageNum, Integer pageSize) {
        Page<DataLifecycle> page = new Page<>(pageNum, pageSize);
        QueryWrapper<DataLifecycle> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("stage_changed_at");
        return lifecycleMapper.selectPage(page, wrapper);
    }

    /**
     * 根据关键词搜索生命周期记录
     */
    @Override
    public IPage<DataLifecycle> searchLifecycles(String keyword, Integer pageNum, Integer pageSize) {
        Page<DataLifecycle> page = new Page<>(pageNum, pageSize);
        return lifecycleMapper.searchByKeyword(page, keyword);
    }

    /**
     * 根据阶段查询生命周期记录
     */
    @Override
    public IPage<DataLifecycle> listLifecyclesByStage(String stage, Integer pageNum, Integer pageSize) {
        Page<DataLifecycle> page = new Page<>(pageNum, pageSize);
        return lifecycleMapper.findByStage(page, stage);
    }

    /**
     * 切换到下一生命周期阶段
     */
    @Override
    @Transactional
    public DataLifecycle transitionToNextStage(Long lifecycleId) {
        DataLifecycle lifecycle = lifecycleMapper.selectById(lifecycleId);
        if (lifecycle == null) {
            throw new BusinessException("生命周期记录不存在");
        }

        LifecycleStage currentStage = lifecycleStageMapper.findByStageCode(lifecycle.getCurrentStage());
        if (currentStage == null) {
            throw new BusinessException("当前阶段不存在");
        }

        String nextStageCode = currentStage.getNextStageCode();
        if (nextStageCode == null) {
            throw new BusinessException("无下一阶段");
        }

        LifecycleStage nextStage = lifecycleStageMapper.findByStageCode(nextStageCode);
        if (nextStage == null) {
            throw new BusinessException("下一阶段不存在");
        }

        lifecycle.setPreviousStage(lifecycle.getCurrentStage());
        lifecycle.setCurrentStage(nextStageCode);
        lifecycle.setStageChangedAt(LocalDateTime.now());
        lifecycleMapper.updateById(lifecycle);

        return lifecycle;
    }

    /**
     * 获取所有生命周期阶段
     */
    @Override
    public List<LifecycleStage> getAllStages() {
        QueryWrapper<LifecycleStage> wrapper = new QueryWrapper<>();
        wrapper.eq("enabled", 1);
        wrapper.orderByAsc("sort_order");
        return lifecycleStageMapper.selectList(wrapper);
    }
}