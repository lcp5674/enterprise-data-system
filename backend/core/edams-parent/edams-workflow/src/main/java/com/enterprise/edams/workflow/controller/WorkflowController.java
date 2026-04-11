package com.enterprise.edams.workflow.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.enterprise.edams.common.result.PageResult;
import com.enterprise.edams.common.result.Result;
import com.enterprise.edams.workflow.entity.WorkflowDefinition;
import com.enterprise.edams.workflow.service.WorkflowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 工作流定义管理控制器
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/workflows")
@RequiredArgsConstructor
@Tag(name = "工作流管理", description="流程定义CRUD、发布等接口")
public class WorkflowController {

    private final WorkflowService workflowService;

    @PostMapping
    @Operation(summary = "创建流程定义（草稿）")
    public Result<WorkflowDefinition> create(@Valid @RequestBody WorkflowDefinition definition) {
        return Result.success(workflowService.create(definition, "system"));
    }

    @PostMapping("/{id}/deploy")
    @Operation(summary = "发布流程定义", description="部署到Flowable引擎，使流程可用")
    public Result<WorkflowDefinition> deploy(@PathVariable Long id) {
        return Result.success(workflowService.deploy(id, "system"));
    }

    @GetMapping
    @Operation(summary = "分页查询流程定义")
    public PageResult<WorkflowDefinition> queryDefinitions(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        IPage<WorkflowDefinition> page = workflowService.queryDefinitions(keyword, type, status, pageNum, pageSize);
        return PageResult.success(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize());
    }

    @GetMapping("/published")
    @Operation(summary = "获取所有已发布的流程定义", description="用于发起流程时的下拉选择")
    public Result<List<WorkflowDefinition>> getPublished() {
        return Result.success(workflowService.getPublishedDefinitions());
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取流程定义详情")
    public Result<WorkflowDefinition> getById(@PathVariable Long id) {
        return Result.success(workflowService.getById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新流程定义")
    public Result<Void> update(@PathVariable Long id,
                               @Valid @RequestBody WorkflowDefinition definition) {
        workflowService.update(id, definition, "system");
        return Result.success();
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "修改流程状态", description="启用或禁用流程定义")
    public Result<Void> changeStatus(@PathVariable Long id, @RequestParam Integer status) {
        workflowService.changeStatus(id, status, "system");
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除流程定义", description="只能删除草稿状态的流程")
    public Result<Void> delete(@PathVariable Long id) {
        workflowService.delete(id, "system");
        return Result.success();
    }
}
