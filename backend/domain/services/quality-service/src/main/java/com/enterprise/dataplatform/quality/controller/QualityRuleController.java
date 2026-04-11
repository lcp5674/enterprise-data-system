package com.enterprise.dataplatform.quality.controller;

import com.enterprise.dataplatform.quality.dto.request.QualityRuleRequest;
import com.enterprise.dataplatform.quality.dto.response.QualityRuleResponse;
import com.enterprise.dataplatform.quality.service.QualityRuleService;
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
 * 质量规则REST控制器
 */
@RestController
@RequestMapping("/api/v1/quality/rules")
@RequiredArgsConstructor
@Tag(name = "质量规则管理", description = "数据质量规则的定义和管理")
public class QualityRuleController {

    private final QualityRuleService ruleService;

    @PostMapping
    @Operation(summary = "创建质量规则")
    public ResponseEntity<Map<String, Object>> createRule(
            @Valid @RequestBody QualityRuleRequest request,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        QualityRuleResponse response = ruleService.createRule(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(wrapResponse(response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新质量规则")
    public ResponseEntity<Map<String, Object>> updateRule(
            @PathVariable Long id,
            @Valid @RequestBody QualityRuleRequest request,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        QualityRuleResponse response = ruleService.updateRule(id, request, userId);
        return ResponseEntity.ok(wrapResponse(response));
    }

    @PostMapping("/{id}/publish")
    @Operation(summary = "发布质量规则")
    public ResponseEntity<Map<String, Object>> publishRule(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        QualityRuleResponse response = ruleService.publishRule(id, userId);
        return ResponseEntity.ok(wrapResponse(response));
    }

    @PostMapping("/{id}/enable")
    @Operation(summary = "启用质量规则")
    public ResponseEntity<Map<String, Object>> enableRule(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        QualityRuleResponse response = ruleService.setRuleEnabled(id, true, userId);
        return ResponseEntity.ok(wrapResponse(response));
    }

    @PostMapping("/{id}/disable")
    @Operation(summary = "禁用质量规则")
    public ResponseEntity<Map<String, Object>> disableRule(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        QualityRuleResponse response = ruleService.setRuleEnabled(id, false, userId);
        return ResponseEntity.ok(wrapResponse(response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询质量规则")
    public ResponseEntity<Map<String, Object>> getRule(@PathVariable Long id) {
        QualityRuleResponse response = ruleService.getRule(id);
        return ResponseEntity.ok(wrapResponse(response));
    }

    @GetMapping("/code/{ruleCode}")
    @Operation(summary = "根据编码查询规则")
    public ResponseEntity<Map<String, Object>> getRuleByCode(@PathVariable String ruleCode) {
        QualityRuleResponse response = ruleService.getRuleByCode(ruleCode);
        return ResponseEntity.ok(wrapResponse(response));
    }

    @GetMapping
    @Operation(summary = "分页查询规则")
    public ResponseEntity<Map<String, Object>> searchRules(
            @RequestParam(required = false) String ruleType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createTime").descending());
        Page<QualityRuleResponse> result = ruleService.searchRules(ruleType, status, keyword, pageable);
        return ResponseEntity.ok(wrapPageResponse(result));
    }

    @GetMapping("/enabled")
    @Operation(summary = "获取启用的规则")
    public ResponseEntity<Map<String, Object>> getEnabledRules() {
        List<QualityRuleResponse> responses = ruleService.getEnabledRules();
        return ResponseEntity.ok(wrapResponse(responses));
    }

    @GetMapping("/asset/{assetId}")
    @Operation(summary = "根据资产获取规则")
    public ResponseEntity<Map<String, Object>> getRulesByAssetId(@PathVariable String assetId) {
        List<QualityRuleResponse> responses = ruleService.getRulesByAssetId(assetId);
        return ResponseEntity.ok(wrapResponse(responses));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除规则")
    public ResponseEntity<Map<String, Object>> deleteRule(@PathVariable Long id) {
        ruleService.deleteRule(id);
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
