package com.enterprise.edams.report.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.edams.report.dto.ReportTemplateCreateRequest;
import com.enterprise.edams.report.dto.ReportTemplateDTO;
import com.enterprise.edams.report.entity.ReportTemplate;
import com.enterprise.edams.report.repository.ReportTemplateMapper;
import com.enterprise.edams.report.service.ReportTemplateService;
import com.enterprise.edams.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 报表模板服务实现
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportTemplateServiceImpl implements ReportTemplateService {

    private final ReportTemplateMapper templateMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReportTemplateDTO createTemplate(ReportTemplateCreateRequest request, Long creatorId, String creatorName) {
        // 校验名称
        if (templateMapper.selectByTemplateName(request.getTemplateName()) != null) {
            throw new BusinessException("模板名称已存在: " + request.getTemplateName());
        }

        // 生成编码
        String templateCode = StringUtils.isNotBlank(request.getTemplateCode()) 
                ? request.getTemplateCode() 
                : generateTemplateCode();
        
        if (templateMapper.existsByTemplateCode(templateCode) > 0) {
            throw new BusinessException("模板编码已存在: " + templateCode);
        }

        ReportTemplate template = new ReportTemplate();
        BeanUtils.copyProperties(request, template);
        template.setTemplateCode(templateCode);
        template.setCreatorId(creatorId);
        template.setCreatorName(creatorName);
        template.setStatus(1);
        template.setUsageCount(0);

        templateMapper.insert(template);
        log.info("报表模板创建成功: id={}, name={}", template.getId(), template.getTemplateName());
        return convertToDTO(template);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReportTemplateDTO updateTemplate(Long id, ReportTemplateCreateRequest request) {
        ReportTemplate template = templateMapper.selectById(id);
        if (template == null) {
            throw new BusinessException("模板不存在: " + id);
        }

        // 校验名称唯一性
        ReportTemplate existTemplate = templateMapper.selectByTemplateName(request.getTemplateName());
        if (existTemplate != null && !existTemplate.getId().equals(id)) {
            throw new BusinessException("模板名称已存在: " + request.getTemplateName());
        }

        template.setTemplateName(request.getTemplateName());
        template.setDescription(request.getDescription());
        template.setLayoutConfig(request.getLayoutConfig());
        template.setDataBinding(request.getDataBinding());
        template.setSupportedFormats(request.getSupportedFormats());

        templateMapper.updateById(template);
        log.info("报表模板更新成功: id={}", id);
        return convertToDTO(template);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTemplate(Long id) {
        ReportTemplate template = templateMapper.selectById(id);
        if (template == null) {
            throw new BusinessException("模板不存在: " + id);
        }
        templateMapper.deleteById(id);
        log.info("报表模板删除成功: id={}", id);
    }

    @Override
    public ReportTemplateDTO getTemplateById(Long id) {
        ReportTemplate template = templateMapper.selectById(id);
        if (template == null) {
            throw new BusinessException("模板不存在: " + id);
        }
        return convertToDTO(template);
    }

    @Override
    public ReportTemplateDTO getTemplateByCode(String templateCode) {
        ReportTemplate template = templateMapper.selectByTemplateCode(templateCode);
        if (template == null) {
            throw new BusinessException("模板不存在: " + templateCode);
        }
        return convertToDTO(template);
    }

    @Override
    public IPage<ReportTemplateDTO> queryTemplates(String keyword, String templateType, Integer status, int pageNum, int pageSize) {
        Page<ReportTemplate> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ReportTemplate> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(ReportTemplate::getTemplateName, keyword)
                    .or().like(ReportTemplate::getTemplateCode, keyword));
        }
        if (StringUtils.isNotBlank(templateType)) {
            wrapper.eq(ReportTemplate::getTemplateType, templateType);
        }
        if (status != null) {
            wrapper.eq(ReportTemplate::getStatus, status);
        }

        wrapper.orderByDesc(ReportTemplate::getUsageCount)
               .orderByDesc(ReportTemplate::getCreatedTime);
        
        IPage<ReportTemplate> templatePage = templateMapper.selectPage(page, wrapper);
        List<ReportTemplateDTO> dtoList = templatePage.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        Page<ReportTemplateDTO> resultPage = new Page<>(templatePage.getCurrent(), templatePage.getSize(), templatePage.getTotal());
        resultPage.setRecords(dtoList);
        return resultPage;
    }

    @Override
    public List<ReportTemplateDTO> getTemplatesByType(String templateType) {
        return templateMapper.selectByTemplateType(templateType).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReportTemplateDTO> getHotTemplates(int limit) {
        return templateMapper.selectHotTemplates(limit).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReportTemplateDTO> searchTemplates(String keyword, int limit) {
        return templateMapper.searchByKeyword(keyword, limit).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void enableTemplate(Long id) {
        templateMapper.updateStatus(id, 1);
        log.info("模板启用成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void disableTemplate(Long id) {
        templateMapper.updateStatus(id, 0);
        log.info("模板禁用成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void incrementUsageCount(Long id) {
        templateMapper.incrementUsageCount(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReportTemplateDTO uploadTemplate(ReportTemplateCreateRequest request, byte[] fileData, Long creatorId, String creatorName) {
        // TODO: 实际文件上传逻辑
        log.info("上传模板文件: name={}, size={}", request.getTemplateName(), fileData.length);
        return createTemplate(request, creatorId, creatorName);
    }

    @Override
    public byte[] downloadTemplate(Long id) {
        ReportTemplate template = templateMapper.selectById(id);
        if (template == null) {
            throw new BusinessException("模板不存在: " + id);
        }
        
        // TODO: 实际文件下载逻辑
        log.info("下载模板文件: id={}", id);
        return new byte[0];
    }

    @Override
    public long countTotalTemplates() {
        return templateMapper.countTotal();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReportTemplateDTO cloneTemplate(Long id, String newTemplateName, Long creatorId, String creatorName) {
        ReportTemplate source = templateMapper.selectById(id);
        if (source == null) {
            throw new BusinessException("原模板不存在: " + id);
        }

        ReportTemplateCreateRequest request = new ReportTemplateCreateRequest();
        request.setTemplateName(newTemplateName);
        request.setTemplateType(source.getTemplateType());
        request.setDescription(source.getDescription());
        request.setFilePath(source.getFilePath());
        request.setFileName(source.getFileName());
        request.setContentType(source.getContentType());
        request.setLayoutConfig(source.getLayoutConfig());
        request.setDataBinding(source.getDataBinding());
        request.setSupportedFormats(source.getSupportedFormats());

        return createTemplate(request, creatorId, creatorName);
    }

    private String generateTemplateCode() {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String random = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return "TPL" + dateStr + random;
    }

    private ReportTemplateDTO convertToDTO(ReportTemplate template) {
        ReportTemplateDTO dto = new ReportTemplateDTO();
        BeanUtils.copyProperties(template, dto);
        return dto;
    }
}
