package com.enterprise.edams.knowledge.controller;

import com.enterprise.edams.common.result.Result;
import com.enterprise.edams.knowledge.dto.OntologyDTO;
import com.enterprise.edams.knowledge.entity.Ontology;
import com.enterprise.edams.knowledge.service.OntologyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 本体管理接口
 *
 * @author Knowledge Team
 * @version 1.0.0
 */
@Tag(name = "本体管理", description = "本体的创建、更新、删除和查询接口")
@RestController
@RequestMapping("/api/v1/knowledge/ontologies")
@RequiredArgsConstructor
public class OntologyController {

    private final OntologyService ontologyService;

    @Operation(summary = "创建本体", description = "创建一个新的本体")
    @PostMapping
    public Result<Ontology> createOntology(@RequestBody OntologyDTO dto) {
        Ontology ontology = ontologyService.createOntology(dto);
        return Result.success("本体创建成功", ontology);
    }

    @Operation(summary = "更新本体", description = "更新指定本体的信息")
    @PutMapping("/{ontologyId}")
    public Result<Ontology> updateOntology(
            @Parameter(description = "本体ID") @PathVariable String ontologyId,
            @RequestBody OntologyDTO dto) {
        Ontology ontology = ontologyService.updateOntology(ontologyId, dto);
        return Result.success("本体更新成功", ontology);
    }

    @Operation(summary = "删除本体", description = "删除指定的本体")
    @DeleteMapping("/{ontologyId}")
    public Result<Void> deleteOntology(
            @Parameter(description = "本体ID") @PathVariable String ontologyId) {
        ontologyService.deleteOntology(ontologyId);
        return Result.success("本体删除成功", null);
    }

    @Operation(summary = "获取本体详情", description = "获取指定本体的详细信息")
    @GetMapping("/{ontologyId}")
    public Result<OntologyDTO> getOntologyDetail(
            @Parameter(description = "本体ID") @PathVariable String ontologyId) {
        OntologyDTO ontology = ontologyService.getOntologyDetail(ontologyId);
        return Result.success(ontology);
    }

    @Operation(summary = "获取图谱下的本体列表", description = "获取指定图谱下的所有本体")
    @GetMapping("/graph/{graphId}")
    public Result<List<OntologyDTO>> listByGraphId(
            @Parameter(description = "图谱ID") @PathVariable String graphId) {
        List<OntologyDTO> ontologies = ontologyService.listByGraphId(graphId);
        return Result.success(ontologies);
    }

    @Operation(summary = "添加实体类型", description = "向本体中添加实体类型定义")
    @PostMapping("/{ontologyId}/entity-types")
    public Result<Void> addEntityType(
            @Parameter(description = "本体ID") @PathVariable String ontologyId,
            @RequestBody OntologyDTO.EntityTypeDTO dto) {
        ontologyService.addEntityType(ontologyId, dto);
        return Result.success("实体类型添加成功", null);
    }

    @Operation(summary = "添加关系类型", description = "向本体中添加关系类型定义")
    @PostMapping("/{ontologyId}/relation-types")
    public Result<Void> addRelationType(
            @Parameter(description = "本体ID") @PathVariable String ontologyId,
            @RequestBody OntologyDTO.RelationTypeDTO dto) {
        ontologyService.addRelationType(ontologyId, dto);
        return Result.success("关系类型添加成功", null);
    }

    @Operation(summary = "验证本体完整性", description = "验证本体的完整性和一致性")
    @GetMapping("/{ontologyId}/validate")
    public Result<OntologyService.ValidationResult> validateOntology(
            @Parameter(description = "本体ID") @PathVariable String ontologyId) {
        OntologyService.ValidationResult result = ontologyService.validateOntology(ontologyId);
        return Result.success(result);
    }

    @Operation(summary = "导出本体", description = "导出本体定义到指定格式")
    @GetMapping("/{ontologyId}/export")
    public Result<String> exportOntology(
            @Parameter(description = "本体ID") @PathVariable String ontologyId,
            @Parameter(description = "导出格式") @RequestParam(defaultValue = "JSON") String format) {
        String ontologyDefinition = ontologyService.exportOntology(ontologyId, format);
        return Result.success(ontologyDefinition);
    }

    @Operation(summary = "导入本体", description = "从指定格式导入本体定义")
    @PostMapping("/import")
    public Result<Ontology> importOntology(
            @Parameter(description = "本体定义") @RequestBody String ontologyDefinition,
            @Parameter(description = "导入格式") @RequestParam(defaultValue = "JSON") String format) {
        Ontology ontology = ontologyService.importOntology(ontologyDefinition, format);
        return Result.success("本体导入成功", ontology);
    }
}
