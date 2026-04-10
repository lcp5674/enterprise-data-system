package com.enterprise.edams.notification.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.edams.notification.dto.NotificationTemplateCreateRequest;
import com.enterprise.edams.notification.dto.NotificationTemplateVO;

/**
 * 通知模板服务接口
 *
 * @author Backend Team
 * @version 1.0.0
 */
public interface NotificationTemplateService {

    /**
     * 创建模板
     */
    NotificationTemplateVO createTemplate(NotificationTemplateCreateRequest request);

    /**
     * 更新模板
     */
    NotificationTemplateVO updateTemplate(String templateId, NotificationTemplateCreateRequest request);

    /**
     * 删除模板
     */
    void deleteTemplate(String templateId);

    /**
     * 根据ID获取模板
     */
    NotificationTemplateVO getTemplateById(String templateId);

    /**
     * 根据编码获取模板
     */
    NotificationTemplateVO getTemplateByCode(String code);

    /**
     * 分页查询模板
     */
    Page<NotificationTemplateVO> pageTemplates(String keyword, String templateType, Integer status, int pageNum, int pageSize);

    /**
     * 获取所有启用的模板
     */
    java.util.List<NotificationTemplateVO> listEnabledTemplates();

    /**
     * 检查模板编码是否存在
     */
    boolean checkCodeExists(String code);

    /**
     * 启用模板
     */
    void enableTemplate(String templateId);

    /**
     * 禁用模板
     */
    void disableTemplate(String templateId);

    /**
     * 渲染模板内容
     */
    String renderTemplate(String templateCode, java.util.Map<String, String> variables);
}
