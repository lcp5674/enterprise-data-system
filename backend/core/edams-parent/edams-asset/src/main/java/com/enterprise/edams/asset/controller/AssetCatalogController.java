package com.enterprise.edams.asset.controller;

import com.enterprise.edams.asset.dto.AssetCatalogCreateRequest;
import com.enterprise.edams.asset.dto.AssetCatalogDTO;
import com.enterprise.edams.asset.service.AssetCatalogService;
import com.enterprise.edams.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 资产目录控制器
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/asset-catalogs")
@RequiredArgsConstructor
@Tag(name = "资产目录管理", description = "资产目录的CRUD、树形结构、排序等接口")
public class AssetCatalogController {

    private final AssetCatalogService catalogService;

    /**
     * 创建目录
     */
    @PostMapping
    @Operation(summary = "创建目录", description = "创建新的资产目录")
    public Result<AssetCatalogDTO> createCatalog(@Valid @RequestBody AssetCatalogCreateRequest request) {
        AssetCatalogDTO catalog = catalogService.createCatalog(request, getCurrentUser());
        return Result.success(catalog);
    }

    /**
     * 更新目录
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新目录", description = "更新目录信息")
    public Result<AssetCatalogDTO> updateCatalog(
            @PathVariable Long id,
            @Valid @RequestBody AssetCatalogCreateRequest request) {
        AssetCatalogDTO catalog = catalogService.updateCatalog(id, request, getCurrentUser());
        return Result.success(catalog);
    }

    /**
     * 删除目录
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除目录", description = "删除目录(需确保无子目录和关联资产)")
    public Result<Void> deleteCatalog(@PathVariable Long id) {
        catalogService.deleteCatalog(id, getCurrentUser());
        return Result.success();
    }

    /**
     * 获取目录详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取目录详情")
    public Result<AssetCatalogDTO> getCatalogById(@PathVariable Long id) {
        AssetCatalogDTO catalog = catalogService.getCatalogById(id);
        return Result.success(catalog);
    }

    /**
     * 根据编码获取目录
     */
    @GetMapping("/code/{code}")
    @Operation(summary = "根据编码获取目录")
    public Result<AssetCatalogDTO> getCatalogByCode(@PathVariable String code) {
        AssetCatalogDTO catalog = catalogService.getCatalogByCode(code);
        return Result.success(catalog);
    }

    /**
     * 获取根目录列表
     */
    @GetMapping("/roots")
    @Operation(summary = "获取根目录列表")
    public Result<List<AssetCatalogDTO>> getRootCatalogs() {
        List<AssetCatalogDTO> catalogs = catalogService.getRootCatalogs();
        return Result.success(catalogs);
    }

    /**
     * 获取子目录列表
     */
    @GetMapping("/{parentId}/children")
    @Operation(summary = "获取子目录列表")
    public Result<List<AssetCatalogDTO>> getChildCatalogs(@PathVariable Long parentId) {
        List<AssetCatalogDTO> catalogs = catalogService.getChildCatalogs(parentId);
        return Result.success(catalogs);
    }

    /**
     * 获取目录树(启用状态)
     */
    @GetMapping("/tree")
    @Operation(summary = "获取目录树", description = "获取启用的目录树形结构")
    public Result<List<AssetCatalogDTO>> getCatalogTree() {
        List<AssetCatalogDTO> tree = catalogService.getCatalogTree();
        return Result.success(tree);
    }

    /**
     * 获取完整目录树(包含禁用)
     */
    @GetMapping("/tree/full")
    @Operation(summary = "获取完整目录树", description = "获取所有目录的树形结构")
    public Result<List<AssetCatalogDTO>> getFullCatalogTree() {
        List<AssetCatalogDTO> tree = catalogService.getFullCatalogTree();
        return Result.success(tree);
    }

    /**
     * 移动目录
     */
    @PutMapping("/{id}/move")
    @Operation(summary = "移动目录", description = "将目录移动到新的父目录下")
    public Result<Void> moveCatalog(
            @PathVariable Long id,
            @RequestParam Long newParentId) {
        catalogService.moveCatalog(id, newParentId, getCurrentUser());
        return Result.success();
    }

    /**
     * 更新目录排序
     */
    @PutMapping("/{id}/sort")
    @Operation(summary = "更新目录排序")
    public Result<Void> updateSortOrder(
            @PathVariable Long id,
            @RequestParam Integer sortOrder) {
        catalogService.updateSortOrder(id, sortOrder, getCurrentUser());
        return Result.success();
    }

    /**
     * 启用目录
     */
    @PutMapping("/{id}/enable")
    @Operation(summary = "启用目录")
    public Result<Void> enableCatalog(@PathVariable Long id) {
        catalogService.enableCatalog(id, getCurrentUser());
        return Result.success();
    }

    /**
     * 禁用目录
     */
    @PutMapping("/{id}/disable")
    @Operation(summary = "禁用目录")
    public Result<Void> disableCatalog(@PathVariable Long id) {
        catalogService.disableCatalog(id, getCurrentUser());
        return Result.success();
    }

    /**
     * 统计目录下资产数量
     */
    @GetMapping("/{id}/asset-count")
    @Operation(summary = "统计目录下资产数量")
    public Result<Integer> countAssetsInCatalog(@PathVariable Long id) {
        int count = catalogService.countAssetsInCatalog(id);
        return Result.success(count);
    }

    /**
     * 获取当前用户
     */
    private String getCurrentUser() {
        // TODO: 从安全上下文获取当前用户
        return "system";
    }
}
