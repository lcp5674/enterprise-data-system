package com.enterprise.edams.watermark.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.enterprise.edams.watermark.dto.*;
import com.enterprise.edams.watermark.entity.*;

import java.util.List;

/**
 * 水印服务接口
 */
public interface WatermarkService extends IService<WatermarkTemplate> {
    
    /**
     * 添加水印到文件
     */
    WatermarkRecord addWatermark(AddWatermarkRequest request);
    
    /**
     * 批量添加水印
     */
    List<WatermarkRecord> batchAddWatermark(BatchWatermarkRequest request);
    
    /**
     * 获取水印记录
     */
    WatermarkRecord getRecord(Long id);
    
    /**
     * 查询水印记录
     */
    Page<WatermarkRecord> searchRecords(RecordSearchRequest request);
    
    /**
     * 创建水印模板
     */
    WatermarkTemplate createTemplate(TemplateCreateRequest request);
    
    /**
     * 更新水印模板
     */
    WatermarkTemplate updateTemplate(Long id, TemplateUpdateRequest request);
    
    /**
     * 获取模板列表
     */
    List<WatermarkTemplate> getTemplates(WatermarkType type);
    
    /**
     * 泄露溯源
     */
    LeakTrace traceLeakage(String fileName, String watermarkContent);
    
    /**
     * 查询溯源记录
     */
    Page<LeakTrace> searchLeakTraces(LeakTraceSearchRequest request);
    
    /**
     * 获取溯源统计
     */
    LeakStatistics getStatistics();
}
