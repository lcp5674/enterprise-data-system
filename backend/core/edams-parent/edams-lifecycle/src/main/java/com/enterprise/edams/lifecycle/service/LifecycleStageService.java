package com.enterprise.edams.lifecycle.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.enterprise.edams.lifecycle.entity.LifecycleStage;

import java.util.List;

/**
 * 生命周期阶段服务接口
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
public interface LifecycleStageService {

    /**
     * 创建生命周期阶段
     */
    LifecycleStage createStage(LifecycleStage stage);

    /**
     * 更新生命周期阶段
     */
    LifecycleStage updateStage(Long id, LifecycleStage stage);

    /**
     * 获取生命周期阶段
     */
    LifecycleStage getStage(Long id);

    /**
     * 根据阶段编码获取生命周期阶段
     */
    LifecycleStage getStageByCode(String stageCode);

    /**
     * 删除生命周期阶段
     */
    void deleteStage(Long id);

    /**
     * 分页查询生命周期阶段
     */
    IPage<LifecycleStage> listStages(Integer pageNum, Integer pageSize);

    /**
     * 获取所有启用的生命周期阶段
     */
    List<LifecycleStage> getAllEnabledStages();

    /**
     * 启用生命周期阶段
     */
    LifecycleStage enableStage(Long id);

    /**
     * 禁用生命周期阶段
     */
    LifecycleStage disableStage(Long id);
}