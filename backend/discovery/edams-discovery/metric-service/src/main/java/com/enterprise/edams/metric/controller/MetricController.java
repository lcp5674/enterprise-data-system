package com.enterprise.edams.metric.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.enterprise.edams.metric.entity.BusinessMetric;
import com.enterprise.edams.metric.service.MetricService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 指标管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/metrics")
@RequiredArgsConstructor
public class MetricController {

    private final MetricService metricService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> createMetric(@RequestBody BusinessMetric metric) {
        Long id = metricService.createMetric(metric);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", Map.of("id", id));
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateMetric(@PathVariable Long id, @RequestBody BusinessMetric metric) {
        metricService.updateMetric(id, metric);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "指标更新成功");
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteMetric(@PathVariable Long id) {
        metricService.deleteMetric(id);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "指标删除成功");
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getMetricById(@PathVariable Long id) {
        BusinessMetric metric = metricService.getMetricById(id);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", metric);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<Map<String, Object>> getMetricByCode(@PathVariable String code) {
        BusinessMetric metric = metricService.getMetricByCode(code);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", metric);
        return ResponseEntity.ok(result);
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> listMetrics(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String domain,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        IPage<BusinessMetric> page = metricService.listMetrics(name, domain, status, pageNum, pageSize);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", Map.of(
                "records", page.getRecords(),
                "total", page.getTotal(),
                "pages", page.getPages()
        ));
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}/lineage")
    public ResponseEntity<Map<String, Object>> getMetricLineage(@PathVariable Long id) {
        List<Map<String, Object>> lineage = metricService.getMetricLineage(id);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", lineage);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        Map<String, Object> statistics = metricService.getStatistics();
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", statistics);
        return ResponseEntity.ok(result);
    }
}
