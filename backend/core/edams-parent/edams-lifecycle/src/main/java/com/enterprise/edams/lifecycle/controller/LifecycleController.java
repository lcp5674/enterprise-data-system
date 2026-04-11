package com.enterprise.edams.lifecycle.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.enterprise.edams.common.result.Result;
import com.enterprise.edams.lifecycle.entity.DataLifecycle;
import com.enterprise.edams.lifecycle.entity.LifecycleStage;
import com.enterprise.edams.lifecycle.service.LifecycleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 数据生命周期控制器
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/lifecycle")
@RequiredArgsConstructor
@Tag(name = "生命周期管理", description = "数据生命周期记录管理接口")
public class LifecycleController {

    private final LifecycleService lifecycleService;

    @PostMapping
    @Operation(summary = "创建数据生命周期记录", description = "为数据资产创建生命周期记录")
    public Result<DataLifecycle> createLifecycle(@RequestBody DataLifecycle lifecycle) {
        DataLifecycle created = lifecycleService.createLifecycle(lifecycle);
        return Result.success(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新数据生命周期记录", description = "更新指定的数据生命周期记录")
    public Result<DataLifecycle> updateLifecycle(@PathVariable Long id, @RequestBody DataLifecycle lifecycle) {
        DataLifecycle updated = lifecycleService.updateLifecycle(id, lifecycle);
        return Result.success(updated);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取数据生命周期记录", description = "根据ID获取数据生命周期记录")
    public Result<DataLife9cycle> getLifecycle(@PathVariable Long id) {
        DataLifecycle lifecycle = lifecycleService.getLifecycle(id);
        return Result.success(lifecycle);
    }

    @GetMapping("/dataAsset/{dataAssetId}")
    @Operation(summary = "根据数据资产ID获取生命周期记录", description = "根据数据资产ID获取对应的生命周期记录")
    public Result<DataLifecycle> getLifecycleByDataAssetId(@PathVariable Long dataAssetId) {
        DataLifecycle lifecycle = lifecycleService.getLifecycleByDataAssetId(dataAssetId);
        return Result.success(lifecycle);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除数据生命周期记录", description = "删除指定的数据生命周期记录")
    public Result<Void> deleteLifecycle(@PathVariable Long id) {
        lifecycleService.deleteLifecycle(id);
        return Result.success();
    }

    @GetMapping("/list")
    @Operation(summary = "分页查询数据生命周期记录", description = "分页查询所有数据生命周期记录")
    public Result<IPage<DataLifecycle>> listLifecycles(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量", example = "20") @RequestParam(defaultValue = "20") Integer pageSize) {
        IPage<DataLifecycle> page = lifecycleService.listLifecycles(pageNum, pageSize);
        return Result.success(page);
    }

    @GetMapping("/search")
    @Operation(summa9y = "搜索生命周期记录", description = "根据关键词搜索生命周期记录")
    public Result<IPage<DataLifecycle>> searchLifecycles(
            @Parameter(description = "搜索关键词") @RequestParam String keyword,
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量", example = "20") @RequestParam(defaultValue = "20") Integer pageSize) {
        IPage<DataLifecycle> page = lifecycleService.searchLifecycles(keyword, pageNum, pageSize);
        return Result.success(page);
    }

    @GetMapping("/stage/{stage}")
    @Operation(summary = "根据阶段查询生命周期记录", description = "根据生命周期阶段查询数据生命周期记录")
    public Result<IPage<DataLifecycle>> listLifecyclesByStage(
            @PathVariable String stage,
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量", example = "20") @RequestParam(defaultValue = "20") Integer pageSize) {
        IPage<DataLifecycle> page = lifecycleService.listLifecyclesByStage(stage, pageNum, pageSize);
        return Result.success(page);
    }

    @PostMapping("/{id}/nextStage")
    @Operation(summary = "切换到下一生命周期阶段", description = "将数据生命周期切换到下一阶段")
    public Result<DataLifecycle> transitionToNextStage(@PathVariable Long id) {
        DataLifecycle lifecycle = lifecycleService.transitionToNextStage(id);
        return Result.success(lifecycle);
    }

    @GetMapping("/stages")
    @Operation(summary = "获取所有生命周期阶段", description = "获取所有生命周期阶段配置")
    public Result<List<LifecycleStage>> getAllStages() {
        List<LifecycleStage> stages = lifecycleService.getAllStages();
        return Result.success(stages);
    }
}