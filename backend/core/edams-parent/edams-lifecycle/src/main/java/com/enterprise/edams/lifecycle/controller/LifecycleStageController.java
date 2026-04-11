package com.enterprise.edams.lifecycle.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.enterprise.edams.common.result.Result;
import com.enterprise.edams.lifecycle.entity.LifecycleStage;
import com.enterprise.edams.lifecycle.service.LifecycleStageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 生命周期阶段控制器
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/lifecycle/stage")
@RequiredArgsConstructor
@Tag(name = "生命周期阶段管理", description = "生命周期阶段配置管理接口")
public class LifecycleStageController {

    private final LifecycleStageService lifecycleStageService;

    @PostMapping
    @Operation(summary = "创建生命周期阶段", description = "创建新的生命周期阶段配置")
    public Result<LifecycleStage> createStage(@RequestBody LifecycleStage stage) {
        LifecycleStage created = lifecycleStageService.createStage(stage);
        return Result.success(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新生命周期阶段", description = "更新指定的生命周期阶段配置")
    public Result<LifecycleStage> updateStage(@PathVariable Long id, @RequestBody LifecycleStage stage) {
        LifecycleStage updated = lifecycleStageService.updateStage(id, stage);
        return Result.success(updated);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取生命周期阶段", description = "根据ID获取生命周期阶段配置")
    public Result<LifecycleStage> getStage(@PathVariable Long id) {
        LifecycleStage stage = lifecycleStageService.getStage(id);
        return Result.success(stage);
    }

    @GetMapping("/code/{stageCode}")
    @Operation(summary = "根据编码获取生命周期阶段", description = "根据阶段编码获取生命周期阶段配置")
    public Result<LifecycleStage> getStageByCode(@PathVariable String stageCode) {
        LifecycleStage stage = lifecycleStageService.getStageByCode(stageCode);
        return Result.success(stage);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除生命周期阶段", description = "删除指定的生命周期阶段配置")
    public Result<Void> deleteStage(@PathVariable Long id) {
        lifecycleStageService.deleteStage(id);
        return Result.success();
    }

    @GetMapping("/list")
    @Operation(summary = "分页查询生命周期阶段", description = "分页查询所有生命周期阶段配置")
    public Result<IPage<LifecycleStage>> listStages(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量", example = "20") @RequestParam(defaultValue = "20") Integer pageSize) {
        IPage<LifecycleStage> page = lifecycleStageService.listStages(pageNum, pageSize);
        return Result.success(page);
    }

    @GetMapping("/enabled")
    @Operation(summary = "获取所有启用的生命周期阶段", description = "获取所有启用的生命周期阶段配置")
    public Result<List<LifecycleStage>> getAllEnabledStages() {
        List<LifecycleStage> stages = lifecycleStageService.getAllEnabledStages();
        return Result.success(stages);
    }

    @PostMapping("/{id}/enable")
    @Operation(summary = "启用生命周期阶段", description = "启用指定的生命周期阶段")
    public Result<LifecycleStage> enableStage(@PathVariable Long id) {
        LifecycleStage stage = lifecycleStageService.enableStage(id);
        return Result.success(stage);
    }

    @PostMapping("/{id}/disable")
    @Operation(summary = "禁用生命周期阶段", description = "禁用指定的生命周期阶段")
    public Result<LifecycleStage> disableStage(@PathVariable Long id) {
        LifecycleStage stage = lifecycleStageService.disableStage(id);
        return Result.success(stage);
    }
}