package com.enterprise.edams.report.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.enterprise.edams.report.dto.ReportTemplateCreateRequest;
import com.enterprise.edams.report.dto.ReportTemplateDTO;

import java.util.List;

/**
 * 报表模板服务接口
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
public interface ReportTemplateService {

    /**
     * 创建模板
     */
    ReportTemplateDTO createTemplate(ReportTemplateCreateRequest request, Long creatorId, String creatorName);

    /**
     * 更新模板
     */
    ReportTemplateDTO updateTemplate(Long id, ReportTemplateCreateRequest request);

    /**
     * 删除模板
     */
    void deleteTemplate(Long id);

    /**
     * 获取模板详情
     */
    ReportTemplateDTO getTemplateById(Long id);

    /**
     * 根据编码获取模板
     */
    ReportTemplateDTO getTemplateByCode(String templateCode);

    /**
     * 分页查询模板
     */
    IPage<ReportTemplateDTO> queryTemplates(String keyword, String templateType, Integer status, int pageNum, int pageSize);

    /**
     * 根据类型查询模板
     */
    List<ReportTemplateDTO> getTemplatesByType(String templateType);

    /**
     * 获取热门模板
     */
    List<ReportTemplateDTO> getHotTemplates(int limit);

    /**
     * 搜索模板
     */
    List<ReportTemplateDTO> searchTemplates(String keyword, int limit);

    /**
     * 启用模板
     */
    void enableTemplate(Long id);

    /**
     * 禁用模板
     */
    void disableTemplate(Long id);

    /**
     * 增加使用次数
     */
    void incrementUsageCount(Long id);

    /**
     * 上传模板文件
     */
    ReportTemplateDTO uploadTemplate(ReportTemplateCreateRequest request, byte[] fileData, Long creatorId, String creatorName);

    /**
     * 下载模板文件
     */
    byte[] downloadTemplate(Long id);

    /**
     * 统计模板总数
     */
    long countTotalTemplates();

    /**
     * 克隆模板
     */
    ReportTemplateDTO cloneTemplate(Long id, String newTemplateName, Long creatorId, String creatorName);
}
