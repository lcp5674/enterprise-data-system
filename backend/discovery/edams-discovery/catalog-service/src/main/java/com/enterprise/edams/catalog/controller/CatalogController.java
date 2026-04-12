package com.enterprise.edams.catalog.controller;

import com.enterprise.edams.catalog.dto.CatalogCreateRequest;
import com.enterprise.edams.catalog.dto.CatalogDTO;
import com.enterprise.edams.catalog.service.CatalogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/catalogs")
@Tag(name = "数据目录管理", description = "数据资产目录树管理")
public class CatalogController {

    @Autowired
    private CatalogService catalogService;

    @GetMapping("/tree")
    @Operation(summary = "获取目录树")
    public ResponseEntity<List<CatalogDTO>> getCatalogTree() {
        return ResponseEntity.ok(catalogService.getCatalogTree());
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取目录详情")
    public ResponseEntity<CatalogDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(catalogService.getById(id));
    }

    @PostMapping
    @Operation(summary = "创建目录")
    public ResponseEntity<CatalogDTO> create(@Valid @RequestBody CatalogCreateRequest request) {
        return ResponseEntity.ok(catalogService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新目录")
    public ResponseEntity<CatalogDTO> update(@PathVariable Long id,
                                              @Valid @RequestBody CatalogCreateRequest request) {
        return ResponseEntity.ok(catalogService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除目录")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        catalogService.delete(id);
        return ResponseEntity.ok(Map.of("success", true, "id", id));
    }

    @GetMapping("/{parentId}/children")
    @Operation(summary = "获取子目录列表")
    public ResponseEntity<List<CatalogDTO>> getChildren(@PathVariable Long parentId) {
        return ResponseEntity.ok(catalogService.getChildren(parentId));
    }

    @GetMapping("/search")
    @Operation(summary = "搜索目录")
    public ResponseEntity<List<CatalogDTO>> search(@RequestParam String keyword) {
        return ResponseEntity.ok(catalogService.search(keyword));
    }

    @GetMapping("/stats")
    @Operation(summary = "目录统计")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(catalogService.getStats());
    }
}
