package com.edams.sandbox.service;

import com.edams.common.model.PageResult;
import com.edams.sandbox.entity.Sandbox;
import com.edams.sandbox.entity.SandboxExecution;

import java.util.Map;

public interface SandboxService {
    
    // 沙箱管理
    Sandbox createSandbox(Sandbox sandbox);
    Sandbox updateSandbox(Long id, Sandbox sandbox);
    void deleteSandbox(Long id);
    Sandbox getSandboxById(Long id);
    PageResult<Sandbox> listSandboxes(Map<String, Object> params);
    void startSandbox(Long sandboxId);
    void stopSandbox(Long sandboxId);
    
    // SQL演练
    SandboxExecution executeSql(Long sandboxId, String sql, Long userId);
    PageResult<SandboxExecution> listSqlExecutions(Long sandboxId, Map<String, Object> params);
    
    // API测试
    SandboxExecution testApi(Long sandboxId, String apiUrl, String method, Map<String, Object> params, Long userId);
    PageResult<SandboxExecution> listApiTests(Long sandboxId, Map<String, Object> params);
    
    // 数据模拟
    SandboxExecution simulateData(Long sandboxId, String simulationType, Map<String, Object> config, Long userId);
    
    // 沙箱状态管理
    void expireSandboxes();
    Map<String, Object> getSandboxStats(Long sandboxId);
}