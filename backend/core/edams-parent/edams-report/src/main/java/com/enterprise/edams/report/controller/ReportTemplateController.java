package com.enterprise.edams.report.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.enterprise.edams.report.dto.ReportTemplateCreateRequest;
import com.enterprise.edams.report.dto.ReportTemplateDTO;
import com.enterprise.edams.report.service.ReportTemplateService;
import com.enterprise.edams.common.result.PageResult;
import com.enterprise.edams.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 报表模板控制器
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/report-templates")
@RequiredArgsConstructor
@Tag(name = "报表模板管理", description = "报表模板的CRUD、上传下载等接口")
public class ReportTemplateController {

    private final ReportTemplateService templateService;

    @PostMapping
    @Operation(summary = "创建模板")
    public Result<ReportTemplateDTO> createTemplate(@Valid @RequestBody ReportTemplateCreateRequest request) {
        ReportTemplateDTO template = templateService.createTemplate(request, getCurrentUserId(), getCurrentUser());
        return Result.success(template);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新模板")
    public Result<ReportTemplateDTO> updateTemplate(
            @PathVariable Long id,
            @Valid @RequestBody ReportTemplateCreateRequest request) {
        ReportTemplateDTO template = templateService.updateTemplate(id, request);
        return Result.success(template);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除模板")
    public Result<Void> deleteTemplate(@PathVariable Long id) {
        templateService.deleteTemplate(id);
        return Result.success();
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取模板详情")
    public Result<ReportTemplateDTO> getTemplateById(@PathVariable Long id) {
        ReportTemplateDTO template = templateService.getTemplateById(id);
        return Result.success(template);
    }

    @GetMapping("/code/{templateCode}")
    @Operation(summary = "根据编码获取模板")
    public Result<ReportTemplateDTO> getTemplateByCode(@PathVariable String templateCode) {
        ReportTemplateDTO template = templateService.getTemplateByCode(templateCode);
        return Result.success(template);
    }

    @GetMapping("/list")
    @Operation(summary = "分页查询模板")
    public PageResult<ReportTemplateDTO> queryTemplates(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String templateType,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        IPage<ReportTemplateDTO> page = templateService.queryTemplates(keyword, templateType, status, pageNum, pageSize);
        return PageResult.success(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize());
    }

    @GetMapping("/type/{templateType}")
    @Operation(summary = "根据类型查询模板")
    public Result<List<ReportTemplateDTO>> getTemplatesByType(@PathVariable String templateType) {
        List<ReportTemplateDTO> templates = templateService.getTemplatesByType(templateType);
        return Result.success(templates);
    }

    @GetMapping("/hot")
    @Operation(summary = "获取热门模板")
    public Result<List<ReportTemplateDTO>> getHotTemplates(
            @RequestParam(defaultValue = "10") int limit) {
        List<ReportTemplateDTO> templates = templateService.getHotTemplates(limit);
        return Result.success(templates);
    }

    @GetMapping("/search")
    @Operation(summary = "搜索模板")
    public Result<List<ReportTemplateDTO>> searchTemplates(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "10") int limit) {
        List<ReportTemplateDTO> templates = templateService.searchTemplates(keyword, limit);
        return Result.success(templates);
    }

    @PutMapping("/{id}/enable")
    @Operation(summary = "启用模板")
    public Result<Void> enableTemplate(@PathVariable Long id) {
        templateService.enableTemplate(id);
        return Result.success();
    }

    @PutMapping("/{id}/disable")
    @Operation(summary = "禁用模板")
    public Result<Void> disableTemplate(@PathVariable Long id) {
        templateService.disableTemplate(id);
        return Result.success();
    }

    @PostMapping("/{id}/clone")
    @Operation(summary = "克隆模板")
    public Result<ReportTemplateDTO> cloneTemplate(
            @PathVariable Long id,
            @RequestParam String newTemplateName) {
        ReportTemplateDTO template = templateService.cloneTemplate(id, newTemplateName, getCurrentUserId(), getCurrentUser());
        return Result.success(template);
    }

    @GetMapping("/{id}/download")
    @Operation(summary = "下载模板文件")
    public Result<byte[]> downloadTemplate(@PathVariable Long id) {
        byte[] data = templateService.downloadTemplate(id);
        return Result.success(data);
    }

    @GetMapping("/count")
    @Operation(summary = "统计模板总数")
    public Result<Long> countTotalTemplates() {
        long count = templateService.countTotalTemplates();
        return Result.success(count);
    }

    private Long getCurrentUserId() {
        return 1L;
    }

    private String getCurrentUser() {
        return "system";
    }
}
