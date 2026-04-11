package com.enterprise.edams.lifecycle.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.enterprise.edams.common.result.Result;
import com.enterprise.edams.lifecycle.entity.ArchiveRecord;
import com.enterprise.edams.lifecycle.service.ArchiveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 归档控制器
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/lifecycle/archive")
@RequiredArgsConstructor
@Tag(name = "归档管理", description = "数据归档记录管理接口")
public class ArchiveController {

    private final ArchiveService archiveService;

    @PostMapping
    @Operation(summary = "创建归档记录", description = "创建数据归档记录")
    public Result<ArchiveRecord> createArchiveRecord(@RequestBody ArchiveRecord archiveRecord) {
        ArchiveRecord created = archiveService.createArchiveRecord(archiveRecord);
        return Result.success(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新归档记录", description = "更新指定的归档记录")
    public Result<ArchiveRecord> updateArchiveRecord(@PathVariable Long id, @RequestBody ArchiveRecord archiveRecord) {
        ArchiveRecord updated = archiveService.updateArchiveRecord(id, archiveRecord);
        return Result.success(updated);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取归档记录", description = "根据ID获取归档记录")
    public Result<ArchiveRecord> getArchiveRecord(@PathVariable Long id) {
        ArchiveRecord record = archiveService.getArchiveRecord(id);
        return Result.success(record);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除归档记录", description = "删除指定的归档记录")
    public Result<Void> deleteArchiveRecord(@PathVariable Long id) {
        archiveService.deleteArchiveRecord(id);
        return Result.success();
    }

    @GetMapping("/list")
    @Operation(summary = "分页查询归档记录", description = "分页查询所有归档记录")
    public Result<IPage<ArchiveRecord>> listArchiveRecords(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量", example = "20") @RequestParam(defaultValue = "20") Integer pageSize) {
        IPage<ArchiveRecord> page = archiveService.listArchiveRecords(pageNum, pageSize);
        return Result.success(page);
    }

    @GetMapping("/dataAsset/{dataAssetId}")
    @Operation(summary = "根据数据资产ID查询归档记录", description = "根据数据资产ID查询对应的归档记录")
    public Result<IPage<ArchiveRecord>> listArchiveRecordsByDataAssetId(
            @PathVariable Long dataAssetId,
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量", example = "20") @RequestParam(defaultValue = "20") Integer pageSize) {
        IPage<ArchiveRecord> page = archiveService.listArchiveRecordsByDataAssetId(dataAssetId, pageNum, pageSize);
        return Result.success(page);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "根据归档状态查询归档记录", description = "根据归档状态查询归档记录")
    public Result<IPage<ArchiveRecord>> listArchiveRecordsByStatus(
            @PathVariable Integer status,
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量", example = "20") @RequestParam(defaultValue = "20") Integer pageSize) {
        IPage<ArchiveRecord> page = archiveService.listArchiveRecordsByStatus(status, pageNum, pageSize);
        return Result.success(page);
    }

    @GetMapping("/restoreStatus/{status}")
    @Operation(summary = "根据还原状态查询归档记录", description = "根据还原状态查询归档记录")
    public Result<IPage<ArchiveRecord>> listArchiveRecordsByRestoreStatus(
            @PathVariable Integer status,
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量", example = "20") @RequestParam(defaultValue = "20") Integer pageSize) {
        IPage<ArchiveRecord> page = archiveService.listArchiveRecordsByRestoreStatus(status, pageNum, pageSize);
        return Result.success(page);
    }

    @PostMapping("/archive")
    @Operation(summary = "归档数据资产", description = "执行数据资产归档操作")
    public Result<ArchiveRecord> archiveDataAsset(
            @Parameter(description = "数据资产ID") @RequestParam Long dataAssetId,
            @Parameter(description = "数据资产名称") @RequestParam String assetName,
            @Parameter(description = "归档类型：0-自动归档，1-手动归档") @RequestParam Integer archiveType,
            @Parameter(description = "操作人") @RequestParam String operator) {
        ArchiveRecord record = archiveService.archiveDataAsset(dataAssetId, assetName, archiveType, operator);
        return Result.success(record);
    }

    @PostMapping("/{id}/restore")
    @Operation(summary = "还原归档数据资产", description = "还原已归档的数据资产")
    public Result<ArchiveRecord> restoreDataAsset(
            @PathVariable Long id,
            @Parameter(description = "操作人") @RequestParam String operator) {
        ArchiveRecord record = archiveService.restoreDataAsset(id, operator);
        return Result.success(record);
    }

    @PostMapping("/{id}/status")
    @Operation(summary = "更新归档状态", description = "更新归档记录的归档状态")
    public Result<ArchiveRecord> updateArchiveStatus(
            @PathVariable Long id,
            @Parameter(description = "归档状态") @RequestParam Integer status) {
        ArchiveRecord record = archiveService.updateArchiveStatus(id, status);
        return Result.success(record);
    }
}