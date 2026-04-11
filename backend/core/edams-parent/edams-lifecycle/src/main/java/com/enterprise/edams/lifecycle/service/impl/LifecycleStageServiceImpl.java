package com.enterprise.edams.lifecycle.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.edams.common.exception.BusinessException;
import com.enterprise.edams.lifecycle.entity.LifecycleStage;
import com.enterprise.edams.lifecycle.repository.LifecycleStageMapper;
import com.enterprise.edams.lifecycle.service.LifecycleStageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 生命周期阶段服务实现
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LifecycleStageServiceImpl implements LifecycleStageService {

    private final LifecycleStageMapper lifecycleStageMapper;

    /**
     * 创建生命周期阶段
     */
    @Override
    @Transactional
    public LifecycleStage createStage(LifecycleStage stage) {
        if (lifecycleStageMapper.findByStageCode(stage.getStageCode()) != null) {
            throw new BusinessException("阶段编码已存在");
        }

        stage.setEnabled(1);
        lifecycleStageMapper.insert(stage);
        return stage;
    }

    /**
     * 更新生命周期阶段
     */
    @Override
    @Transactional
    public LifecycleStage updateStage(Long id, LifecycleStage stage) {
        LifecycleStage existing = lifecycleStageMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("生命周期阶段不存在");
        }

        lifecycleStageMapper.updateById(stage);
        return lifecycleStageMapper.selectById(id);
    }

    /**
     * 获取生命周期阶段
     */
    @Override
    public LifecycleStage getStage(Long id) {
        return lifecycleStageMapper.selectById(id);
    }

    /**
     * 根据阶段编码获取生命周期阶段
     */
    @Override
    public LifecycleStage getStageByCode(String stageCode) {
        return lifecycleStageMapper.findByStageCode(stageCode);
    }

    /**
     * 删除生命周期阶段
     */
    @Override
    @Transactional
    public void deleteStage(Long id) {
        lifecycleStageMapper.deleteById(id);
    }

    /**
     * 分页查询生命周期阶段
     */
    @Override
    public IPage<LifecycleStage> listStages(Integer pageNum, Integer pageSize) {
        Page<LifecycleStage> page = new Page<>(pageNum, pageSize);
        QueryWrapper<LifecycleStage> wrapper = new QueryWrapper<>();
        wrapper.orderByAsc("sort_order");
        return lifecycleStageMapper.selectPage(page, wrapper);
    }

    /**
     * 获取所有启用的生命周期阶段
     */
    @Override
    public List<LifecycleStage> getAllEnabledStages() {
        QueryWrapper<LifecycleStage> wrapper = new QueryWrapper<>();
        wrapper.eq("enabled", 1);
        wrapper.orderByAsc("sort_order");
        return lifecycleStageMapper.selectList(wrapper);
    }

    /**
     * 启用生命周期阶段
     */
    @Override
    @Transactional
    public LifecycleStage enableStage(Long id) {
        LifecycleStage stage = lifecycleStageMapper.selectById(id);
        if (stage == null) {
            throw new BusinessException("生命周期阶段不存在");
        }

        stage.setEnabled(1);
        lifecycleStageMapper.updateById(stage);
        return stage;
    }

    /**
     * 禁用生命周期阶段
     */
    @Override
    @Transactional
    public LifecycleStage disableStage(Long id) {
        LifecycleStage stage = lifecycleStageMapper.selectById(id);
        if (stage == null) {
            throw new BusinessException("生命周期阶段不存在");
        }

        stage.setEnabled(0);
        lifecycleStageMapper.updateById(stage);
        return stage;
    }
}