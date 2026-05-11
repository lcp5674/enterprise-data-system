package com.enterprise.dataplatform.ethics.controller;

import com.enterprise.dataplatform.ethics.domain.dto.request.EthicsReviewRequest;
import com.enterprise.dataplatform.ethics.domain.dto.request.FairnessEvaluationRequest;
import com.enterprise.dataplatform.ethics.domain.dto.request.ReviewCommentRequest;
import com.enterprise.dataplatform.ethics.domain.dto.request.SocialImpactRequest;
import com.enterprise.dataplatform.ethics.domain.dto.response.EthicsReviewResponse;
import com.enterprise.dataplatform.ethics.domain.dto.response.FairnessEvaluationResponse;
import com.enterprise.dataplatform.ethics.domain.dto.response.SocialImpactResponse;
import com.enterprise.dataplatform.ethics.service.EthicsReviewService;
import com.enterprise.dataplatform.ethics.service.FairnessEvaluationService;
import com.enterprise.dataplatform.ethics.service.SocialResponsibilityService;
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
@RequestMapping("/api/v1/ethics")
@RequiredArgsConstructor
@Tag(name = "伦理审查管理", description = "数据伦理审查流程管理")
public class EthicsReviewController {

    private final EthicsReviewService reviewService;
    private final SocialResponsibilityService socialService;
    private final FairnessEvaluationService fairnessService;

    @PostMapping("/reviews")
    @Operation(summary = "创建伦理审查")
    public ResponseEntity<Map<String, Object>> createReview(
            @Valid @RequestBody EthicsReviewRequest request,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        EthicsReviewResponse response = reviewService.createReview(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(wrapResponse(response));
    }

    @PostMapping("/reviews/{id}/submit")
    @Operation(summary = "提交伦理审查")
    public ResponseEntity<Map<String, Object>> submitReview(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        EthicsReviewResponse response = reviewService.submitReview(id, userId);
        return ResponseEntity.ok(wrapResponse(response));
    }

    @PostMapping("/reviews/{id}/start-review")
    @Operation(summary = "开始审核")
    public ResponseEntity<Map<String, Object>> startReview(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        EthicsReviewResponse response = reviewService.startReview(id, userId);
        return ResponseEntity.ok(wrapResponse(response));
    }

    @PostMapping("/reviews/{id}/comments")
    @Operation(summary = "添加审查意见")
    public ResponseEntity<Map<String, Object>> addComment(
            @PathVariable Long id,
            @Valid @RequestBody ReviewCommentRequest request,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        EthicsReviewResponse response = reviewService.addReviewComment(id, request, userId);
        return ResponseEntity.ok(wrapResponse(response));
    }

    @PostMapping("/reviews/{id}/approve")
    @Operation(summary = "批准伦理审查")
    public ResponseEntity<Map<String, Object>> approveReview(
            @PathVariable Long id,
            @RequestParam(required = false) String approvalComments,
            @RequestBody(required = false) Map<String, List<String>> conditionsWrapper,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        List<String> conditions = conditionsWrapper != null ? conditionsWrapper.get("conditions") : null;
        EthicsReviewResponse response = reviewService.approveReview(id, userId, approvalComments, conditions);
        return ResponseEntity.ok(wrapResponse(response));
    }

    @PostMapping("/reviews/{id}/reject")
    @Operation(summary = "拒绝伦理审查")
    public ResponseEntity<Map<String, Object>> rejectReview(
            @PathVariable Long id,
            @RequestParam String rejectionReason,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        EthicsReviewResponse response = reviewService.rejectReview(id, userId, rejectionReason);
        return ResponseEntity.ok(wrapResponse(response));
    }

    @PostMapping("/reviews/{id}/request-revision")
    @Operation(summary = "要求修改")
    public ResponseEntity<Map<String, Object>> requestRevision(
            @PathVariable Long id,
            @RequestParam String revisionReason,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        EthicsReviewResponse response = reviewService.requestRevision(id, userId, revisionReason);
        return ResponseEntity.ok(wrapResponse(response));
    }

    @PostMapping("/reviews/{id}/execute")
    @Operation(summary = "执行审查")
    public ResponseEntity<Map<String, Object>> executeReview(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        EthicsReviewResponse response = reviewService.executeReview(id, userId);
        return ResponseEntity.ok(wrapResponse(response));
    }

    @PostMapping("/reviews/{id}/cancel")
    @Operation(summary = "取消审查")
    public ResponseEntity<Map<String, Object>> cancelReview(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        EthicsReviewResponse response = reviewService.cancelReview(id, userId);
        return ResponseEntity.ok(wrapResponse(response));
    }

    @GetMapping("/reviews/{id}")
    @Operation(summary = "获取审查详情")
    public ResponseEntity<Map<String, Object>> getReview(@PathVariable Long id) {
        EthicsReviewResponse response = reviewService.getReview(id);
        return ResponseEntity.ok(wrapResponse(response));
    }

    @GetMapping("/reviews/code/{reviewCode}")
    @Operation(summary = "根据编码获取审查")
    public ResponseEntity<Map<String, Object>> getReviewByCode(@PathVariable String reviewCode) {
        EthicsReviewResponse response = reviewService.getReviewByCode(reviewCode);
        return ResponseEntity.ok(wrapResponse(response));
    }

    @GetMapping("/reviews")
    @Operation(summary = "查询审查列表")
    public ResponseEntity<Map<String, Object>> searchReviews(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String reviewType,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String assetId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createTime").descending());
        Page<EthicsReviewResponse> result = reviewService.searchReviews(
                status, reviewType, priority, assetId, pageable);
        return ResponseEntity.ok(wrapPageResponse(result));
    }

    @GetMapping("/reviews/pending")
    @Operation(summary = "获取待处理审查")
    public ResponseEntity<Map<String, Object>> getPendingReviews(
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        List<EthicsReviewResponse> responses = reviewService.getPendingReviews(userId);
        return ResponseEntity.ok(wrapResponse(responses));
    }

    @PostMapping("/social-impact")
    @Operation(summary = "生成社会责任评估报告")
    public ResponseEntity<Map<String, Object>> generateSocialImpactReport(
            @Valid @RequestBody SocialImpactRequest request,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        SocialImpactResponse response = socialService.generateSocialImpactReport(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(wrapResponse(response));
    }

    @GetMapping("/social-impact/{id}")
    @Operation(summary = "获取社会责任报告")
    public ResponseEntity<Map<String, Object>> getSocialImpactReport(@PathVariable Long id) {
        SocialImpactResponse response = socialService.getReport(id);
        return ResponseEntity.ok(wrapResponse(response));
    }

    @GetMapping("/social-impact/asset/{assetId}")
    @Operation(summary = "获取资产社会责任报告列表")
    public ResponseEntity<Map<String, Object>> getAssetSocialReports(
            @PathVariable String assetId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("reportDate").descending());
        Page<SocialImpactResponse> result = socialService.getAssetReports(assetId, pageable);
        return ResponseEntity.ok(wrapPageResponse(result));
    }

    @PostMapping("/fairness-evaluation")
    @Operation(summary = "执行公平性评估")
    public ResponseEntity<Map<String, Object>> evaluateFairness(
            @Valid @RequestBody FairnessEvaluationRequest request) {
        FairnessEvaluationResponse response = fairnessService.evaluateFairness(request);
        return ResponseEntity.ok(wrapResponse(response));
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
