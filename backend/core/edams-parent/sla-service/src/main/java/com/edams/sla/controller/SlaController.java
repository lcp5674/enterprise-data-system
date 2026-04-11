package com.edams.sla.controller;

import com.edams.common.model.ApiResponse;
import com.edams.common.model.PageResult;
import com.edams.sla.entity.SlaAgreement;
import com.edams.sla.entity.SlaReport;
import com.edams.sla.service.SlaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@Tag(name = "SLA监控管理")
@RestController
@RequestMapping("/api/sla")
@RequiredArgsConstructor
public class SlaController {
    
    private final SlaService slaService;
    
    @Operation(summary = "创建SLA协议")
    @PostMapping("/agreement")
    public ApiResponse<SlaAgreement> createAgreement(@Valid @RequestBody SlaAgreement agreement) {
        return ApiResponse.success(slaService.createAgreement(agreement));
    }
    
    @Operation(summary = "更新SLA协议")
    @PutMapping("/agreement/{id}")
    public ApiResponse<SlaAgreement> updateAgreement(
            @PathVariable Long id,
            @Valid @RequestBody SlaAgreement agreement) {
        return ApiResponse.success(slaService.updateAgreement(id, agreement));
    }
    
    @Operation(summary = "删除SLA协议")
    @DeleteMapping("/agreement/{id}")
    public ApiResponse<Void> deleteAgreement(@PathVariable Long id) {
        slaService.deleteAgreement(id);
        return ApiResponse.success();
    }
    
    @Operation(summary = "获取SLA协议详情")
    @GetMapping("/agreement/{id}")
    public ApiResponse<SlaAgreement> getAgreement(@PathVariable Long id) {
        return ApiResponse.success(slaService.getAgreementById(id));
    }
    
    @Operation(summary = "分页查询SLA协议列表")
    @GetMapping("/agreement/list")
    public ApiResponse<PageResult<SlaAgreement>> listAgreements(
            @RequestParam(required = false) String serviceName,
            @RequestParam(required = false) String serviceType,
            @RequestParam(required = false) Long ownerId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String metricType,
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "createdTime,desc") String[] sort) {
        
        Map<String, Object> params = new HashMap<>();
        params.put("serviceName", serviceName);
        params.put("serviceType", serviceType);
        params.put("ownerId", ownerId);
        params.put("status", status);
        params.put("metricType", metricType);
        params.put("name", name);
        params.put("page", page);
        params.put("size", size);
        params.put("sort", sort);
        
        return ApiResponse.success(slaService.listAgreements(params));
    }
    
    @Operation(summary = "监控SLA协议")
    @PostMapping("/agreement/{id}/monitor")
    public ApiResponse<Void> monitorAgreement(@PathVariable Long id) {
        slaService.monitorSla(id);
        return ApiResponse.success();
    }
    
    @Operation(summary = "检查SLA合规性")
    @GetMapping("/agreement/{id}/compliance")
    public ApiResponse<Map<String, Object>> checkCompliance(@PathVariable Long id) {
        return ApiResponse.success(slaService.checkSlaCompliance(id));
    }
    
    @Operation(summary = "生成SLA报告")
    @PostMapping("/agreement/{id}/report")
    public ApiResponse<Void> generateReport(
            @PathVariable Long id,
            @RequestParam(defaultValue = "DAILY") String period,
            @Parameter(description = "生成用户ID", required = true) @RequestParam Long userId) {
        slaService.generateReport(id, period, userId);
        return ApiResponse.success();
    }
    
    @Operation(summary = "获取SLA报告详情")
    @GetMapping("/report/{id}")
    public ApiResponse<SlaReport> getReport(@PathVariable Long id) {
        return ApiResponse.success(slaService.getReportById(id));
    }
    
    @Operation(summary = "分页查询SLA报告列表")
    @GetMapping("/report/list")
    public ApiResponse<PageResult<SlaReport>> listReports(
            @RequestParam(required = false) Long agreementId,
            @RequestParam(required = false) String reportPeriod,
            @RequestParam(required = false) String analysisResult,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        
        Map<String, Object> params = new HashMap<>();
        params.put("agreementId", agreementId);
        params.put("reportPeriod", reportPeriod);
        params.put("analysisResult", analysisResult);
        params.put("page", page);
        params.put("size", size);
        
        return ApiResponse.success(slaService.listReports(params));
    }
    
    @Operation(summary = "发送报告通知")
    @PostMapping("/report/{id}/notify")
    public ApiResponse<Void> sendReportNotification(@PathVariable Long id) {
        slaService.sendReportNotification(id);
        return ApiResponse.success();
    }
    
    @Operation(summary = "获取SLA统计信息")
    @GetMapping("/agreement/{id}/stats")
    public ApiResponse<Map<String, Object>> getSlaStats(@PathVariable Long id) {
        return ApiResponse.success(slaService.getSlaStats(id));
    }
    
    @Operation(summary = "获取服务SLA统计")
    @GetMapping("/service/{serviceName}/stats")
    public ApiResponse<Map<String, Object>> getServiceStats(@PathVariable String serviceName) {
        return ApiResponse.success(slaService.getServiceSlaStats(serviceName));
    }
    
    @Operation(summary = "检查SLA违规")
    @PostMapping("/violations/check")
    public ApiResponse<Void> checkViolations() {
        slaService.checkViolations();
        return ApiResponse.success();
    }
    
    @Operation(summary = "发送SLA违规警报")
    @PostMapping("/agreement/{id}/alert")
    public ApiResponse<Void> sendViolationAlert(@PathVariable Long id) {
        slaService.sendViolationAlert(id);
        return ApiResponse.success();
    }
}