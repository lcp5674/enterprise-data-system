package com.edams.value.controller;

import com.edams.common.model.ApiResponse;
import com.edams.common.model.PageResult;
import com.edams.value.entity.DataValue;
import com.edams.value.entity.ValueMetric;
import com.edams.value.service.ValueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@Tag(name = "数据价值评估管理")
@RestController
@RequestMapping("/api/value")
@RequiredArgsConstructor
public class ValueController {
    
    private final ValueService valueService;
    
    @Operation(summary = "评估数据资产价值")
    @PostMapping("/assess")
    public ApiResponse<DataValue> assessDataValue(
            @Parameter(description = "资产ID", required = true) @RequestParam Long assetId,
            @Parameter(description = "评估者ID", required = true) @RequestParam Long assessorId) {
        return ApiResponse.success(valueService.assessDataValue(assetId, assessorId));
    }
    
    @Operation(summary = "获取价值评估详情")
    @GetMapping("/{id}")
    public ApiResponse<DataValue> getValue(@PathVariable Long id) {
        return ApiResponse.success(valueService.getValueById(id));
    }
    
    @Operation(summary = "分页查询价值评估列表")
    @GetMapping("/list")
    public ApiResponse<PageResult<DataValue>> listValues(
            @RequestParam(required = false) Long assetId,
            @RequestParam(required = false) String assetType,
            @RequestParam(required = false) String valueCategory,
            @RequestParam(required = false) Long assessorId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Double minScore,
            @RequestParam(required = false) Double maxScore,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "assessmentDate,desc") String[] sort) {
        
        Map<String, Object> params = new HashMap<>();
        params.put("assetId", assetId);
        params.put("assetType", assetType);
        params.put("valueCategory", valueCategory);
        params.put("assessorId", assessorId);
        params.put("status", status);
        params.put("minScore", minScore);
        params.put("maxScore", maxScore);
        params.put("page", page);
        params.put("size", size);
        params.put("sort", sort);
        
        return ApiResponse.success(valueService.listValues(params));
    }
    
    @Operation(summary = "更新价值评估")
    @PutMapping("/{id}")
    public ApiResponse<DataValue> updateValue(
            @PathVariable Long id,
            @Valid @RequestBody DataValue value) {
        return ApiResponse.success(valueService.updateValue(id, value));
    }
    
    @Operation(summary = "删除价值评估")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteValue(@PathVariable Long id) {
        valueService.deleteValue(id);
        return ApiResponse.success();
    }
    
    @Operation(summary = "创建价值度量标准")
    @PostMapping("/metric")
    public ApiResponse<ValueMetric> createMetric(@Valid @RequestBody ValueMetric metric) {
        return ApiResponse.success(valueService.createMetric(metric));
    }
    
    @Operation(summary = "更新价值度量标准")
    @PutMapping("/metric/{id}")
    public ApiResponse<ValueMetric> updateMetric(
            @PathVariable Long id,
            @Valid @RequestBody ValueMetric metric) {
        return ApiResponse.success(valueService.updateMetric(id, metric));
    }
    
    @Operation(summary = "删除价值度量标准")
    @DeleteMapping("/metric/{id}")
    public ApiResponse<Void> deleteMetric(@PathVariable Long id) {
        valueService.deleteMetric(id);
        return ApiResponse.success();
    }
    
    @Operation(summary = "获取价值度量标准详情")
    @GetMapping("/metric/{id}")
    public ApiResponse<ValueMetric> getMetric(@PathVariable Long id) {
        return ApiResponse.success(valueService.getMetricById(id));
    }
    
    @Operation(summary = "分页查询价值度量标准列表")
    @GetMapping("/metric/list")
    public ApiResponse<PageResult<ValueMetric>> listMetrics(
            @RequestParam(required = false) String metricType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String metricName,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        
        Map<String, Object> params = new HashMap<>();
        params.put("metricType", metricType);
        params.put("status", status);
        params.put("metricName", metricName);
        params.put("page", page);
        params.put("size", size);
        
        return ApiResponse.success(valueService.listMetrics(params));
    }
    
    @Operation(summary = "分析资产价值")
    @GetMapping("/asset/{assetId}/analysis")
    public ApiResponse<Map<String, Object>> analyzeAssetValue(@PathVariable Long assetId) {
        return ApiResponse.success(valueService.analyzeAssetValue(assetId));
    }
    
    @Operation(summary = "分析价值趋势")
    @GetMapping("/asset/{assetId}/trend")
    public ApiResponse<Map<String, Object>> analyzeTrend(@PathVariable Long assetId) {
        return ApiResponse.success(valueService.analyzeTrend(assetId));
    }
    
    @Operation(summary = "比较资产价值")
    @GetMapping("/compare")
    public ApiResponse<Map<String, Object>> compareValues(
            @Parameter(description = "资产1ID", required = true) @RequestParam Long assetId1,
            @Parameter(description = "资产2ID", required = true) @RequestParam Long assetId2) {
        return ApiResponse.success(valueService.compareValues(assetId1, assetId2));
    }
    
    @Operation(summary = "预测未来价值")
    @GetMapping("/asset/{assetId}/predict")
    public ApiResponse<Map<String, Object>> predictFutureValue(@PathVariable Long assetId) {
        return ApiResponse.success(valueService.predictFutureValue(assetId));
    }
    
    @Operation(summary = "计算价值趋势")
    @PostMapping("/calculate-trends")
    public ApiResponse<Void> calculateTrends() {
        valueService.calculateValueTrends();
        return ApiResponse.success();
    }
    
    @Operation(summary = "生成价值报告")
    @GetMapping("/asset/{assetId}/report")
    public ApiResponse<Map<String, Object>> generateValueReport(@PathVariable Long assetId) {
        return ApiResponse.success(valueService.generateValueReport(assetId));
    }
}