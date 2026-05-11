package com.enterprise.dataplatform.ethics.controller;

import com.enterprise.dataplatform.ethics.domain.dto.request.EthicsFrameworkRequest;
import com.enterprise.dataplatform.ethics.domain.dto.response.EthicsFrameworkResponse;
import com.enterprise.dataplatform.ethics.service.EthicsFrameworkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/ethics/frameworks")
@RequiredArgsConstructor
@Tag(name = "伦理框架管理", description = "数据伦理框架的定义和管理")
public class EthicsFrameworkController {

    private final EthicsFrameworkService frameworkService;

    @PostMapping
    @Operation(summary = "创建伦理框架")
    public ResponseEntity<Map<String, Object>> createFramework(
            @Valid @RequestBody EthicsFrameworkRequest request,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        EthicsFrameworkResponse response = frameworkService.createFramework(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(wrapResponse(response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新伦理框架")
    public ResponseEntity<Map<String, Object>> updateFramework(
            @PathVariable Long id,
            @Valid @RequestBody EthicsFrameworkRequest request,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        EthicsFrameworkResponse response = frameworkService.updateFramework(id, request, userId);
        return ResponseEntity.ok(wrapResponse(response));
    }

    @PostMapping("/{id}/publish")
    @Operation(summary = "发布伦理框架")
    public ResponseEntity<Map<String, Object>> publishFramework(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        EthicsFrameworkResponse response = frameworkService.publishFramework(id, userId);
        return ResponseEntity.ok(wrapResponse(response));
    }

    @PostMapping("/{id}/archive")
    @Operation(summary = "归档伦理框架")
    public ResponseEntity<Map<String, Object>> archiveFramework(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        EthicsFrameworkResponse response = frameworkService.archiveFramework(id, userId);
        return ResponseEntity.ok(wrapResponse(response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取框架详情")
    public ResponseEntity<Map<String, Object>> getFramework(@PathVariable Long id) {
        EthicsFrameworkResponse response = frameworkService.getFramework(id);
        return ResponseEntity.ok(wrapResponse(response));
    }

    @GetMapping("/code/{frameworkCode}")
    @Operation(summary = "根据编码获取框架")
    public ResponseEntity<Map<String, Object>> getFrameworkByCode(@PathVariable String frameworkCode) {
        EthicsFrameworkResponse response = frameworkService.getFrameworkByCode(frameworkCode);
        return ResponseEntity.ok(wrapResponse(response));
    }

    @GetMapping
    @Operation(summary = "查询伦理框架列表")
    public ResponseEntity<Map<String, Object>> searchFrameworks(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createTime").descending());
        Page<EthicsFrameworkResponse> result = frameworkService.searchFrameworks(status, category, enabled, keyword, pageable);
        return ResponseEntity.ok(wrapPageResponse(result));
    }

    @GetMapping("/active")
    @Operation(summary = "获取活跃框架")
    public ResponseEntity<Map<String, Object>> getActiveFrameworks() {
        List<EthicsFrameworkResponse> responses = frameworkService.getActiveFrameworks();
        return ResponseEntity.ok(wrapResponse(responses));
    }

    @GetMapping("/category/{category}")
    @Operation(summary = "根据分类获取框架")
    public ResponseEntity<Map<String, Object>> getFrameworksByCategory(@PathVariable String category) {
        List<EthicsFrameworkResponse> responses = frameworkService.getFrameworksByCategory(category);
        return ResponseEntity.ok(wrapResponse(responses));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除伦理框架")
    public ResponseEntity<Map<String, Object>> deleteFramework(@PathVariable Long id) {
        frameworkService.deleteFramework(id);
        return ResponseEntity.ok(wrapResponse("删除成功"));
    }

    private Map<String, Object> wrapResponse(Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "success");
        response.put("data", data);
        return response;
    }

    private Map<String, Object> wrapPageResponse(Page<?> page) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "success");
        Map<String, Object> pageData = new HashMap<>();
        pageData.put("content", page.getContent());
        pageData.put("page", page.getNumber());
        pageData.put("size", page.getSize());
        pageData.put("totalElements", page.getTotalElements());
        pageData.put("totalPages", page.getTotalPages());
        response.put("data", pageData);
        return response;
    }
}
