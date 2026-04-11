package com.enterprise.dataplatform.quality.controller;

import com.enterprise.dataplatform.quality.dto.request.CheckExecutionRequest;
import com.enterprise.dataplatform.quality.dto.response.QualityCheckResultResponse;
import com.enterprise.dataplatform.quality.service.QualityCheckService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 质量检查REST控制器
 */
@RestController
@RequestMapping("/api/v1/quality/checks")
@RequiredArgsConstructor
@Tag(name = "质量检查管理", description = "数据质量检查执行和结果查询")
public class QualityCheckController {

    private final QualityCheckService checkService;

    @PostMapping
    @Operation(summary = "执行质量检查")
    public ResponseEntity<Map<String, Object>> executeCheck(
            @Valid @RequestBody CheckExecutionRequest request,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String executor) {
        request.setExecutor(executor);
        QualityCheckResultResponse response = checkService.executeCheck(request, executor);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(wrapResponse(response));
    }

    @PostMapping("/batch")
    @Operation(summary = "批量执行检查")
    public ResponseEntity<Map<String, Object>> batchExecuteCheck(
            @RequestParam List<Long> taskIds,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String executor) {
        List<QualityCheckResultResponse> responses = checkService.batchExecuteCheck(taskIds, executor);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(wrapResponse(responses));
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询检查结果")
    public ResponseEntity<Map<String, Object>> getCheckResult(@PathVariable Long id) {
        QualityCheckResultResponse response = checkService.getCheckResult(id);
        return ResponseEntity.ok(wrapResponse(response));
    }

    private Map<String, Object> wrapResponse(Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "success");
        response.put("data", data);
        return response;
    }
}
