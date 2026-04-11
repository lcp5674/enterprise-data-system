package com.enterprise.dataplatform.ruleengine.controller;

import com.enterprise.dataplatform.ruleengine.domain.entity.RuleDefinition;
import com.enterprise.dataplatform.ruleengine.domain.entity.RuleExecutionLog;
import com.enterprise.dataplatform.ruleengine.dto.ApiResponse;
import com.enterprise.dataplatform.ruleengine.dto.RuleCreateRequest;
import com.enterprise.dataplatform.ruleengine.service.RuleEngineService;
import com.enterprise.dataplatform.ruleengine.service.RuleManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 规则管理API控制器
 * 提供规则的CRUD管理和规则执行日志查询接口
 */
@RestController
@RequestMapping("/api/rules")
@Tag(name = "规则管理", description = "规则定义与管理接口")
public class RuleManagementController {

    private static final Logger logger = LoggerFactory.getLogger(RuleManagementController.class);

    @Autowired
    private RuleManagementService ruleManagementService;

    @Autowired
    private RuleEngineService ruleEngineService;

    @GetMapping
    @Operation(summary = "获取规则列表", description = "分页获取规则定义列表，支持按分类和状态筛选")
    public ApiResponse<Page<RuleDefinition>> getRules(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdTime") String sort,
            @RequestParam(defaultValue = "DESC") String direction) {
        try {
            Sort.Direction sortDirection = "ASC".equalsIgnoreCase(direction) ?
                    Sort.Direction.ASC : Sort.Direction.DESC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
            Page<RuleDefinition> rules = ruleManagementService.getRules(category, status, pageable);
            return ApiResponse.success(rules);
        } catch (Exception e) {
            logger.error("获取规则列表失败", e);
            return ApiResponse.error("获取规则列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取规则详情", description = "根据ID获取规则定义详情")
    public ApiResponse<RuleDefinition> getRuleById(@PathVariable Long id) {
        try {
            RuleDefinition rule = ruleManagementService.getRuleById(id);
            return ApiResponse.success(rule);
        } catch (Exception e) {
            logger.error("获取规则详情失败: id={}", id, e);
            return ApiResponse.error("获取规则详情失败: " + e.getMessage());
        }
    }

    @PostMapping
    @Operation(summary = "创建规则", description = "创建新的规则定义")
    public ApiResponse<RuleDefinition> createRule(
            @Valid @RequestBody RuleCreateRequest request) {
        try {
            RuleDefinition rule = new RuleDefinition();
            rule.setRuleName(request.getRuleName());
            rule.setRuleCode(request.getRuleCode());
            rule.setCategory(request.getCategory());
            rule.setDescription(request.getDescription());
            rule.setRuleContent(request.getRuleContent());
            rule.setRuleFilePath(request.getRuleFilePath());
            rule.setStatus(request.getStatus());
            rule.setPriority(request.getPriority());
            rule.setTriggerCondition(request.getTriggerCondition());
            rule.setParameters(request.getParameters());

            RuleDefinition created = ruleManagementService.createRule(rule);
            return ApiResponse.success("规则创建成功", created);
        } catch (Exception e) {
            logger.error("创建规则失败", e);
            return ApiResponse.error("创建规则失败: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新规则", description = "更新已有的规则定义")
    public ApiResponse<RuleDefinition> updateRule(
            @PathVariable Long id,
            @Valid @RequestBody RuleCreateRequest request) {
        try {
            RuleDefinition rule = new RuleDefinition();
            rule.setRuleName(request.getRuleName());
            rule.setRuleCode(request.getRuleCode());
            rule.setCategory(request.getCategory());
            rule.setDescription(request.getDescription());
            rule.setRuleContent(request.getRuleContent());
            rule.setRuleFilePath(request.getRuleFilePath());
            rule.setStatus(request.getStatus());
            rule.setPriority(request.getPriority());
            rule.setTriggerCondition(request.getTriggerCondition());
            rule.setParameters(request.getParameters());

            RuleDefinition updated = ruleManagementService.updateRule(id, rule);
            return ApiResponse.success("规则更新成功", updated);
        } catch (Exception e) {
            logger.error("更新规则失败: id={}", id, e);
            return ApiResponse.error("更新规则失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除规则", description = "逻辑删除规则定义")
    public ApiResponse<Void> deleteRule(@PathVariable Long id) {
        try {
            ruleManagementService.deleteRule(id);
            return ApiResponse.success("规则删除成功", null);
        } catch (Exception e) {
            logger.error("删除规则失败: id={}", id, e);
            return ApiResponse.error("删除规则失败: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "切换规则状态", description = "启用/禁用/草稿状态切换")
    public ApiResponse<Void> toggleRuleStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        try {
            ruleManagementService.toggleRuleStatus(id, status);
            return ApiResponse.success("规则状态更新成功", null);
        } catch (Exception e) {
            logger.error("切换规则状态失败: id={}, status={}", id, status, e);
            return ApiResponse.error("切换规则状态失败: " + e.getMessage());
        }
    }

    @PostMapping("/reload")
    @Operation(summary = "重载规则", description = "重新加载所有Drools规则文件")
    public ApiResponse<Void> reloadRules() {
        try {
            ruleEngineService.reloadRules();
            return ApiResponse.success("规则重载成功", null);
        } catch (Exception e) {
            logger.error("规则重载失败", e);
            return ApiResponse.error("规则重载失败: " + e.getMessage());
        }
    }

    @GetMapping("/statistics")
    @Operation(summary = "获取规则统计", description = "获取规则引擎统计信息")
    public ApiResponse<Map<String, Object>> getStatistics() {
        try {
            Map<String, Object> stats = ruleManagementService.getRuleStatistics();
            return ApiResponse.success(stats);
        } catch (Exception e) {
            logger.error("获取统计信息失败", e);
            return ApiResponse.error("获取统计信息失败: " + e.getMessage());
        }
    }

    @GetMapping("/categories")
    @Operation(summary = "获取规则分类", description = "获取所有可用的规则分类列表")
    public ApiResponse<List<String>> getCategories() {
        try {
            List<String> categories = ruleManagementService.getCategories();
            return ApiResponse.success(categories);
        } catch (Exception e) {
            logger.error("获取规则分类失败", e);
            return ApiResponse.error("获取规则分类失败: " + e.getMessage());
        }
    }

    @GetMapping("/logs")
    @Operation(summary = "获取执行日志", description = "获取规则执行日志，支持按资产ID筛选")
    public ApiResponse<List<RuleExecutionLog>> getExecutionLogs(
            @RequestParam(required = false) String assetId) {
        try {
            List<RuleExecutionLog> logs = ruleEngineService.getExecutionLogs(assetId);
            return ApiResponse.success(logs);
        } catch (Exception e) {
            logger.error("获取执行日志失败", e);
            return ApiResponse.error("获取执行日志失败: " + e.getMessage());
        }
    }
}
