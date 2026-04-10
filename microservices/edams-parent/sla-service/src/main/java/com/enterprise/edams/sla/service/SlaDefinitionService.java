package com.enterprise.edams.sla.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.enterprise.edams.sla.entity.SlaDefinition;
import com.enterprise.edams.sla.dto.*;

import java.util.List;
import java.util.Map;

/**
 * SLA定义服务接口
 */
public interface SlaDefinitionService extends IService<SlaDefinition> {
    
    /**
     * 创建SLA定义
     */
    SlaDefinition createSla(SlaCreateRequest request);
    
    /**
     * 更新SLA定义
     */
    SlaDefinition updateSla(Long id, SlaUpdateRequest request);
    
    /**
     * 删除SLA定义
     */
    void deleteSla(Long id);
    
    /**
     * 获取SLA详情
     */
    SlaDefinition getSlaById(Long id);
    
    /**
     * 分页查询SLA
     */
    Page<SlaDefinition> searchSlas(SlaSearchRequest request);
    
    /**
     * 启用SLA
     */
    void enableSla(Long id);
    
    /**
     * 停用SLA
     */
    void disableSla(Long id);
    
    /**
     * 获取SLA统计
     */
    SlaStatistics getSlaStatistics();
    
    /**
     * 获取SLA达标率
     */
    SlaCompliance getSlaCompliance(Long slaId);
}
