package com.enterprise.edams.knowledge.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.edams.knowledge.dto.OntologyDTO;
import com.enterprise.edams.knowledge.entity.Ontology;
import com.enterprise.edams.knowledge.service.OntologyService;
import com.enterprise.edams.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 本体论控制器
 */
@Tag(name = "本体论管理", description = "本体论的CRUD操作")
@RestController
@RequestMapping("/api/knowledge/ontology")
@RequiredArgsConstructor
public class OntologyController {

    private final OntologyService ontologyService;

    @GetMapping
    @Operation(summary = "分页查询本体论")
    public Result<Page<Ontology>> page(
            @Parameter(description = "本体论名称") @RequestParam(required = false) String name,
            @Parameter(description = "状态") @RequestParam(required = false) String status,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int pageSize) {
        OntologyDTO dto = new OntologyDTO();
        dto.setName(name);
        dto.setStatus(status);
        Page<Ontology> page = ontologyService.selectPage(dto, pageNum, pageSize);
        return Result.success(page);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询本体论")
    public Result<OntologyDTO> getById(@PathVariable Long id) {
        OntologyDTO ontology = ontologyService.getById(id);
        return Result.success(ontology);
    }

    @PostMapping
    @Operation(summary = "创建本体论")
    public Result<OntologyDTO> create(@RequestBody OntologyDTO dto) {
        OntologyDTO created = ontologyService.create(dto);
        return Result.success(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新本体论")
    public Result<OntologyDTO> update(@PathVariable Long id, @RequestBody OntologyDTO dto) {
        OntologyDTO updated = ontologyService.update(id, dto);
        return Result.success(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除本体论")
    public Result<Void> delete(@PathVariable Long id) {
        ontologyService.delete(id);
        return Result.success();
    }

    @PostMapping("/{id}/publish")
    @Operation(summary = "发布本体论")
    public Result<OntologyDTO> publish(@PathVariable Long id) {
        OntologyDTO published = ontologyService.publish(id);
        return Result.success(published);
    }

    @GetMapping("/published")
    @Operation(summary = "获取所有已发布的本体论")
    public Result<List<OntologyDTO>> getPublished() {
        List<OntologyDTO> ontologies = ontologyService.getPublishedOntologies();
        return Result.success(ontologies);
    }

    @GetMapping("/{id}/statistics")
    @Operation(summary = "获取本体论统计信息")
    public Result<Ontology> getStatistics(@PathVariable Long id) {
        Ontology statistics = ontologyService.getStatistics(id);
        return Result.success(statistics);
    }
}
