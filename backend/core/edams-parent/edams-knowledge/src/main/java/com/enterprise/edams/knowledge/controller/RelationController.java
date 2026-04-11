package com.enterprise.edams.knowledge.controller;

import com.enterprise.edams.knowledge.dto.RelationDTO;
import com.enterprise.edams.knowledge.service.RelationService;
import com.enterprise.edams.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 关系控制器
 */
@Tag(name = "关系管理", description = "关系的CRUD操作")
@RestController
@RequestMapping("/api/knowledge/relation")
@RequiredArgsConstructor
public class RelationController {

    private final RelationService relationService;

    @GetMapping("/entity/{entityId}")
    @Operation(summary = "获取实体的所有关系")
    public Result<List<RelationDTO>> getByEntityId(
            @PathVariable @Parameter(description = "实体ID") Long entityId) {
        List<RelationDTO> relations = relationService.getEntityRelations(entityId);
        return Result.success(relations);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询关系")
    public Result<RelationDTO> getById(@PathVariable Long id) {
        RelationDTO relation = relationService.getById(id);
        return Result.success(relation);
    }

    @GetMapping("/between")
    @Operation(summary = "查询两个实体间的关系")
    public Result<List<RelationDTO>> getBetweenEntities(
            @RequestParam @Parameter(description = "源实体ID") Long sourceId,
            @RequestParam @Parameter(description = "目标实体ID") Long targetId) {
        List<RelationDTO> relations = relationService.getBetweenEntities(sourceId, targetId);
        return Result.success(relations);
    }

    @PostMapping
    @Operation(summary = "创建关系")
    public Result<RelationDTO> create(@RequestBody RelationDTO dto) {
        RelationDTO created = relationService.create(dto);
        return Result.success(created);
    }

    @PostMapping("/batch")
    @Operation(summary = "批量创建关系")
    public Result<List<RelationDTO>> batchCreate(@RequestBody List<RelationDTO> dtoList) {
        List<RelationDTO> created = relationService.batchCreate(dtoList);
        return Result.success(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新关系")
    public Result<RelationDTO> update(@PathVariable Long id, @RequestBody RelationDTO dto) {
        RelationDTO updated = relationService.update(id, dto);
        return Result.success(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除关系")
    public Result<Void> delete(@PathVariable Long id) {
        relationService.delete(id);
        return Result.success();
    }

    @GetMapping("/count")
    @Operation(summary = "获取指定类型的关系统计")
    public Result<Long> countByType(
            @RequestParam @Parameter(description = "本体论ID") Long ontologyId,
            @RequestParam @Parameter(description = "关系类型") String relationType) {
        Long count = relationService.countByType(ontologyId, relationType);
        return Result.success(count);
    }
}
