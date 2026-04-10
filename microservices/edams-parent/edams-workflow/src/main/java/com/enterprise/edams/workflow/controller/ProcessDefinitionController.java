package com.enterprise.edams.workflow.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.edams.common.result.Result;
import com.enterprise.edams.workflow.dto.ProcessDefinitionCreateRequest;
import com.enterprise.edams.workflow.dto.ProcessDefinitionDTO;
import com.enterprise.edams.workflow.entity.ProcessDefinition;
import com.enterprise.edams.workflow.service.ProcessDefinitionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 流程定义控制器
 *
 * @author EDAMS Team
 */
@RestController
@RequestMapping("/api/v1/process-definitions")
@RequiredArgsConstructor
@Tag(name = "流程定义管理", description = "流程定义相关接口")
public class ProcessDefinitionController {

    private final ProcessDefinitionService processDefinitionService;

    @PostMapping
    @Operation(summary = "创建流程定义")
    public Result<ProcessDefinitionDTO> createProcessDefinition(
            @Valid @RequestBody ProcessDefinitionCreateRequest request) {
        return Result.success(processDefinitionService.createProcessDefinition(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新流程定义")
    public Result<ProcessDefinitionDTO> updateProcessDefinition(
            @PathVariable String id,
            @Valid @RequestBody ProcessDefinitionCreateRequest request) {
        return Result.success(processDefinitionService.updateProcessDefinition(id, request));
    }

    @PostMapping("/{id}/deploy")
    @Operation(summary = "发布流程定义")
    public Result<Void> deployProcessDefinition(@PathVariable String id) {
        processDefinitionService.deployProcessDefinition(id);
        return Result.success();
    }

    @PostMapping("/{id}/deactivate")
    @Operation(summary = "停用流程定义")
    public Result<Void> deactivateProcessDefinition(@PathVariable String id) {
        processDefinitionService.deactivateProcessDefinition(id);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除流程定义")
    public Result<Void> deleteProcessDefinition(@PathVariable String id) {
        processDefinitionService.deleteProcessDefinition(id);
        return Result.success();
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取流程定义详情")
    public Result<ProcessDefinitionDTO> getProcessDefinition(@PathVariable String id) {
        return Result.success(processDefinitionService.getProcessDefinition(id));
    }

    @GetMapping
    @Operation(summary = "分页查询流程定义")
    public Result<Page<ProcessDefinitionDTO>> listProcessDefinitions(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer status) {
        Page<ProcessDefinition> page = new Page<>(current, size);
        return Result.success(processDefinitionService.listProcessDefinitions(page, keyword, category, status));
    }

    @GetMapping("/{processKey}/versions")
    @Operation(summary = "获取流程定义的所有版本")
    public Result<List<ProcessDefinitionDTO>> getProcessDefinitionVersions(
            @PathVariable String processKey) {
        return Result.success(processDefinitionService.getProcessDefinitionVersions(processKey));
    }

    @GetMapping("/{id}/bpmn")
    @Operation(summary = "获取BPMN XML")
    public Result<String> getBpmnXml(@PathVariable String id) {
        return Result.success(processDefinitionService.getBpmnXml(id));
    }

    @GetMapping("/{id}/diagram")
    @Operation(summary = "获取流程图片")
    public Result<String> getProcessDiagram(@PathVariable String id) {
        return Result.success(processDefinitionService.getProcessDiagram(id));
    }
}
