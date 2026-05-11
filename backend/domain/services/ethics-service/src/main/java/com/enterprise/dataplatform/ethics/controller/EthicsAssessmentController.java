package com.enterprise.dataplatform.ethics.controller;

import com.enterprise.dataplatform.ethics.domain.dto.request.EthicsAssessmentRequest;
import com.enterprise.dataplatform.ethics.domain.dto.response.EthicsAssessmentResponse;
import com.enterprise.dataplatform.ethics.service.EthicsAssessmentService;
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
import java.util.Map;

@RestController
@RequestMapping("/api/v1/ethics/assessments")
@RequiredArgsConstructor
@Tag(name = "伦理评估管理", description = "数据伦理评估的创建和管理")
public class EthicsAssessmentController {

    private final EthicsAssessmentService assessmentService;

    @PostMapping
    @Operation(summary = "创建伦理评估")
    public ResponseEntity<Map<String, Object>> createAssessment(
            @Valid @RequestBody EthicsAssessmentRequest request,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        EthicsAssessmentResponse response = assessmentService.createAssessment(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(wrapResponse(response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取评估结果")
    public ResponseEntity<Map<String, Object>> getAssessment(@PathVariable Long id) {
        EthicsAssessmentResponse response = assessmentService.getAssessment(id);
        return ResponseEntity.ok(wrapResponse(response));
    }

    @GetMapping("/code/{assessmentCode}")
    @Operation(summary = "根据编码获取评估")
    public ResponseEntity<Map<String, Object>> getAssessmentByCode(@PathVariable String assessmentCode) {
        EthicsAssessmentResponse response = assessmentService.getAssessmentByCode(assessmentCode);
        return ResponseEntity.ok(wrapResponse(response));
    }

    @GetMapping("/asset/{assetId}")
    @Operation(summary = "获取资产伦理评估历史")
    public ResponseEntity<Map<String, Object>> getAssetAssessmentHistory(
            @PathVariable String assetId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("assessedAt").descending());
        Page<EthicsAssessmentResponse> result = assessmentService.getAssetAssessmentHistory(assetId, pageable);
        return ResponseEntity.ok(wrapPageResponse(result));
    }

    @GetMapping
    @Operation(summary = "查询评估列表")
    public ResponseEntity<Map<String, Object>> searchAssessments(
            @RequestParam(required = false) String assetId,
            @RequestParam(required = false) Long frameworkId,
            @RequestParam(required = false) String riskLevel,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("assessedAt").descending());
        Page<EthicsAssessmentResponse> result = assessmentService.searchAssessments(
                assetId, frameworkId, riskLevel, status, pageable);
        return ResponseEntity.ok(wrapPageResponse(result));
    }

    @GetMapping("/asset/{assetId}/average-score")
    @Operation(summary = "获取资产平均伦理评分")
    public ResponseEntity<Map<String, Object>> getAverageScoreByAsset(@PathVariable String assetId) {
        Double avgScore = assessmentService.getAverageScoreByAsset(assetId);
        return ResponseEntity.ok(wrapResponse(Map.of("assetId", assetId, "averageScore", avgScore)));
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
