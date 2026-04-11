package com.enterprise.edams.notification.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.edams.common.exception.BusinessException;
import com.enterprise.edams.common.result.ResultCode;
import com.enterprise.edams.notification.dto.NotificationTemplateCreateRequest;
import com.enterprise.edams.notification.dto.NotificationTemplateVO;
import com.enterprise.edams.notification.entity.NotificationTemplate;
import com.enterprise.edams.notification.repository.NotificationTemplateRepository;
import com.enterprise.edams.notification.service.NotificationTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 通知模板服务实现
 *
 * @author Backend Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationTemplateServiceImpl implements NotificationTemplateService {

    private final NotificationTemplateRepository templateRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String TEMPLATE_CACHE_PREFIX = "edams:notification:template:";
    private static final long CACHE_EXPIRE_MINUTES = 60;

    @Override
    @Transactional
    public NotificationTemplateVO createTemplate(NotificationTemplateCreateRequest request) {
        log.info("创建通知模板: code={}, name={}", request.getCode(), request.getName());

        // 检查编码是否存在
        if (checkCodeExists(request.getCode())) {
            throw new BusinessException(ResultCode.RESOURCE_ALREADY_EXISTS, "模板编码已存在");
        }

        NotificationTemplate template = NotificationTemplate.builder()
                .code(request.getCode())
                .name(request.getName())
                .templateType(request.getTemplateType())
                .title(request.getTitle())
                .content(request.getContent())
                .variables(request.getVariables())
                .description(request.getDescription())
                .status(1)
                .createdBy(getCurrentUsername())
                .createdTime(LocalDateTime.now())
                .updatedBy(getCurrentUsername())
                .updatedTime(LocalDateTime.now())
                .isDeleted(0)
                .build();

        templateRepository.insert(template);

        return convertToVO(template);
    }

    @Override
    @Transactional
    public NotificationTemplateVO updateTemplate(String templateId, NotificationTemplateCreateRequest request) {
        log.info("更新通知模板: templateId={}", templateId);

        NotificationTemplate template = templateRepository.selectById(templateId);
        if (template == null) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND, "模板不存在");
        }

        // 更新字段
        if (request.getName() != null) {
            template.setName(request.getName());
        }
        if (request.getTitle() != null) {
            template.setTitle(request.getTitle());
        }
        if (request.getContent() != null) {
            template.setContent(request.getContent());
        }
        if (request.getVariables() != null) {
            template.setVariables(request.getVariables());
        }
        if (request.getDescription() != null) {
            template.setDescription(request.getDescription());
        }

        template.setUpdatedBy(getCurrentUsername());
        template.setUpdatedTime(LocalDateTime.now());

        templateRepository.updateById(template);

        // 清除缓存
        clearTemplateCache(template.getCode());

        return convertToVO(template);
    }

    @Override
    @Transactional
    public void deleteTemplate(String templateId) {
        log.info("删除通知模板: templateId={}", templateId);

        NotificationTemplate template = templateRepository.selectById(templateId);
        if (template == null) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND, "模板不存在");
        }

        // 清除缓存
        clearTemplateCache(template.getCode());

        // 逻辑删除
        template.setIsDeleted(1);
        template.setDeletedBy(getCurrentUsername());
        template.setDeletedTime(LocalDateTime.now());
        templateRepository.updateById(template);
    }

    @Override
    public NotificationTemplateVO getTemplateById(String templateId) {
        NotificationTemplate template = templateRepository.selectById(templateId);
        if (template == null) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND, "模板不存在");
        }
        return convertToVO(template);
    }

    @Override
    public NotificationTemplateVO getTemplateByCode(String code) {
        // 先从缓存获取
        String cacheKey = TEMPLATE_CACHE_PREFIX + code;
        NotificationTemplateVO cachedTemplate = (NotificationTemplateVO) redisTemplate.opsForValue().get(cacheKey);
        if (cachedTemplate != null) {
            return cachedTemplate;
        }

        NotificationTemplate template = templateRepository.findByCode(code);
        if (template == null) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND, "模板不存在");
        }

        NotificationTemplateVO vo = convertToVO(template);
        
        // 缓存结果
        redisTemplate.opsForValue().set(cacheKey, vo, CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES);

        return vo;
    }

    @Override
    public Page<NotificationTemplateVO> pageTemplates(String keyword, String templateType, Integer status, int pageNum, int pageSize) {
        LambdaQueryWrapper<NotificationTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NotificationTemplate::getIsDeleted, 0);

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(NotificationTemplate::getName, keyword)
                    .or().like(NotificationTemplate::getCode, keyword));
        }

        if (templateType != null && !templateType.isEmpty()) {
            wrapper.eq(NotificationTemplate::getTemplateType, templateType);
        }

        if (status != null) {
            wrapper.eq(NotificationTemplate::getStatus, status);
        }

        wrapper.orderByDesc(NotificationTemplate::getCreatedTime);

        Page<NotificationTemplate> page = new Page<>(pageNum, pageSize);
        Page<NotificationTemplate> result = templateRepository.selectPage(page, wrapper);

        Page<NotificationTemplateVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(result.getRecords().stream().map(this::convertToVO).collect(Collectors.toList()));

        return voPage;
    }

    @Override
    public List<NotificationTemplateVO> listEnabledTemplates() {
        LambdaQueryWrapper<NotificationTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NotificationTemplate::getIsDeleted, 0)
                .eq(NotificationTemplate::getStatus, 1)
                .orderByAsc(NotificationTemplate::getName);
        return templateRepository.selectList(wrapper).stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public boolean checkCodeExists(String code) {
        return templateRepository.existsByCode(code);
    }

    @Override
    @Transactional
    public void enableTemplate(String templateId) {
        NotificationTemplate template = templateRepository.selectById(templateId);
        if (template == null) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND, "模板不存在");
        }
        template.setStatus(1);
        template.setUpdatedBy(getCurrentUsername());
        template.setUpdatedTime(LocalDateTime.now());
        templateRepository.updateById(template);

        clearTemplateCache(template.getCode());
    }

    @Override
    @Transactional
    public void disableTemplate(String templateId) {
        NotificationTemplate template = templateRepository.selectById(templateId);
        if (template == null) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND, "模板不存在");
        }
        template.setStatus(0);
        template.setUpdatedBy(getCurrentUsername());
        template.setUpdatedTime(LocalDateTime.now());
        templateRepository.updateById(template);

        clearTemplateCache(template.getCode());
    }

    @Override
    public String renderTemplate(String templateCode, Map<String, String> variables) {
        NotificationTemplate template = templateRepository.findByCode(templateCode);
        if (template == null) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND, "模板不存在");
        }

        String result = template.getContent();
        if (variables != null) {
            for (Map.Entry<String, String> entry : variables.entrySet()) {
                result = result.replace("${" + entry.getKey() + "}", entry.getValue() != null ? entry.getValue() : "");
            }
        }
        return result;
    }

    // ========== 私有方法 ==========

    private NotificationTemplateVO convertToVO(NotificationTemplate template) {
        NotificationTemplateVO vo = new NotificationTemplateVO();
        BeanUtils.copyProperties(template, vo);
        return vo;
    }

    private void clearTemplateCache(String code) {
        String cacheKey = TEMPLATE_CACHE_PREFIX + code;
        redisTemplate.delete(cacheKey);
    }

    private String getCurrentUsername() {
        try {
            return org.springframework.security.core.SecurityContextHolder.getContext()
                    .getAuthentication().getName();
        } catch (Exception e) {
            return "system";
        }
    }
}
