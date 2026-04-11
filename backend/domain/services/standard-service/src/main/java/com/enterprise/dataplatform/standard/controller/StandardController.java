package com.enterprise.dataplatform.standard.controller;

import com.enterprise.dataplatform.standard.dto.request.DataStandardRequest;
import com.enterprise.dataplatform.standard.dto.response.DataStandardResponse;
import com.enterprise.dataplatform.standard.service.DataStandardService;
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
 * 数据标准REST控制器
 */
@RestController
@RequestMapping("/api/v1/standards")
@RequiredArgsConstructor
@Tag(name = "数据标准管理", description = "数据标准的定义、发布和管理")
public class StandardController {

    private final DataStandardService standardService;

    /**
     * 创建数据标准
     */
    @PostMapping
    @Operation(summary = "创建数据标准", description = "创建一个新的数据标准")
    public ResponseEntity<Map<String, Object>> createStandard(
            @Valid @RequestBody DataStandardRequest request,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        DataStandardResponse response = standardService.createStandard(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(wrapResponse(response));
    }

    /**
     * 更新数据标准
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新数据标准", description = "更新指定的数据标准")
    public ResponseEntity<Map<String, Object>> updateStandard(
            @PathVariable Long id,
            @Valid @RequestBody DataStandardRequest request,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        DataStandardResponse response = standardService.updateStandard(id, request, userId);
        return ResponseEntity.ok(wrapResponse(response));
    }

    /**
     * 发布数据标准
     */
    @PostMapping("/{id}/publish")
    @Operation(summary = "发布数据标准", description = "将草稿状态的标准发布为激活状态")
    public ResponseEntity<Map<String, Object>> publishStandard(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        DataStandardResponse response = standardService.publishStandard(id, userId);
        return ResponseEntity.ok(wrapResponse(response));
    }

    /**
     * 废弃数据标准
     */
    @PostMapping("/{id}/deprecate")
    @Operation(summary = "废弃数据标准", description = "将激活的标准标记为废弃状态")
    public ResponseEntity<Map<String, Object>> deprecateStandard(
            @PathVariable Long id,
            @RequestParam(required = false) String reason,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        DataStandardResponse response = standardService.deprecateStandard(id, userId, reason);
        return ResponseEntity.ok(wrapResponse(response));
    }

    /**
     * 查询数据标准
     */
    @GetMapping("/{id}")
    @Operation(summary = "查询数据标准", description = "根据ID查询数据标准详情")
    public ResponseEntity<Map<String, Object>> getStandard(@PathVariable Long id) {
        DataStandardResponse response = standardService.getStandard(id);
        return ResponseEntity.ok(wrapResponse(response));
    }

    /**
     * 根据编码查询数据标准
     */
    @GetMapping("/code/{standardCode}")
    @Operation(summary = "根据编码查询数据标准", description = "根据标准编码查询数据标准")
    public ResponseEntity<Map<String, Object>> getStandardByCode(@PathVariable String standardCode) {
        DataStandardResponse response = standardService.getStandardByCode(standardCode);
        return ResponseEntity.ok(wrapResponse(response));
    }

    /**
     * 分页查询数据标准
     */
    @GetMapping
    @Operation(summary = "分页查询数据标准", description = "支持多条件分页查询")
    public ResponseEntity<Map<String, Object>> searchStandards(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String standardType,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createTime") String sortBy,
            @RequestParam(defaultValue = "desc") String sortOrder) {
        
        Sort sort = sortOrder.equalsIgnoreCase("asc") ? 
                Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<DataStandardResponse> result = standardService.searchStandards(
                category, status, standardType, keyword, pageable);
        
        return ResponseEntity.ok(wrapPageResponse(result));
    }

    /**
     * 查询所有激活的标准
     */
    @GetMapping("/active")
    @Operation(summary = "查询激活的标准", description = "获取所有处于激活状态的数据标准")
    public ResponseEntity<Map<String, Object>> getActiveStandards() {
        List<DataStandardResponse> response = standardService.getAllActiveStandards();
        return ResponseEntity.ok(wrapResponse(response));
    }

    /**
     * 删除数据标准
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除数据标准", description = "删除指定的数据标准")
    public ResponseEntity<Map<String, Object>> deleteStandard(@PathVariable Long id) {
        standardService.deleteStandard(id);
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
