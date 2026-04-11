package com.enterprise.edams.notification.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.edams.common.exception.BusinessException;
import com.enterprise.edams.notification.entity.NotificationTemplate;
import com.enterprise.edams.notification.repository.NotificationTemplateMapper;
import com.enterprise.edams.notification.service.NotificationTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * 通知模板服务实现
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationTemplateServiceImpl implements NotificationTemplateService {

    private final NotificationTemplateMapper templateMapper;

    @Override
    public IPage<NotificationTemplate> queryTemplates(String keyword, String channel,
                                                      Integer type, int pageNum, int pageSize) {
        Page<NotificationTemplate> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<NotificationTemplate> wrapper = new LambdaQueryWrapper<>();

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(NotificationTemplate::getName, keyword)
                    .or().like(NotificationTemplate::getCode, keyword)
                    .or().like(NotificationTemplate::getSubject, keyword));
        }
        if (channel != null) wrapper.eq(NotificationTemplate::getChannel, channel);
        if (type != null) wrapper.eq(NotificationTemplate::getType, type);

        wrapper.orderByDesc(NotificationTemplate::getCreatedTime);
        return templateMapper.selectPage(page, wrapper);
    }

    @Override
    public List<NotificationTemplate> getEnabledTemplates() {
        LambdaQueryWrapper<NotificationTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NotificationTemplate::getStatus, 1).orderByAsc(NotificationTemplate::getCode);
        return templateMapper.selectList(wrapper);
    }

    @Override
    public NotificationTemplate getById(Long id) {
        NotificationTemplate t = templateMapper.selectById(id);
        if (t == null || t.getDeleted() == 1) throw new BusinessException("模板不存在");
        return t;
    }

    /** 根据编码获取模板 */
    public NotificationTemplate getByCode(String code) {
        return templateMapper.findByCode(code);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public NotificationTemplate create(NotificationTemplate template, String operator) {
        // 检查编码唯一性（简化：通过查询检查）
        if (templateMapper.findByCode(template.getCode()) != null) {
            throw new BusinessException("模板编码已存在: " + template.getCode());
        }
        template.setStatus(1);
        template.setCreatedBy(operator);
        templateMapper.insert(template);
        log.info("通知模板创建成功: {} ({})", template.getName(), template.getId());
        return template;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, NotificationTemplate template, String operator) {
        NotificationTemplate existing = templateMapper.selectById(id);
        if (existing == null || existing.getDeleted() == 1) {
            throw new BusinessException("模板不存在");
        }
        existing.setName(template.getName());
        existing.setCode(template.getCode());
        existing.setChannel(template.getChannel());
        existing.setType(template.getType());
        existing.setSubject(template.getSubject());
        existing.setContentTemplate(template.getContentTemplate());
        existing.setDescription(template.getDescription());
        existing.setStatus(template.getStatus());
        existing.setUpdatedBy(operator);
        templateMapper.updateById(existing);
        log.info("通知模板更新成功: {}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id, String operator) {
        NotificationTemplate t = templateMapper.selectById(id);
        if (t == null || t.getDeleted() == 1) throw new BusinessException("模板不存在");
        t.setDeleted(1);
        t.setUpdatedBy(operator);
        templateMapper.updateById(t);
        log.info("通知模板已删除: {}", id);
    }

    @Override
    public String renderTemplate(NotificationTemplate template, Map<String, Object> params) {
        String content = template.getContentTemplate();
        if (content == null) content = "";
        
        if (params != null) {
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                content = content.replace("${" + entry.getKey() + "}",
                        entry.getValue() != null ? entry.getValue().toString() : "");
            }
        }
        return content;
    }
}
