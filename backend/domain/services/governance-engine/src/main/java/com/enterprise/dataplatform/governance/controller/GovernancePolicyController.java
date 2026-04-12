package com.enterprise.dataplatform.governance.controller;

import com.enterprise.dataplatform.governance.domain.entity.GovernancePolicy;
import com.enterprise.dataplatform.governance.dto.request.GovernancePolicyRequest;
import com.enterprise.dataplatform.governance.dto.response.GovernancePolicyResponse;
import com.enterprise.dataplatform.governance.service.GovernanceOrchestrationService;
import com.enterprise.dataplatform.governance.service.AIRecommendationService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v1/governance/policies")
@RequiredArgsConstructor
@Tag(name = "治理策略管理", description = "治理策略的CRUD和执行管理")
public class GovernancePolicyController {

    private final GovernanceOrchestrationService orchestrationService;
    private final AIRecommendationService recommendationService;

    @PostMapping
    @Operation(summary = "创建治理策略", description = "创建新的治理策略")
    public ResponseEntity<GovernancePolicyResponse> createPolicy(
            @Validated @RequestBody GovernancePolicyRequest request) {
        log.info("创建治理策略: {}", request.getName());
        GovernancePolicy policy = orchestrationService.createPolicy(request);
        return ResponseEntity.ok(toPolicyResponse(policy));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新治理策略", description = "更新现有治理策略")
    public ResponseEntity<GovernancePolicyResponse> updatePolicy(
            @PathVariable Long id,
            @Validated @RequestBody GovernancePolicyRequest request) {
        log.info("更新治理策略: {}", id);
        GovernancePolicy policy = orchestrationService.updatePolicy(id, request);
        return ResponseEntity.ok(toPolicyResponse(policy));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除治理策略", description = "删除治理策略")
    public ResponseEntity<Void> deletePolicy(@PathVariable Long id) {
        log.info("删除治理策略: {}", id);
        orchestrationService.deletePolicy(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取策略详情", description = "根据ID获取策略详情")
    public ResponseEntity<GovernancePolicyResponse> getPolicy(@PathVariable Long id) {
        log.info("获取策略详情: {}", id);
        return orchestrationService.getPolicyById(id)
                .map(this::toPolicyResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "分页查询策略", description = "分页查询治理策略")
    public ResponseEntity<IPage<GovernancePolicyResponse>> listPolicies(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String policyType,
            @RequestParam(required = false) String assetType,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean enabled) {
        log.info("分页查询策略: pageNum={}, pageSize={}, policyType={}", pageNum, pageSize, policyType);
        Page<GovernancePolicy> page = orchestrationService.listPolicies(
                pageNum, pageSize, policyType, assetType, keyword, enabled);
        IPage<GovernancePolicyResponse> responsePage = page.convert(this::toPolicyResponse);
        return ResponseEntity.ok(responsePage);
    }

    @GetMapping("/types")
    @Operation(summary = "获取策略类型列表", description = "获取所有可用的策略类型")
    public ResponseEntity<List<Map<String, Object>>> getPolicyTypes() {
        return ResponseEntity.ok(orchestrationService.getAvailablePolicyTypes());
    }

    @PostMapping("/{id}/enable")
    @Operation(summary = "启用策略", description = "启用指定的治理策略")
    public ResponseEntity<GovernancePolicyResponse> enablePolicy(@PathVariable Long id) {
        log.info("启用策略: {}", id);
        return orchestrationService.getPolicyById(id)
                .map(policy -> {
                    policy.setEnabled(true);
                    return orchestrationService.updatePolicyStatus(id, true);
                })
                .map(this::toPolicyResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/disable")
    @Operation(summary = "禁用策略", description = "禁用指定的治理策略")
    public ResponseEntity<GovernancePolicyResponse> disablePolicy(@PathVariable Long id) {
        log.info("禁用策略: {}", id);
        return orchestrationService.getPolicyById(id)
                .map(policy -> {
                    policy.setEnabled(false);
                    return orchestrationService.updatePolicyStatus(id, false);
                })
                .map(this::toPolicyResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/recommend")
    @Operation(summary = "AI推荐策略", description = "基于AI推荐适用的治理策略")
    public ResponseEntity<List<GovernancePolicyResponse>> recommendPolicies(
            @RequestParam String assetType,
            @RequestParam String domain,
            @RequestParam(required = false) String assetId) {
        log.info("AI推荐策略: assetType={}, domain={}", assetType, domain);
        List<GovernancePolicy> policies = recommendationService.recommendPoliciesForAsset(
                assetType, domain, assetId);
        List<GovernancePolicyResponse> responses = policies.stream()
                .map(this::toPolicyResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    private GovernancePolicyResponse toPolicyResponse(GovernancePolicy policy) {
        return GovernancePolicyResponse.builder()
                .id(policy.getId())
                .name(policy.getName())
                .policyType(policy.getPolicyType())
                .description(policy.getDescription())
                .assetType(policy.getAssetType())
                .assetSubType(policy.getAssetSubType())
                .priority(policy.getPriority())
                .priorityLabel(getPriorityLabel(policy.getPriority()))
                .triggerCondition(policy.getTriggerCondition())
                .actionDefinition(policy.getActionDefinition())
                .parameters(policy.getParameters())
                .applicableRoles(policy.getApplicableRoles())
                .enabled(policy.getEnabled())
                .status(policy.getEnabled() ? "active" : "inactive")
                .effectiveFrom(policy.getEffectiveFrom())
                .effectiveTo(policy.getEffectiveTo())
                .createdAt(policy.getCreatedAt())
                .updatedAt(policy.getUpdatedAt())
                .createdBy(policy.getCreatedBy())
                .build();
    }

    private String getPriorityLabel(Integer priority) {
        if (priority == null) return "未知";
        return switch (priority) {
            case 1 -> "紧急";
            case 2 -> "高";
            case 3 -> "中";
            case 4 -> "低";
            default -> "未知";
        };
    }
}
