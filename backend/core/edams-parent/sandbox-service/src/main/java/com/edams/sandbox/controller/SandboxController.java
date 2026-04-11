package com.edams.sandbox.controller;

import com.edams.common.model.ApiResponse;
import com.edams.common.model.PageResult;
import com.edams.sandbox.entity.Sandbox;
import com.edams.sandbox.entity.SandboxExecution;
import com.edams.sandbox.service.SandboxService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@Tag(name = "沙箱环境管理")
@RestController
@RequestMapping("/api/sandbox")
@RequiredArgsConstructor
public class SandboxController {
    
    private final SandboxService sandboxService;
    
    @Operation(summary = "创建沙箱")
    @PostMapping
    public ApiResponse<Sandbox> createSandbox(@Valid @RequestBody Sandbox sandbox) {
        return ApiResponse.success(sandboxService.createSandbox(sandbox));
    }
    
    @Operation(summary = "更新沙箱")
    @PutMapping("/{id}")
    public ApiResponse<Sandbox> updateSandbox(
            @PathVariable Long id,
            @Valid @RequestBody Sandbox sandbox) {
        return ApiResponse.success(sandboxService.updateSandbox(id, sandbox));
    }
    
    @Operation(summary = "删除沙箱")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteSandbox(@PathVariable Long id) {
        sandboxService.deleteSandbox(id);
        return ApiResponse.success();
    }
    
    @Operation(summary = "获取沙箱详情")
    @GetMapping("/{id}")
    public ApiResponse<Sandbox> getSandbox(@PathVariable Long id) {
        return ApiResponse.success(sandboxService.getSandboxById(id));
    }
    
    @Operation(summary = "分页查询沙箱列表")
    @GetMapping("/list")
    public ApiResponse<PageResult<Sandbox>> listSandboxes(
            @RequestParam(required = false) Long ownerId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String sandboxType,
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "createdTime,desc") String[] sort) {
        
        Map<String, Object> params = new HashMap<>();
        params.put("ownerId", ownerId);
        params.put("status", status);
        params.put("sandboxType", sandboxType);
        params.put("name", name);
        params.put("page", page);
        params.put("size", size);
        params.put("sort", sort);
        
        return ApiResponse.success(sandboxService.listSandboxes(params));
    }
    
    @Operation(summary = "启动沙箱")
    @PostMapping("/{id}/start")
    public ApiResponse<Void> startSandbox(@PathVariable Long id) {
        sandboxService.startSandbox(id);
        return ApiResponse.success();
    }
    
    @Operation(summary = "停止沙箱")
    @PostMapping("/{id}/stop")
    public ApiResponse<Void> stopSandbox(@PathVariable Long id) {
        sandboxService.stopSandbox(id);
        return ApiResponse.success();
    }
    
    @Operation(summary = "执行SQL")
    @PostMapping("/{id}/sql")
    public ApiResponse<SandboxExecution> executeSql(
            @PathVariable Long id,
            @RequestParam String sql,
            @Parameter(description = "执行用户ID", required = true) @RequestParam Long userId) {
        return ApiResponse.success(sandboxService.executeSql(id, sql, userId));
    }
    
    @Operation(summary = "查询SQL执行记录")
    @GetMapping("/{id}/sql/history")
    public ApiResponse<PageResult<SandboxExecution>> listSqlHistory(
            @PathVariable Long id,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        
        Map<String, Object> params = new HashMap<>();
        params.put("status", status);
        params.put("page", page);
        params.put("size", size);
        
        return ApiResponse.success(sandboxService.listSqlExecutions(id, params));
    }
    
    @Operation(summary = "测试API")
    @PostMapping("/{id}/api-test")
    public ApiResponse<SandboxExecution> testApi(
            @PathVariable Long id,
            @RequestParam String apiUrl,
            @RequestParam(defaultValue = "GET") String method,
            @RequestBody(required = false) Map<String, Object> params,
            @Parameter(description = "测试用户ID", required = true) @RequestParam Long userId) {
        return ApiResponse.success(sandboxService.testApi(id, apiUrl, method, params, userId));
    }
    
    @Operation(summary = "查询API测试记录")
    @GetMapping("/{id}/api-test/history")
    public ApiResponse<PageResult<SandboxExecution>> listApiTestHistory(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        
        Map<String, Object> params = new HashMap<>();
        params.put("page", page);
        params.put("size", size);
        
        return ApiResponse.success(sandboxService.listApiTests(id, params));
    }
    
    @Operation(summary = "数据模拟")
    @PostMapping("/{id}/simulate")
    public ApiResponse<SandboxExecution> simulateData(
            @PathVariable Long id,
            @RequestParam String simulationType,
            @RequestBody Map<String, Object> config,
            @Parameter(description = "模拟用户ID", required = true) @RequestParam Long userId) {
        return ApiResponse.success(sandboxService.simulateData(id, simulationType, config, userId));
    }
    
    @Operation(summary = "获取沙箱统计信息")
    @GetMapping("/{id}/stats")
    public ApiResponse<Map<String, Object>> getSandboxStats(@PathVariable Long id) {
        return ApiResponse.success(sandboxService.getSandboxStats(id));
    }
    
    @Operation(summary = "检查并标记过期沙箱")
    @PostMapping("/expire-check")
    public ApiResponse<Void> expireCheck() {
        sandboxService.expireSandboxes();
        return ApiResponse.success();
    }
}