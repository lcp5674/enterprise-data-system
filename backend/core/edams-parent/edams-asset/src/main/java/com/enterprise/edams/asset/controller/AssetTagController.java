package com.enterprise.edams.asset.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.enterprise.edams.asset.dto.AssetTagCreateRequest;
import com.enterprise.edams.asset.dto.AssetTagDTO;
import com.enterprise.edams.asset.service.AssetTagService;
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
 * 资产标签控制器
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/asset-tags")
@RequiredArgsConstructor
@Tag(name = "资产标签管理", description = "资产标签的CRUD、分类、热门标签等接口")
public class AssetTagController {

    private final AssetTagService tagService;

    /**
     * 创建标签
     */
    @PostMapping
    @Operation(summary = "创建标签", description = "创建新的资产标签")
    public Result<AssetTagDTO> createTag(@Valid @RequestBody AssetTagCreateRequest request) {
        AssetTagDTO tag = tagService.createTag(request, getCurrentUserId());
        return Result.success(tag);
    }

    /**
     * 更新标签
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新标签", description = "更新标签信息")
    public Result<AssetTagDTO> updateTag(
            @PathVariable Long id,
            @Valid @RequestBody AssetTagCreateRequest request) {
        AssetTagDTO tag = tagService.updateTag(id, request);
        return Result.success(tag);
    }

    /**
     * 删除标签
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除标签", description = "删除标签(需确保无关联资产)")
    public Result<Void> deleteTag(@PathVariable Long id) {
        tagService.deleteTag(id);
        return Result.success();
    }

    /**
     * 获取标签详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取标签详情")
    public Result<AssetTagDTO> getTagById(@PathVariable Long id) {
        AssetTagDTO tag = tagService.getTagById(id);
        return Result.success(tag);
    }

    /**
     * 根据编码获取标签
     */
    @GetMapping("/code/{tagCode}")
    @Operation(summary = "根据编码获取标签")
    public Result<AssetTagDTO> getTagByCode(@PathVariable String tagCode) {
        AssetTagDTO tag = tagService.getTagByCode(tagCode);
        return Result.success(tag);
    }

    /**
     * 根据名称获取标签
     */
    @GetMapping("/name/{tagName}")
    @Operation(summary = "根据名称获取标签")
    public Result<AssetTagDTO> getTagByName(@PathVariable String tagName) {
        AssetTagDTO tag = tagService.getTagByName(tagName);
        return Result.success(tag);
    }

    /**
     * 分页查询标签列表
     */
    @GetMapping
    @Operation(summary = "分页查询标签列表")
    public PageResult<AssetTagDTO> queryTags(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        IPage<AssetTagDTO> page = tagService.queryTags(keyword, category, status, pageNum, pageSize);
        return PageResult.success(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize());
    }

    /**
     * 获取所有标签
     */
    @GetMapping("/all")
    @Operation(summary = "获取所有标签")
    public Result<List<AssetTagDTO>> getAllTags() {
        List<AssetTagDTO> tags = tagService.getAllTags();
        return Result.success(tags);
    }

    /**
     * 根据分类获取标签
     */
    @GetMapping("/category/{category}")
    @Operation(summary = "根据分类获取标签")
    public Result<List<AssetTagDTO>> getTagsByCategory(@PathVariable String category) {
        List<AssetTagDTO> tags = tagService.getTagsByCategory(category);
        return Result.success(tags);
    }

    /**
     * 获取热门标签
     */
    @GetMapping("/hot")
    @Operation(summary = "获取热门标签", description = "按使用次数排序的热门标签")
    public Result<List<AssetTagDTO>> getHotTags(
            @RequestParam(defaultValue = "10") int limit) {
        List<AssetTagDTO> tags = tagService.getHotTags(limit);
        return Result.success(tags);
    }

    /**
     * 搜索标签
     */
    @GetMapping("/search")
    @Operation(summary = "搜索标签")
    public Result<List<AssetTagDTO>> searchTags(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "10") int limit) {
        List<AssetTagDTO> tags = tagService.searchTags(keyword, limit);
        return Result.success(tags);
    }

    /**
     * 为资产添加标签
     */
    @PostMapping("/asset/{assetId}")
    @Operation(summary = "为资产添加标签")
    public Result<Void> addTagToAsset(
            @PathVariable Long assetId,
            @RequestParam Long tagId) {
        tagService.addTagToAsset(assetId, tagId, getCurrentUserId());
        return Result.success();
    }

    /**
     * 为资产批量添加标签
     */
    @PostMapping("/asset/{assetId}/batch")
    @Operation(summary = "为资产批量添加标签")
    public Result<Void> addTagsToAsset(
            @PathVariable Long assetId,
            @RequestBody List<Long> tagIds) {
        tagService.addTagsToAsset(assetId, tagIds, getCurrentUserId());
        return Result.success();
    }

    /**
     * 移除资产的标签
     */
    @DeleteMapping("/asset/{assetId}")
    @Operation(summary = "移除资产的标签")
    public Result<Void> removeTagFromAsset(
            @PathVariable Long assetId,
            @RequestParam Long tagId) {
        tagService.removeTagFromAsset(assetId, tagId);
        return Result.success();
    }

    /**
     * 移除资产的所有标签
     */
    @DeleteMapping("/asset/{assetId}/all")
    @Operation(summary = "移除资产的所有标签")
    public Result<Void> removeAllTagsFromAsset(@PathVariable Long assetId) {
        tagService.removeAllTagsFromAsset(assetId);
        return Result.success();
    }

    /**
     * 获取资产的标签列表
     */
    @GetMapping("/asset/{assetId}")
    @Operation(summary = "获取资产的标签列表")
    public Result<List<AssetTagDTO>> getTagsByAssetId(@PathVariable Long assetId) {
        List<AssetTagDTO> tags = tagService.getTagsByAssetId(assetId);
        return Result.success(tags);
    }

    /**
     * 启用标签
     */
    @PutMapping("/{id}/enable")
    @Operation(summary = "启用标签")
    public Result<Void> enableTag(@PathVariable Long id) {
        tagService.enableTag(id);
        return Result.success();
    }

    /**
     * 禁用标签
     */
    @PutMapping("/{id}/disable")
    @Operation(summary = "禁用标签")
    public Result<Void> disableTag(@PathVariable Long id) {
        tagService.disableTag(id);
        return Result.success();
    }

    /**
     * 获取标签分类列表
     */
    @GetMapping("/categories")
    @Operation(summary = "获取标签分类列表")
    public Result<List<String>> getTagCategories() {
        List<String> categories = tagService.getTagCategories();
        return Result.success(categories);
    }

    /**
     * 获取当前用户ID
     */
    private Long getCurrentUserId() {
        // TODO: 从安全上下文获取当前用户ID
        return 1L;
    }
}
