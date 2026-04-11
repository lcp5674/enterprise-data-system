package com.enterprise.edams.notification.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.enterprise.edams.notification.entity.NotificationTemplate;
import java.util.List;

/**
 * 通知模板服务接口
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
public interface NotificationTemplateService {

    /** 分页查询模板 */
    IPage<NotificationTemplate> queryTemplates(String keyword, String channel, 
                                               Integer type, int pageNum, int pageSize);

    /** 获取所有启用的模板（用于下拉选择） */
    List<NotificationTemplate> getEnabledTemplates();

    /** 根据ID获取模板 */
    NotificationTemplate getById(Long id);

    /** 创建模板 */
    NotificationTemplate create(NotificationTemplate template, String operator);

    /** 更新模板 */
    void update(Long id, NotificationTemplate template, String operator);

    /** 删除模板 */
    void delete(Long id, String operator);

    /** 使用模板渲染内容（变量替换） */
    String renderTemplate(NotificationTemplate template, java.util.Map<String, Object> params);
}
