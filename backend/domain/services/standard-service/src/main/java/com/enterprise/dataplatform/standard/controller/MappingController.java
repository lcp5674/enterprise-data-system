package com.enterprise.dataplatform.standard.controller;

import com.enterprise.dataplatform.standard.dto.request.StandardMappingRequest;
import com.enterprise.dataplatform.standard.dto.response.StandardMappingResponse;
import com.enterprise.dataplatform.standard.service.StandardMappingService;
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

/**
 * 标准映射REST控制器
 */
@RestController
@RequestMapping("/api/v1/mappings")
@RequiredArgsConstructor
@Tag(name = "标准映射管理", description = "数据标准与数据资产字段的映射管理")
public class MappingController {

    private final StandardMappingService mappingService;

    /**
     * 创建标准映射
     */
    @PostMapping
    @Operation(summary = "创建标准映射", description = "创建数据标准与资产字段的映射关系")
    public ResponseEntity<Map<String, Object>> createMapping(
            @Valid @RequestBody StandardMappingRequest request,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        StandardMappingResponse response = mappingService.createMapping(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(wrapResponse(response));
    }

    /**
     * 批量创建映射
     */
    @PostMapping("/batch")
    @Operation(summary = "批量创建映射", description = "批量创建多个标准映射")
    public ResponseEntity<Map<String, Object>> batchCreateMappings(
            @Valid @RequestBody List<StandardMappingRequest> requests,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        List<StandardMappingResponse> responses = mappingService.batchCreateMappings(requests, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(wrapResponse(responses));
    }

    /**
     * 更新映射
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新标准映射", description = "更新指定的标准映射")
    public ResponseEntity<Map<String, Object>> updateMapping(
            @PathVariable Long id,
            @Valid @RequestBody StandardMappingRequest request,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        StandardMappingResponse response = mappingService.updateMapping(id, request, userId);
        return ResponseEntity.ok(wrapResponse(response));
    }

    /**
     * 审批映射
     */
    @PostMapping("/{id}/approve")
    @Operation(summary = "审批映射", description = "审批通过指定的标准映射")
    public ResponseEntity<Map<String, Object>> approveMapping(
            @PathVariable Long id,
            @RequestParam(required = false) String comment,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String approver) {
        StandardMappingResponse response = mappingService.approveMapping(id, approver, comment);
        return ResponseEntity.ok(wrapResponse(response));
    }

    /**
     * 拒绝映射
     */
    @PostMapping("/{id}/reject")
    @Operation(summary = "拒绝映射", description = "拒绝指定的标准映射")
    public ResponseEntity<Map<String, Object>> rejectMapping(
            @PathVariable Long id,
            @RequestParam(required = false) String reason,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String approver) {
        StandardMappingResponse response = mappingService.rejectMapping(id, approver, reason);
        return ResponseEntity.ok(wrapResponse(response));
    }

    /**
     * 查询映射
     */
    @GetMapping("/{id}")
    @Operation(summary = "查询映射", description = "根据ID查询标准映射")
    public ResponseEntity<Map<String, Object>> getMapping(@PathVariable Long id) {
        StandardMappingResponse response = mappingService.getMapping(id);
        return ResponseEntity.ok(wrapResponse(response));
    }

    /**
     * 根据标准ID查询映射
     */
    @GetMapping("/standard/{standardId}")
    @Operation(summary = "根据标准查询映射", description = "根据数据标准ID查询所有映射")
    public ResponseEntity<Map<String, Object>> getMappingsByStandardId(@PathVariable Long standardId) {
        List<StandardMappingResponse> responses = mappingService.getMappingsByStandardId(standardId);
        return ResponseEntity.ok(wrapResponse(responses));
    }

    /**
     * 根据资产ID查询映射
     */
    @GetMapping("/asset/{assetId}")
    @Operation(summary = "根据资产查询映射", description = "根据数据资产ID查询所有映射")
    public ResponseEntity<Map<String, Object>> getMappingsByAssetId(@PathVariable String assetId) {
        List<StandardMappingResponse> responses = mappingService.getMappingsByAssetId(assetId);
        return ResponseEntity.ok(wrapResponse(responses));
    }

    /**
     * 分页查询映射
     */
    @GetMapping
    @Operation(summary = "分页查询映射", description = "支持多条件分页查询")
    public ResponseEntity<Map<String, Object>> searchMappings(
            @RequestParam(required = false) Long standardId,
            @RequestParam(required = false) String assetId,
            @RequestParam(required = false) String mappingStatus,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createTime") String sortBy,
            @RequestParam(defaultValue = "desc") String sortOrder) {
        
        Sort sort = sortOrder.equalsIgnoreCase("asc") ? 
                Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<StandardMappingResponse> result = mappingService.searchMappings(
                standardId, assetId, mappingStatus, pageable);
        
        return ResponseEntity.ok(wrapPageResponse(result));
    }

    /**
     * 查询待审批的映射
     */
    @GetMapping("/pending")
    @Operation(summary = "待审批映射", description = "查询所有待审批的标准映射")
    public ResponseEntity<Map<String, Object>> getPendingMappings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createTime").descending());
        Page<StandardMappingResponse> result = mappingService.getPendingMappings(pageable);
        return ResponseEntity.ok(wrapPageResponse(result));
    }

    /**
     * 删除映射
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除映射", description = "删除指定的标准映射")
    public ResponseEntity<Map<String, Object>> deleteMapping(@PathVariable Long id) {
        mappingService.deleteMapping(id);
        return ResponseEntity.ok(wrapResponse("删除成功"));
    }

    /**
     * 包装响应数据
     */
    private Map<String, Object> wrapResponse(Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "success");
        response.put("data", data);
        return response;
    }

    /**
     * 包装分页响应数据
     */
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
        pageData.put("first", page.isFirst());
        pageData.put("last", page.isLast());
        
        response.put("data", pageData);
        return response;
    }
}
