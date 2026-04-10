package com.enterprise.edams.version.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.edams.common.result.Result;
import com.enterprise.edams.version.entity.VersionRecord;
import com.enterprise.edams.version.service.VersionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 版本管理控制器
 *
 * @author EDAMS Team
 */
@RestController
@RequestMapping("/api/v1/versions")
@RequiredArgsConstructor
@Tag(name = "版本管理", description = "版本管理相关接口")
public class VersionController {

    private final VersionService versionService;

    @PostMapping("/create")
    @Operation(summary = "创建版本")
    public Result<VersionRecord> createVersion(
            @RequestParam String businessType,
            @RequestParam String businessId,
            @RequestBody Map<String, Object> dataContent,
            @RequestParam(required = false) String versionTag,
            @RequestParam(required = false) String versionComment,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Name") String userName) {
        return Result.success(versionService.createVersion(businessType, businessId, dataContent,
                versionTag, versionComment, userId, userName));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取版本详情")
    public Result<VersionRecord> getVersion(@PathVariable String id) {
        return Result.success(versionService.getVersion(id));
    }

    @GetMapping("/business/{businessType}/{businessId}")
    @Operation(summary = "查询业务的所有版本")
    public Result<List<VersionRecord>> getVersionsByBusiness(
            @PathVariable String businessType,
            @PathVariable String businessId) {
        return Result.success(versionService.getVersionsByBusiness(businessType, businessId));
    }

    @GetMapping("/business/{businessType}/{businessId}/latest")
    @Operation(summary = "获取最新版本")
    public Result<VersionRecord> getLatestVersion(
            @PathVariable String businessType,
            @PathVariable String businessId) {
        return Result.success(versionService.getLatestVersion(businessType, businessId));
    }

    @PostMapping("/{id}/rollback")
    @Operation(summary = "回滚到指定版本")
    public Result<Map<String, Object>> rollbackVersion(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Name") String userName) {
        return Result.success(versionService.rollbackVersion(id, userId, userName));
    }

    @GetMapping("/compare")
    @Operation(summary = "对比两个版本")
    public Result<Map<String, Object>> compareVersions(
            @RequestParam String versionId1,
            @RequestParam String versionId2) {
        return Result.success(versionService.compareVersions(versionId1, versionId2));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除版本")
    public Result<Void> deleteVersion(@PathVariable String id) {
        versionService.deleteVersion(id);
        return Result.success();
    }
}
