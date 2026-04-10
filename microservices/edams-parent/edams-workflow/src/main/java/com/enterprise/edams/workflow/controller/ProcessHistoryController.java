package com.enterprise.edams.workflow.controller;

import com.enterprise.edams.common.result.Result;
import com.enterprise.edams.workflow.dto.ProcessHistoryDTO;
import com.enterprise.edams.workflow.service.ProcessHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 流程历史记录控制器
 *
 * @author EDAMS Team
 */
@RestController
@RequestMapping("/api/v1/process-history")
@RequiredArgsConstructor
@Tag(name = "流程历史记录", description = "流程历史记录相关接口")
public class ProcessHistoryController {

    private final ProcessHistoryService processHistoryService;

    @GetMapping("/instance/{instanceId}")
    @Operation(summary = "查询流程实例的历史记录")
    public Result<List<ProcessHistoryDTO>> getProcessHistory(@PathVariable String instanceId) {
        return Result.success(processHistoryService.getProcessHistory(instanceId));
    }

    @GetMapping("/instance/{instanceId}/node/{nodeId}")
    @Operation(summary = "查询节点的历史记录")
    public Result<List<ProcessHistoryDTO>> getNodeHistory(
            @PathVariable String instanceId,
            @PathVariable String nodeId) {
        return Result.success(processHistoryService.getNodeHistory(instanceId, nodeId));
    }
}
