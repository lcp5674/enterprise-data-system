package com.enterprise.edams.value.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.enterprise.edams.value.entity.ValueAssessment;
import com.enterprise.edams.value.dto.*;

import java.util.List;
import java.util.Map;

/**
 * 数据价值评估服务接口
 */
public interface ValueAssessmentService extends IService<ValueAssessment> {
    
    /**
     * 创建评估任务
     */
    ValueAssessment createAssessment(Long assetId);
    
    /**
     * 执行评估
     */
    ValueAssessment executeAssessment(Long assessmentId);
    
    /**
     * 获取评估详情
     */
    ValueAssessment getAssessmentById(Long id);
    
    /**
     * 分页查询评估记录
     */
    Page<ValueAssessment> searchAssessments(AssessmentSearchRequest request);
    
    /**
     * 获取资产的最新评估
     */
    ValueAssessment getLatestAssessment(Long assetId);
    
    /**
     * 批量评估
     */
    List<ValueAssessment> batchAssess(List<Long> assetIds);
    
    /**
     * 获取价值分布统计
     */
    Map<String, Object> getValueDistribution();
    
    /**
     * 获取TOP价值资产
     */
    List<ValueAssessment> getTopValueAssets(int topN);
}
