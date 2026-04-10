package com.enterprise.edams.value.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.edams.value.dto.AssessmentSearchRequest;
import com.enterprise.edams.value.entity.ValueAssessment;
import com.enterprise.edams.value.service.ValueAssessmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 数据价值评估控制器
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/value/assessments")
@Tag(name = "数据价值评估", description = "数据资产价值评估相关接口")
public class ValueAssessmentController {
    
    private final ValueAssessmentService valueAssessmentService;
    
    @PostMapping
    @Operation(summary = "创建评估", description = "为指定资产创建价值评估任务")
    public ValueAssessment createAssessment(
            @Parameter(description = "资产ID") @RequestParam Long assetId) {
        return valueAssessmentService.createAssessment(assetId);
    }
    
    @PostMapping("/{id}/execute")
    @Operation(summary = "执行评估", description = "执行价值评估计算")
    public ValueAssessment executeAssessment(
            @Parameter(description = "评估ID") @PathVariable Long id) {
        return valueAssessmentService.executeAssessment(id);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "获取评估详情", description = "获取评估详细信息")
    public ValueAssessment getAssessment(
            @Parameter(description = "评估ID") @PathVariable Long id) {
        return valueAssessmentService.getAssessmentById(id);
    }
    
    @GetMapping
    @Operation(summary = "查询评估列表", description = "分页查询评估记录")
    public Page<ValueAssessment> searchAssessments(AssessmentSearchRequest request) {
        return valueAssessmentService.searchAssessments(request);
    }
    
    @GetMapping("/asset/{assetId}/latest")
    @Operation(summary = "获取资产最新评估", description = "获取指定资产的最新评估结果")
    public ValueAssessment getLatestAssessment(
            @Parameter(description = "资产ID") @PathVariable Long assetId) {
        return valueAssessmentService.getLatestAssessment(assetId);
    }
    
    @PostMapping("/batch")
    @Operation(summary = "批量评估", description = "批量评估多个资产")
    public List<ValueAssessment> batchAssess(
            @RequestBody List<Long> assetIds) {
        return valueAssessmentService.batchAssess(assetIds);
    }
    
    @GetMapping("/statistics/distribution")
    @Operation(summary = "价值分布统计", description = "获取价值分布统计信息")
    public Map<String, Object> getValueDistribution() {
        return valueAssessmentService.getValueDistribution();
    }
    
    @GetMapping("/top")
    @Operation(summary = "TOP价值资产", description = "获取价值最高的资产列表")
    public List<ValueAssessment> getTopValueAssets(
            @Parameter(description = "数量") @RequestParam(defaultValue = "10") int topN) {
        return valueAssessmentService.getTopValueAssets(topN);
    }
}
