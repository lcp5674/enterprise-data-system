package com.enterprise.edams.notification.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.edams.common.result.PageResult;
import com.enterprise.edams.common.result.Result;
import com.enterprise.edams.notification.dto.NotificationTemplateCreateRequest;
import com.enterprise.edams.notification.dto.NotificationTemplateVO;
import com.enterprise.edams.notification.service.NotificationTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 通知模板控制器
 *
 * @author Backend Team
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/notification-templates")
@RequiredArgsConstructor
@Tag(name = "通知模板管理", description = "通知模板相关接口")
public class TemplateController {

    private final NotificationTemplateService templateService;

    @PostMapping
    @Operation(summary = "创建模板", description = "创建通知模板")
    public Result<NotificationTemplateVO> createTemplate(@Valid @RequestBody NotificationTemplateCreateRequest request) {
        NotificationTemplateVO template = templateService.createTemplate(request);
        return Result.success(template);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新模板", description = "更新通知模板")
    public Result<NotificationTemplateVO> updateTemplate(
            @Parameter(description = "模板ID") @PathVariable String id,
            @Valid @RequestBody NotificationTemplateCreateRequest request) {
        NotificationTemplateVO template = templateService.updateTemplate(id, request);
        return Result.success(template);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除模板", description = "删除通知模板")
    public Result<Void> deleteTemplate(
            @Parameter(description = "模板ID") @PathVariable String id) {
        templateService.deleteTemplate(id);
        return Result.success();
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取模板详情", description = "根据ID获取模板详情")
    public Result<NotificationTemplateVO> getTemplateById(
            @Parameter(description = "模板ID") @PathVariable String id) {
        NotificationTemplateVO template = templateService.getTemplateById(id);
        return Result.success(template);
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "根据编码获取模板", description = "根据模板编码获取模板")
    public Result<NotificationTemplateVO> getTemplateByCode(
            @Parameter(description = "模板编码") @PathVariable String code) {
        NotificationTemplateVO template = templateService.getTemplateByCode(code);
        return Result.success(template);
    }

    @GetMapping
    @Operation(summary = "分页查询模板", description = "分页查询模板列表")
    public Result<PageResult<NotificationTemplateVO>> pageTemplates(
            @Parameter(description = "关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "模板类型") @RequestParam(required = false) String templateType,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "20") int pageSize) {
        Page<NotificationTemplateVO> page = templateService.pageTemplates(keyword, templateType, status, pageNum, pageSize);
        return Result.success(PageResult.of(page));
    }

    @GetMapping("/enabled")
    @Operation(summary = "获取所有启用的模板", description = "获取所有启用的模板列表")
    public Result<List<NotificationTemplateVO>> listEnabledTemplates() {
        List<NotificationTemplateVO> templates = templateService.listEnabledTemplates();
        return Result.success(templates);
    }

    @GetMapping("/check/code")
    @Operation(summary = "检查模板编码是否存在", description = "检查模板编码是否可用")
    public Result<Boolean> checkCodeExists(
            @Parameter(description = "模板编码") @RequestParam String code) {
        boolean exists = templateService.checkCodeExists(code);
        return Result.success(!exists);
    }

    @PutMapping("/{id}/enable")
    @Operation(summary = "启用模板", description = "启用通知模板")
    public Result<Void> enableTemplate(
            @Parameter(description = "模板ID") @PathVariable String id) {
        templateService.enableTemplate(id);
        return Result.success();
    }

    @PutMapping("/{id}/disable")
    @Operation(summary = "禁用模板", description = "禁用通知模板")
    public Result<Void> disableTemplate(
            @Parameter(description = "模板ID") @PathVariable String id) {
        templateService.disableTemplate(id);
        return Result.success();
    }

    @PostMapping("/render")
    @Operation(summary = "渲染模板", description = "渲染模板内容")
    public Result<String> renderTemplate(
            @Parameter(description = "模板编码") @RequestParam String templateCode,
            @RequestBody Map<String, String> variables) {
        String content = templateService.renderTemplate(templateCode, variables);
        return Result.success(content);
    }
}
