package com.enterprise.edams.asset.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.enterprise.edams.asset.dto.AssetCreateRequest;
import com.enterprise.edams.asset.dto.AssetDTO;
import com.enterprise.edams.asset.dto.AssetQueryRequest;
import com.enterprise.edams.asset.dto.AssetUpdateRequest;
import com.enterprise.edams.asset.service.AssetService;
import com.enterprise.edams.common.enums.AssetStatus;
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
 * 资产管理控制器
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/assets")
@RequiredArgsConstructor
@Tag(name = "资产管理", description = "数据资产的CRUD、状态管理、标签管理等接口")
public class AssetController {

    private final AssetService assetService;

    /**
     * 创建资产
     */
    @PostMapping
    @Operation(summary = "创建资产", description = "创建新的数据资产")
    public Result<AssetDTO> createAsset(@Valid @RequestBody AssetCreateRequest request) {
        AssetDTO asset = assetService.createAsset(request, getCurrentUser());
        return Result.success(asset);
    }

    /**
     * 更新资产
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新资产", description = "更新资产信息")
    public Result<AssetDTO> updateAsset(
            @PathVariable Long id,
            @Valid @RequestBody AssetUpdateRequest request) {
        AssetDTO asset = assetService.updateAsset(id, request, getCurrentUser());
        return Result.success(asset);
    }

    /**
     * 删除资产
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除资产", description = "逻辑删除资产")
    public Result<Void> deleteAsset(@PathVariable Long id) {
        assetService.deleteAsset(id, getCurrentUser());
        return Result.success();
    }

    /**
     * 获取资产详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取资产详情", description = "根据ID获取资产详情")
    public Result<AssetDTO> getAssetById(@PathVariable Long id) {
        AssetDTO asset = assetService.getAssetById(id);
        return Result.success(asset);
    }

    /**
     * 根据编码获取资产
     */
    @GetMapping("/code/{assetCode}")
    @Operation(summary = "根据编码获取资产")
    public Result<AssetDTO> getAssetByCode(@PathVariable String assetCode) {
        AssetDTO asset = assetService.getAssetByCode(assetCode);
        return Result.success(asset);
    }

    /**
     * 分页查询资产列表
     */
    @PostMapping("/list")
    @Operation(summary = "分页查询资产列表", description = "支持多条件筛选和分页")
    public PageResult<AssetDTO> queryAssets(@RequestBody AssetQueryRequest request) {
        IPage<AssetDTO> page = assetService.queryAssets(request);
        return PageResult.success(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize());
    }

    /**
     * 搜索资产
     */
    @GetMapping("/search")
    @Operation(summary = "搜索资产", description = "关键词搜索资产")
    public Result<List<AssetDTO>> searchAssets(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "20") int limit) {
        List<AssetDTO> assets = assetService.searchAssets(keyword);
        return Result.success(assets.stream().limit(limit).toList());
    }

    /**
     * 发布资产
     */
    @PutMapping("/{id}/publish")
    @Operation(summary = "发布资产", description = "将资产从草稿/审核状态发布为正式状态")
    public Result<Void> publishAsset(@PathVariable Long id) {
        assetService.publishAsset(id, getCurrentUser());
        return Result.success();
    }

    /**
     * 归档资产
     */
    @PutMapping("/{id}/archive")
    @Operation(summary = "归档资产", description = "将资产归档")
    public Result<Void> archiveAsset(@PathVariable Long id) {
        assetService.archiveAsset(id, getCurrentUser());
        return Result.success();
    }

    /**
     * 更新资产状态
     */
    @PutMapping("/{id}/status")
    @Operation(summary = "更新资产状态")
    public Result<Void> updateAssetStatus(
            @PathVariable Long id,
            @RequestParam AssetStatus status) {
        assetService.updateAssetStatus(id, status, getCurrentUser());
        return Result.success();
    }

    /**
     * 获取目录下的资产
     */
    @GetMapping("/catalog/{catalogId}")
    @Operation(summary = "获取目录下的资产")
    public PageResult<AssetDTO> getAssetsByCatalog(
            @PathVariable Long catalogId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        IPage<AssetDTO> page = assetService.getAssetsByCatalog(catalogId, pageNum, pageSize);
        return PageResult.success(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize());
    }

    /**
     * 获取业务域下的资产
     */
    @GetMapping("/domain/{domainId}")
    @Operation(summary = "获取业务域下的资产")
    public PageResult<AssetDTO> getAssetsByDomain(
            @PathVariable Long domainId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        IPage<AssetDTO> page = assetService.getAssetsByDomain(domainId, pageNum, pageSize);
        return PageResult.success(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize());
    }

    /**
     * 获取用户的资产
     */
    @GetMapping("/owner/{ownerId}")
    @Operation(summary = "获取用户的资产")
    public PageResult<AssetDTO> getAssetsByOwner(
            @PathVariable Long ownerId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        IPage<AssetDTO> page = assetService.getAssetsByOwner(ownerId, pageNum, pageSize);
        return PageResult.success(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize());
    }

    /**
     * 为资产打标签
     */
    @PostMapping("/{id}/tags")
    @Operation(summary = "为资产打标签")
    public Result<Void> tagAsset(
            @PathVariable Long id,
            @RequestBody List<Long> tagIds) {
        assetService.tagAsset(id, tagIds, getCurrentUser());
        return Result.success();
    }

    /**
     * 移除资产标签
     */
    @DeleteMapping("/{id}/tags")
    @Operation(summary = "移除资产标签")
    public Result<Void> untagAsset(
            @PathVariable Long id,
            @RequestParam List<Long> tagIds) {
        assetService.untagAsset(id, tagIds);
        return Result.success();
    }

    /**
     * 获取资产的标签
     */
    @GetMapping("/{id}/tags")
    @Operation(summary = "获取资产的标签")
    public Result<List<String>> getAssetTags(@PathVariable Long id) {
        List<String> tags = assetService.getAssetTags(id);
        return Result.success(tags);
    }

    /**
     * 同步资产元数据
     */
    @PostMapping("/{id}/sync")
    @Operation(summary = "同步资产元数据", description = "触发资产元数据同步")
    public Result<Void> syncAssetMetadata(@PathVariable Long id) {
        assetService.syncAssetMetadata(id, getCurrentUser());
        return Result.success();
    }

    /**
     * 统计用户资产数量
     */
    @GetMapping("/owner/{ownerId}/count")
    @Operation(summary = "统计用户资产数量")
    public Result<Long> countAssetsByOwner(@PathVariable Long ownerId) {
        long count = assetService.countAssetsByOwner(ownerId);
        return Result.success(count);
    }

    /**
     * 统计业务域资产数量
     */
    @GetMapping("/domain/{domainId}/count")
    @Operation(summary = "统计业务域资产数量")
    public Result<Long> countAssetsByDomain(@PathVariable Long domainId) {
        long count = assetService.countAssetsByDomain(domainId);
        return Result.success(count);
    }

    /**
     * 获取当前用户(模拟)
     */
    private String getCurrentUser() {
        // TODO: 从安全上下文获取当前用户
        return "system";
    }
}
