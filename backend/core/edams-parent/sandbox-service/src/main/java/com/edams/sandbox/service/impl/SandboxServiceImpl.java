package com.edams.sandbox.service.impl;

import com.edams.common.model.PageResult;
import com.edams.common.utils.JsonUtils;
import com.edams.common.utils.PageUtils;
import com.edams.sandbox.entity.Sandbox;
import com.edams.sandbox.entity.SandboxExecution;
import com.edams.sandbox.repository.SandboxExecutionRepository;
import com.edams.sandbox.repository.SandboxRepository;
import com.edams.sandbox.service.SandboxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SandboxServiceImpl implements SandboxService {
    
    private final SandboxRepository sandboxRepository;
    private final SandboxExecutionRepository executionRepository;
    
    @Override
    @Transactional
    public Sandbox createSandbox(Sandbox sandbox) {
        log.info("Creating sandbox: {}", sandbox.getName());
        return sandboxRepository.save(sandbox);
    }
    
    @Override
    @Transactional
    public Sandbox updateSandbox(Long id, Sandbox sandbox) {
        log.info("Updating sandbox: {}", id);
        Sandbox existing = getSandboxById(id);
        existing.setName(sandbox.getName());
        existing.setDescription(sandbox.getDescription());
        existing.setResourceConfig(sandbox.getResourceConfig());
        existing.setExpireTime(sandbox.getExpireTime());
        return sandboxRepository.save(existing);
    }
    
    @Override
    @Transactional
    public void deleteSandbox(Long id) {
        log.info("Deleting sandbox: {}", id);
        sandboxRepository.deleteById(id);
    }
    
    @Override
    public Sandbox getSandboxById(Long id) {
        return sandboxRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sandbox not found: " + id));
    }
    
    @Override
    public PageResult<Sandbox> listSandboxes(Map<String, Object> params) {
        Specification<Sandbox> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (params.get("ownerId") != null) {
                predicates.add(cb.equal(root.get("ownerId"), params.get("ownerId")));
            }
            if (params.get("status") != null) {
                predicates.add(cb.equal(root.get("status"), params.get("status")));
            }
            if (params.get("sandboxType") != null) {
                predicates.add(cb.equal(root.get("sandboxType"), params.get("sandboxType")));
            }
            if (params.get("name") != null) {
                predicates.add(cb.like(root.get("name"), "%" + params.get("name") + "%"));
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        
        Pageable pageable = PageUtils.getPageable(params);
        Page<Sandbox> page = sandboxRepository.findAll(spec, pageable);
        return PageResult.of(page);
    }
    
    @Override
    @Transactional
    public void startSandbox(Long sandboxId) {
        Sandbox sandbox = getSandboxById(sandboxId);
        sandbox.setStatus("RUNNING");
        sandboxRepository.save(sandbox);
        log.info("Sandbox started: {}", sandboxId);
    }
    
    @Override
    @Transactional
    public void stopSandbox(Long sandboxId) {
        Sandbox sandbox = getSandboxById(sandboxId);
        sandbox.setStatus("STOPPED");
        sandboxRepository.save(sandbox);
        log.info("Sandbox stopped: {}", sandboxId);
    }
    
    @Override
    @Transactional
    public SandboxExecution executeSql(Long sandboxId, String sql, Long userId) {
        log.info("Executing SQL in sandbox {}: {}", sandboxId, sql);
        
        SandboxExecution execution = new SandboxExecution();
        execution.setSandboxId(sandboxId);
        execution.setExecutionType("SQL_EXECUTION");
        execution.setContent(sql);
        execution.setExecutedBy(userId);
        execution.setStatus("RUNNING");
        execution.setStartTime(LocalDateTime.now());
        
        execution = executionRepository.save(execution);
        
        // 模拟SQL执行结果
        try {
            Thread.sleep(500); // 模拟执行耗时
            execution.setStatus("SUCCESS");
            execution.setResult(JsonUtils.toJson(Map.of(
                "rows_affected", 1,
                "execution_time", 500,
                "data", List.of(Map.of("id", 1, "name", "test"))
            )));
        } catch (Exception e) {
            execution.setStatus("FAILED");
            execution.setErrorMessage("SQL execution failed: " + e.getMessage());
        }
        
        execution.setEndTime(LocalDateTime.now());
        execution.setDuration(500L);
        
        return executionRepository.save(execution);
    }
    
    @Override
    public PageResult<SandboxExecution> listSqlExecutions(Long sandboxId, Map<String, Object> params) {
        Specification<SandboxExecution> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("sandboxId"), sandboxId));
            predicates.add(cb.equal(root.get("executionType"), "SQL_EXECUTION"));
            
            if (params.get("status") != null) {
                predicates.add(cb.equal(root.get("status"), params.get("status")));
            }
            if (params.get("startTime") != null && params.get("endTime") != null) {
                predicates.add(cb.between(root.get("createdTime"), 
                    (LocalDateTime) params.get("startTime"), 
                    (LocalDateTime) params.get("endTime")));
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        
        Pageable pageable = PageUtils.getPageable(params);
        Page<SandboxExecution> page = executionRepository.findAll(spec, pageable);
        return PageResult.of(page);
    }
    
    @Override
    @Transactional
    public SandboxExecution testApi(Long sandboxId, String apiUrl, String method, Map<String, Object> params, Long userId) {
        log.info("Testing API in sandbox {}: {} {}", sandboxId, method, apiUrl);
        
        SandboxExecution execution = new SandboxExecution();
        execution.setSandboxId(sandboxId);
        execution.setExecutionType("API_TEST");
        execution.setContent(apiUrl);
        execution.setParameters(JsonUtils.toJson(params));
        execution.setExecutedBy(userId);
        execution.setStatus("RUNNING");
        execution.setStartTime(LocalDateTime.now());
        
        execution = executionRepository.save(execution);
        
        // 模拟API测试结果
        try {
            Thread.sleep(300); // 模拟网络请求耗时
            execution.setStatus("SUCCESS");
            execution.setResult(JsonUtils.toJson(Map.of(
                "status_code", 200,
                "response_time", 300,
                "response", Map.of("success", true, "message", "API test successful")
            )));
        } catch (Exception e) {
            execution.setStatus("FAILED");
            execution.setErrorMessage("API test failed: " + e.getMessage());
        }
        
        execution.setEndTime(LocalDateTime.now());
        execution.setDuration(300L);
        
        return executionRepository.save(execution);
    }
    
    @Override
    public PageResult<SandboxExecution> listApiTests(Long sandboxId, Map<String, Object> params) {
        Specification<SandboxExecution> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("sandboxId"), sandboxId));
            predicates.add(cb.equal(root.get("executionType"), "API_TEST"));
            
            if (params.get("status") != null) {
                predicates.add(cb.equal(root.get("status"), params.get("status")));
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        
        Pageable pageable = PageUtils.getPageable(params);
        Page<SandboxExecution> page = executionRepository.findAll(spec, pageable);
        return PageResult.of(page);
    }
    
    @Override
    @Transactional
    public SandboxExecution simulateData(Long sandboxId, String simulationType, Map<String, Object> config, Long userId) {
        log.info("Simulating data in sandbox {}: {}", sandboxId, simulationType);
        
        SandboxExecution execution = new SandboxExecution();
        execution.setSandboxId(sandboxId);
        execution.setExecutionType("DATA_SIMULATION");
        execution.setContent(simulationType);
        execution.setParameters(JsonUtils.toJson(config));
        execution.setExecutedBy(userId);
        execution.setStatus("RUNNING");
        execution.setStartTime(LocalDateTime.now());
        
        execution = executionRepository.save(execution);
        
        // 模拟数据生成
        try {
            Thread.sleep(1000);
            execution.setStatus("SUCCESS");
            execution.setResult(JsonUtils.toJson(Map.of(
                "records_generated", 1000,
                "simulation_time", 1000,
                "data_schema", List.of("id", "name", "value", "timestamp")
            )));
        } catch (Exception e) {
            execution.setStatus("FAILED");
            execution.setErrorMessage("Data simulation failed: " + e.getMessage());
        }
        
        execution.setEndTime(LocalDateTime.now());
        execution.setDuration(1000L);
        
        return executionRepository.save(execution);
    }
    
    @Override
    @Transactional
    public void expireSandboxes() {
        log.info("Checking expired sandboxes");
        List<Sandbox> expired = sandboxRepository.findExpiredSandboxes(LocalDateTime.now());
        
        for (Sandbox sandbox : expired) {
            sandbox.setStatus("EXPIRED");
            sandboxRepository.save(sandbox);
            log.info("Marked sandbox as expired: {}", sandbox.getId());
        }
    }
    
    @Override
    public Map<String, Object> getSandboxStats(Long sandboxId) {
        Sandbox sandbox = getSandboxById(sandboxId);
        
        List<SandboxExecution> executions = executionRepository.findBySandboxId(sandboxId);
        long successCount = executions.stream().filter(e -> "SUCCESS".equals(e.getStatus())).count();
        long failedCount = executions.stream().filter(e -> "FAILED".equals(e.getStatus())).count();
        long totalDuration = executions.stream().mapToLong(e -> e.getDuration() != null ? e.getDuration() : 0).sum();
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("sandboxInfo", sandbox);
        stats.put("totalExecutions", executions.size());
        stats.put("successCount", successCount);
        stats.put("failedCount", failedCount);
        stats.put("successRate", executions.size() > 0 ? (double) successCount / executions.size() : 0);
        stats.put("avgDuration", executions.size() > 0 ? totalDuration / executions.size() : 0);
        stats.put("lastExecution", executions.isEmpty() ? null : executions.get(executions.size() - 1));
        
        return stats;
    }
}