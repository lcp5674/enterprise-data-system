package com.enterprise.edams.datasource.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.enterprise.edams.datasource.dto.*;
import com.enterprise.edams.datasource.entity.DatasourceConfig;
import com.enterprise.edams.datasource.service.DatasourceConfigService;
import com.enterprise.edams.datasource.vo.DatasourceDetailVO;
import com.enterprise.edams.datasource.vo.DatasourceStatisticsVO;
import com.enterprise.edams.datasource.vo.DatasourceVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据源配置管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/datasources")
@RequiredArgsConstructor
public class DatasourceConfigController {

    private final DatasourceConfigService datasourceConfigService;

    /**
     * 创建数据源配置
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createDatasource(@Valid @RequestBody CreateDatasourceRequest request) {
        Long id = datasourceConfigService.createDatasource(request);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "数据源配置创建成功");
        result.put("data", Map.of("id", id));
        
        return ResponseEntity.ok(result);
    }

    /**
     * 更新数据源配置
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateDatasource(
            @PathVariable Long id,
            @Valid @RequestBody UpdateDatasourceRequest request) {
        
        datasourceConfigService.updateDatasource(id, request);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "数据源配置更新成功");
        
        return ResponseEntity.ok(result);
    }

    /**
     * 删除数据源配置
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteDatasource(@PathVariable Long id) {
        datasourceConfigService.deleteDatasource(id);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "数据源配置删除成功");
        
        return ResponseEntity.ok(result);
    }

    /**
     * 获取数据源详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getDatasourceDetail(@PathVariable Long id) {
        DatasourceDetailVO detail = datasourceConfigService.getDatasourceDetail(id);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", detail);
        
        return ResponseEntity.ok(result);
    }

    /**
     * 分页查询数据源列表
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> listDatasources(DatasourceQueryDTO query) {
        IPage<DatasourceVO> page = datasourceConfigService.listDatasources(query);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", Map.of(
                "records", page.getRecords(),
                "total", page.getTotal(),
                "pages", page.getPages(),
                "current", page.getCurrent(),
                "size", page.getSize()
        ));
        
        return ResponseEntity.ok(result);
    }

    /**
     * 启用数据源
     */
    @PostMapping("/{id}/enable")
    public ResponseEntity<Map<String, Object>> enableDatasource(@PathVariable Long id) {
        datasourceConfigService.enableDatasource(id);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "数据源启用成功");
        
        return ResponseEntity.ok(result);
    }

    /**
     * 禁用数据源
     */
    @PostMapping("/{id}/disable")
    public ResponseEntity<Map<String, Object>> disableDatasource(@PathVariable Long id) {
        datasourceConfigService.disableDatasource(id);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "数据源禁用成功");
        
        return ResponseEntity.ok(result);
    }

    /**
     * 测试数据源连接
     */
    @PostMapping("/test-connection")
    public ResponseEntity<Map<String, Object>> testConnection(@Valid @RequestBody ConnectionTestRequest request) {
        ConnectionTestResponse response = datasourceConfigService.testConnection(request);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", response.getSuccess());
        result.put("data", response);
        
        return ResponseEntity.ok(result);
    }

    /**
     * 测试已存在的数据源连接
     */
    @PostMapping("/{id}/test-connection")
    public ResponseEntity<Map<String, Object>> testDatasourceConnection(@PathVariable Long id) {
        ConnectionTestResponse response = datasourceConfigService.testDatasourceConnection(id);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", response.getSuccess());
        result.put("data", response);
        
        return ResponseEntity.ok(result);
    }

    /**
     * 获取数据源统计信息
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        DatasourceStatisticsVO statistics = datasourceConfigService.getStatistics();
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", statistics);
        
        return ResponseEntity.ok(result);
    }

    /**
     * 根据编码获取数据源
     */
    @GetMapping("/code/{code}")
    public ResponseEntity<Map<String, Object>> getByCode(@PathVariable String code) {
        DatasourceConfig config = datasourceConfigService.getByCode(code);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", config);
        
        return ResponseEntity.ok(result);
    }

    /**
     * 批量获取数据源
     */
    @PostMapping("/batch")
    public ResponseEntity<Map<String, Object>> listByCodes(@RequestBody List<String> codes) {
        List<DatasourceConfig> configs = datasourceConfigService.listByCodes(codes);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", configs);
        
        return ResponseEntity.ok(result);
    }

    /**
     * 验证数据源编码唯一性
     */
    @GetMapping("/check-code/{code}")
    public ResponseEntity<Map<String, Object>> checkCodeUnique(@PathVariable String code) {
        boolean unique = datasourceConfigService.isCodeUnique(code);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", Map.of("unique", unique));
        
        return ResponseEntity.ok(result);
    }
}
