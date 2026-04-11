package com.enterprise.edams.knowledge.controller;

import com.enterprise.edams.knowledge.dto.OntologyClassDTO;
import com.enterprise.edams.knowledge.service.OntologyClassService;
import com.enterprise.edams.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 本体类控制器
 */
@Tag(name = "本体类管理", description = "本体类的CRUD和树形结构操作")
@RestController
@RequestMapping("/api/knowledge/class")
@RequiredArgsConstructor
public class OntologyClassController {

    private final OntologyClassService classService;

    @GetMapping("/ontology/{ontologyId}")
    @Operation(summary = "查询本体论下的所有类")
    public Result<List<OntologyClassDTO>> getByOntologyId(
            @PathVariable @Parameter(description = "本体论ID") Long ontologyId) {
        List<OntologyClassDTO> classes = classService.selectByOntologyId(ontologyId);
        return Result.success(classes);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询类")
    public Result<OntologyClassDTO> getById(@PathVariable Long id) {
        OntologyClassDTO clazz = classService.getById(id);
        return Result.success(clazz);
    }

    @GetMapping("/{id}/tree")
    @Operation(summary = "获取类的子树")
    public Result<List<OntologyClassDTO>> getSubclassTree(@PathVariable Long id) {
        List<OntologyClassDTO> tree = classService.getSubclassTree(id);
        return Result.success(tree);
    }

    @GetMapping("/ontology/{ontologyId}/tree")
    @Operation(summary = "获取本体论的类树")
    public Result<List<OntologyClassDTO>> getClassTree(
            @PathVariable @Parameter(description = "本体论ID") Long ontologyId) {
        List<OntologyClassDTO> tree = classService.getClassTree(ontologyId);
        return Result.success(tree);
    }

    @GetMapping("/ontology/{ontologyId}/roots")
    @Operation(summary = "获取根类列表")
    public Result<List<OntologyClassDTO>> getRootClasses(
            @PathVariable @Parameter(description = "本体论ID") Long ontologyId) {
        List<OntologyClassDTO> roots = classService.getRootClasses(ontologyId);
        return Result.success(roots);
    }

    @PostMapping
    @Operation(summary = "创建类")
    public Result<OntologyClassDTO> create(@RequestBody OntologyClassDTO dto) {
        OntologyClassDTO created = classService.create(dto);
        return Result.success(created);
    }

    @PostMapping("/batch")
    @Operation(summary = "批量创建类")
    public Result<List<OntologyClassDTO>> batchCreate(@RequestBody List<OntologyClassDTO> dtoList) {
        List<OntologyClassDTO> created = classService.batchCreate(dtoList);
        return Result.success(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新类")
    public Result<OntologyClassDTO> update(@PathVariable Long id, @RequestBody OntologyClassDTO dto) {
        OntologyClassDTO updated = classService.update(id, dto);
        return Result.success(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除类")
    public Result<Void> delete(@PathVariable Long id) {
        classService.delete(id);
        return Result.success();
    }
}
