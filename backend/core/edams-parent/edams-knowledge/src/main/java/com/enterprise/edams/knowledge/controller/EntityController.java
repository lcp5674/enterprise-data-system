package com.enterprise.edams.knowledge.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.edams.knowledge.dto.EntityDTO;
import com.enterprise.edams.knowledge.dto.EntityDetailDTO;
import com.enterprise.edams.knowledge.entity.Entity;
import com.enterprise.edams.knowledge.service.EntityService;
import com.enterprise.edams.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 实体控制器
 */
@Tag(name = "实体管理", description = "实体的CRUD操作")
@RestController
@RequestMapping("/api/knowledge/entity")
@RequiredArgsConstructor
public class EntityController {

    private final EntityService entityService;

    @GetMapping
    @Operation(summary = "分页查询实体")
    public Result<Page<Entity>> page(
            @Parameter(description = "本体论ID") @RequestParam(required = false) Long ontologyId,
            @Parameter(description = "类ID") @RequestParam(required = false) Long classId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int pageSize) {
        Page<Entity> page = entityService.selectPage(ontologyId, classId, pageNum, pageSize);
        return Result.success(page);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询实体")
    public Result<EntityDTO> getById(@PathVariable Long id) {
        EntityDTO entity = entityService.getById(id);
        return Result.success(entity);
    }

    @GetMapping("/{id}/detail")
    @Operation(summary = "获取实体详情")
    public Result<EntityDetailDTO> getDetail(@PathVariable Long id) {
        EntityDetailDTO detail = entityService.getDetail(id);
        return Result.success(detail);
    }

    @GetMapping("/unique/{uniqueId}")
    @Operation(summary = "根据唯一标识查询实体")
    public Result<EntityDTO> getByUniqueId(@PathVariable String uniqueId) {
        EntityDTO entity = entityService.getByUniqueId(uniqueId);
        return Result.success(entity);
    }

    @GetMapping("/search")
    @Operation(summary = "搜索实体")
    public Result<List<EntityDTO>> search(
            @Parameter(description = "关键词") @RequestParam String keyword,
            @Parameter(description = "本体论ID") @RequestParam(required = false) Long ontologyId,
            @Parameter(description = "返回数量") @RequestParam(defaultValue = "20") int limit) {
        List<EntityDTO> entities = entityService.search(keyword, ontologyId, limit);
        return Result.success(entities);
    }

    @GetMapping("/hot")
    @Operation(summary = "获取热门实体")
    public Result<List<EntityDTO>> getHot(
            @Parameter(description = "返回数量") @RequestParam(defaultValue = "10") int limit) {
        List<EntityDTO> entities = entityService.getHotEntities(limit);
        return Result.success(entities);
    }

    @PostMapping
    @Operation(summary = "创建实体")
    public Result<EntityDTO> create(@RequestBody EntityDTO dto) {
        EntityDTO created = entityService.create(dto);
        return Result.success(created);
    }

    @PostMapping("/batch")
    @Operation(summary = "批量创建实体")
    public Result<List<EntityDTO>> batchCreate(@RequestBody List<EntityDTO> dtoList) {
        List<EntityDTO> created = entityService.batchCreate(dtoList);
        return Result.success(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新实体")
    public Result<EntityDTO> update(@PathVariable Long id, @RequestBody EntityDTO dto) {
        EntityDTO updated = entityService.update(id, dto);
        return Result.success(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除实体")
    public Result<Void> delete(@PathVariable Long id) {
        entityService.delete(id);
        return Result.success();
    }

    @PostMapping("/{id}/view")
    @Operation(summary = "增加访问次数")
    public Result<Void> incrementView(@PathVariable Long id) {
        entityService.incrementViewCount(id);
        return Result.success();
    }

    @PostMapping("/{entityId}/link-asset")
    @Operation(summary = "关联资产")
    public Result<EntityDTO> linkAsset(
            @PathVariable Long entityId,
            @RequestParam Long assetId,
            @RequestParam String assetType) {
        EntityDTO linked = entityService.linkAsset(entityId, assetId, assetType);
        return Result.success(linked);
    }
}
