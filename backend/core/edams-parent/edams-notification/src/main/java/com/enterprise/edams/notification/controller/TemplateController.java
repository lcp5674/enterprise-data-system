package com.enterprise.edams.notification.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.enterprise.edams.common.result.PageResult;
import com.enterprise.edams.common.result.Result;
import com.enterprise.edams.notification.entity.NotificationTemplate;
import com.enterprise.edams.notification.service.NotificationTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 通知模板管理控制器
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/notification-templates")
@RequiredArgsConstructor
@Tag(name = "通知模板", description = "通知模板CRUD接口")
public class TemplateController {

    private final NotificationTemplateService templateService;

    @GetMapping
    @Operation(summary = "分页查询模板")
    public PageResult<NotificationTemplate> queryTemplates(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String channel,
            @RequestParam(required = false) Integer type,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        IPage<NotificationTemplate> page = templateService.queryTemplates(keyword, channel, type, pageNum, pageSize);
        return PageResult.success(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize());
    }

    @GetMapping("/enabled")
    @Operation(summary = "获取所有启用的模板")
    public Result<List<NotificationTemplate>> getEnabled() {
        return Result.success(templateService.getEnabledTemplates());
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取模板详情")
    public Result<NotificationTemplate> getById(@PathVariable Long id) {
        return Result.success(templateService.getById(id));
    }

    @PostMapping
    @Operation(summary = "创建模板")
    public Result<NotificationTemplate> create(@Valid @RequestBody NotificationTemplate template) {
        return Result.success(templateService.create(template, "system"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新模板")
    public Result<Void> update(@PathVariable Long id,
                               @Valid @RequestBody NotificationTemplate template) {
        templateService.update(id, template, "system");
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除模板")
    public Result<Void> delete(@PathVariable Long id) {
        templateService.delete(id, "system");
        return Result.success();
    }
}
