package com.enterprise.edams.lifecycle.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.enterprise.edams.lifecycle.entity.DataLifecycle;
import com.enterprise.edams.lifecycle.entity.LifecycleStage;

import java.util.List;

/**
 * 数据生命周期服务接口
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
public interface LifecycleService {

    /**
     * 创建数据生命周期记录
     */
    DataLifecycle createLifecycle(DataLifecycle lifecycle);

    /**
     * 更新数据生命周期记录
     */
    DataLifecycle updateLifecycle(Long id, DataLifecycle lifecycle);

    /**
     * 获取数据生命周期记录
     */
    DataLifecycle getLifecycle(Long id);

    /**
     * 根据数据资产ID获取生命周期记录
     */
    DataLifecycle getLifecycleByDataAssetId(Long dataAssetId);

    /**
     * 删除数据生命周期记录
     */
    void deleteLifecycle(Long id);

    /**
     * 分页查询数据生命周期记录
     */
    IPage<DataLifecycle> listLifecycles(Integer pageNum, Integer pageSize);

    /**
     * 根据关键词搜索生命周期记录
     */
    IPage<DataLifecycle> searchLifecycles(String keyword, Integer pageNum, Integer pageSize);

    /**
     * 根据阶段查询生命周期记录
     */
    IPage<DataLifecycle> listLifecyclesByStage(String stage, Integer pageNum, Integer pageSize);

    /**
     * 切换到下一生命周期阶段
     */
    DataLifecycle transitionToNextStage(Long lifecycleId);

    /**
     * 获取所有生命周期阶段
     */
    List<LifecycleStage> getAllStages();
}