package com.enterprise.dataplatform.standard.controller;

import com.enterprise.dataplatform.standard.dto.request.ComplianceCheckRequest;
import com.enterprise.dataplatform.standard.dto.response.ComplianceCheckResponse;
import com.enterprise.dataplatform.standard.service.ComplianceCheckService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 合规检查REST控制器
 */
@RestController
@RequestMapping("/api/v1/compliance")
@RequiredArgsConstructor
@Tag(name = "合规检查管理", description = "数据标准合规性检查")
public class ComplianceController {

    private final ComplianceCheckService checkService;

    /**
     * 执行合规检查
     */
    @PostMapping("/check")
    @Operation(summary = "执行合规检查", description = "对指定的标准和资产执行合规性检查")
    public ResponseEntity<Map<String, Object>> executeCheck(
            @Valid @RequestBody ComplianceCheckRequest request,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String executor) {
        
        // 生成批次号
        if (request.getBatchNo() == null || request.getBatchNo().isEmpty()) {
            request.setBatchNo("CHECK-" + UUID.randomUUID().toString().substring(0, 8));
        }
        
        ComplianceCheckResponse response = checkService.executeCheck(request, executor);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(wrapResponse(response));
    }

    /**
     * 批量执行检查
     */
    @PostMapping("/check/batch")
    @Operation(summary = "批量执行检查", description = "对多个标准或多个资产执行批量检查")
    public ResponseEntity<Map<String, Object>> batchExecuteCheck(
            @RequestParam List<Long> standardIds,
            @RequestParam String assetId,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String executor) {
        
        String batchNo = "BATCH-" + UUID.randomUUID().toString().substring(0, 8);
        List<ComplianceCheckResponse> responses = checkService.batchExecuteCheck(
                batchNo, standardIds, assetId, executor);
        
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(wrapResponse(responses));
    }

    /**
     * 查询检查结果
     */
    @GetMapping("/check/{id}")
    @Operation(summary = "查询检查结果", description = "根据ID查询合规检查结果")
    public ResponseEntity<Map<String, Object>> getCheck(@PathVariable Long id) {
        ComplianceCheckResponse response = checkService.getCheck(id);
        return ResponseEntity.ok(wrapResponse(response));
    }

    /**
     * 根据批次号查询
     */
    @GetMapping("/check/batch/{batchNo}")
    @Operation(summary = "根据批次查询", description = "根据批次号查询所有检查结果")
    public ResponseEntity<Map<String, Object>> getChecksByBatchNo(@PathVariable String batchNo) {
        List<ComplianceCheckResponse> responses = checkService.getChecksByBatchNo(batchNo);
        return ResponseEntity.ok(wrapResponse(responses));
    }

    /**
     * 分页查询检查记录
     */
    @GetMapping("/checks")
    @Operation(summary = "分页查询检查记录", description = "支持多条件分页查询检查记录")
    public ResponseEntity<Map<String, Object>> searchChecks(
            @RequestParam(required = false) Long standardId,
            @RequestParam(required = false) String assetId,
            @RequestParam(required = false) String checkResult,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "checkTime") String sortBy,
            @RequestParam(defaultValue = "desc") String sortOrder) {
        
        Sort sort = sortOrder.equalsIgnoreCase("asc") ? 
                Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<ComplianceCheckResponse> result = checkService.searchChecks(
                standardId, assetId, checkResult, startTime, endTime, pageable);
        
        return ResponseEntity.ok(wrapPageResponse(result));
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
