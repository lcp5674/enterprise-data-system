package com.enterprise.edams.sandbox.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.edams.sandbox.dto.*;
import com.enterprise.edams.sandbox.entity.*;
import com.enterprise.edams.sandbox.service.SandboxService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/sandbox")
@Tag(name = "沙箱管理", description = "数据沙箱与样本脱敏接口")
public class SandboxController {
    
    private final SandboxService sandboxService;
    
    @PostMapping("/instance")
    @Operation(summary = "创建沙箱", description = "创建新的沙箱实例")
    public SandboxInstance createSandbox(@RequestBody SandboxCreateRequest request) {
        return sandboxService.createSandbox(request);
    }
    
    @GetMapping("/instance/{id}")
    @Operation(summary = "获取实例详情", description = "获取沙箱实例详情")
    public SandboxInstance getInstance(
            @Parameter(description = "实例ID") @PathVariable Long id) {
        return sandboxService.getInstance(id);
    }
    
    @GetMapping("/instances")
    @Operation(summary = "查询实例列表", description = "分页查询沙箱实例")
    public Page<SandboxInstance> searchInstances(InstanceSearchRequest request) {
        return sandboxService.searchInstances(request);
    }
    
    @PostMapping("/instance/{id}/start")
    @Operation(summary = "启动实例", description = "启动沙箱实例")
    public SandboxInstance startInstance(
            @Parameter(description = "实例ID") @PathVariable Long id) {
        return sandboxService.startInstance(id);
    }
    
    @PostMapping("/instance/{id}/stop")
    @Operation(summary = "停止实例", description = "停止沙箱实例")
    public SandboxInstance stopInstance(
            @Parameter(description = "实例ID") @PathVariable Long id) {
        return sandboxService.stopInstance(id);
    }
    
    @DeleteMapping("/instance/{id}")
    @Operation(summary = "删除实例", description = "删除沙箱实例")
    public void deleteInstance(
            @Parameter(description = "实例ID") @PathVariable Long id) {
        sandboxService.deleteInstance(id);
    }
    
    @PostMapping("/sample")
    @Operation(summary = "申请样本数据", description = "申请脱敏后的样本数据")
    public SampleDataRequest requestSampleData(@RequestBody SampleDataRequestDto request) {
        return sandboxService.requestSampleData(request);
    }
    
    @GetMapping("/samples")
    @Operation(summary = "查询样本申请", description = "分页查询样本数据申请记录")
    public Page<SampleDataRequest> searchSampleRequests(SampleSearchRequest request) {
        return sandboxService.searchSampleRequests(request);
    }
    
    @PostMapping("/desensitization/rule")
    @Operation(summary = "创建脱敏规则", description = "创建数据脱敏规则")
    public DesensitizationRule createRule(@RequestBody DesensitizationRuleDto request) {
        return sandboxService.createDesensitizationRule(request);
    }
    
    @GetMapping("/desensitization/rules")
    @Operation(summary = "获取脱敏规则", description = "获取所有脱敏规则")
    public List<DesensitizationRule> getRules() {
        return sandboxService.getDesensitizationRules();
    }
    
    @GetMapping("/desensitization/preview")
    @Operation(summary = "预览脱敏效果", description = "预览脱敏效果")
    public String previewDesensitization(
            @RequestParam Long ruleId,
            @RequestParam String originalValue) {
        return sandboxService.previewDesensitization(ruleId, originalValue);
    }
}
