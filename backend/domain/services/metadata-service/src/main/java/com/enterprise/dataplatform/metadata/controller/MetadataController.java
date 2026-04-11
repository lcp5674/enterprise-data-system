package com.enterprise.dataplatform.metadata.controller;

import com.enterprise.dataplatform.metadata.dto.request.MetadataFieldRequest;
import com.enterprise.dataplatform.metadata.dto.request.MetadataRegisterRequest;
import com.enterprise.dataplatform.metadata.dto.request.MetadataSearchRequest;
import com.enterprise.dataplatform.metadata.dto.response.MetadataResponse;
import com.enterprise.dataplatform.metadata.service.MetadataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Metadata Management REST Controller
 * Provides endpoints for metadata CRUD operations and search
 */
@RestController
@RequestMapping("/api/v1/metadata")
@RequiredArgsConstructor
@Tag(name = "元数据管理", description = "数据资产元数据的注册、查询、更新和删除")
public class MetadataController {

    private final MetadataService metadataService;

    /**
     * Register new metadata object
     */
    @PostMapping("/register")
    @Operation(summary = "注册元数据", description = "注册新的元数据对象及其字段信息")
    public ResponseEntity<Map<String, Object>> registerMetadata(
            @Valid @RequestBody MetadataRegisterRequest request,
            @RequestBody(required = false) List<MetadataFieldRequest> fields) {
        MetadataResponse response = metadataService.registerMetadata(request, fields);
        return ResponseEntity.status(HttpStatus.CREATED).body(wrapResponse(response));
    }

    /**
     * Update existing metadata object
     */
    @PutMapping("/{objectId}")
    @Operation(summary = "更新元数据", description = "更新已存在的元数据对象")
    public ResponseEntity<Map<String, Object>> updateMetadata(
            @PathVariable String objectId,
            @Valid @RequestBody MetadataRegisterRequest request,
            @RequestBody(required = false) List<MetadataFieldRequest> fields) {
        MetadataResponse response = metadataService.updateMetadata(objectId, request, fields);
        return ResponseEntity.ok(wrapResponse(response));
    }

    /**
     * Delete metadata object (logical delete)
     */
    @DeleteMapping("/{objectId}")
    @Operation(summary = "删除元数据", description = "逻辑删除元数据对象（状态改为DEPRECATED）")
    public ResponseEntity<Map<String, Object>> deleteMetadata(@PathVariable String objectId) {
        metadataService.deleteMetadata(objectId);
        return ResponseEntity.ok(wrapResponse("元数据删除成功"));
    }

    /**
     * Get metadata by objectId
     */
    @GetMapping("/{objectId}")
    @Operation(summary = "获取元数据", description = "根据对象ID查询元数据详情")
    public ResponseEntity<Map<String, Object>> getMetadata(@PathVariable String objectId) {
        MetadataResponse response = metadataService.getMetadata(objectId);
        return ResponseEntity.ok(wrapResponse(response));
    }

    /**
     * Search metadata with filters and pagination
     */
    @GetMapping("/search")
    @Operation(summary = "搜索元数据", description = "多条件分页搜索元数据")
    public ResponseEntity<Map<String, Object>> searchMetadata(
            @Parameter(description = "关键词（搜索名称和描述）")
            @RequestParam(required = false) String keyword,
            @Parameter(description = "对象类型：TABLE/VIEW/API/FILE/STREAM")
            @RequestParam(required = false) String objectType,
            @Parameter(description = "域编码")
            @RequestParam(required = false) String domainCode,
            @Parameter(description = "敏感等级：PUBLIC/INTERNAL/CONFIDENTIAL/SECRET")
            @RequestParam(required = false) String sensitivity,
            @Parameter(description = "状态")
            @RequestParam(required = false) String status,
            @Parameter(description = "负责人")
            @RequestParam(required = false) String owner,
            @Parameter(description = "页码")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小")
            @RequestParam(defaultValue = "20") int size) {

        MetadataSearchRequest searchRequest = MetadataSearchRequest.builder()
                .keyword(keyword)
                .objectType(objectType)
                .domainCode(domainCode)
                .sensitivity(sensitivity)
                .status(status)
                .owner(owner)
                .page(page)
                .size(size)
                .build();

        Page<MetadataResponse> result = metadataService.searchMetadata(searchRequest);
        return ResponseEntity.ok(wrapPageResponse(result));
    }

    /**
     * Get metadata by domain code
     */
    @GetMapping("/domain/{domainCode}")
    @Operation(summary = "按域查询", description = "获取指定域的所有元数据")
    public ResponseEntity<Map<String, Object>> getMetadataByDomain(@PathVariable String domainCode) {
        List<MetadataResponse> responses = metadataService.getMetadataByDomain(domainCode);
        return ResponseEntity.ok(wrapResponse(responses));
    }

    /**
     * Get metadata statistics
     */
    @GetMapping("/stats")
    @Operation(summary = "获取统计", description = "获取元数据统计信息（按类型、域、敏感等级分类）")
    public ResponseEntity<Map<String, Object>> getMetadataStats() {
        Map<String, Object> stats = metadataService.getMetadataStats();
        return ResponseEntity.ok(wrapResponse(stats));
    }

    /**
     * Sync metadata from asset service
     */
    @PostMapping("/sync/{assetId}")
    @Operation(summary = "同步资产元数据", description = "从资产服务同步元数据信息")
    public ResponseEntity<Map<String, Object>> syncFromAsset(
            @PathVariable String assetId,
            @RequestBody Map<String, Object> assetInfo) {
        MetadataResponse response = metadataService.syncFromAsset(assetId, assetInfo);
        return ResponseEntity.ok(wrapResponse(response));
    }

    // =============== Response Wrappers ===============

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
