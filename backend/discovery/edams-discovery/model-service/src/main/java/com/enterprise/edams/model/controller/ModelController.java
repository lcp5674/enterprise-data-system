package com.enterprise.edams.model.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.enterprise.edams.model.dto.CreateModelRequest;
import com.enterprise.edams.model.dto.ModelQueryDTO;
import com.enterprise.edams.model.dto.UpdateModelRequest;
import com.enterprise.edams.model.service.ModelService;
import com.enterprise.edams.model.vo.ModelDetailVO;
import com.enterprise.edams.model.vo.ModelStatisticsVO;
import com.enterprise.edams.model.vo.ModelVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据模型控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/models")
@RequiredArgsConstructor
public class ModelController {

    private final ModelService modelService;

    /**
     * 创建数据模型
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createModel(@Valid @RequestBody CreateModelRequest request) {
        Long id = modelService.createModel(request);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "数据模型创建成功");
        result.put("data", Map.of("id", id));
        return ResponseEntity.ok(result);
    }

    /**
     * 更新数据模型
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateModel(
            @PathVariable Long id,
            @Valid @RequestBody UpdateModelRequest request) {
        modelService.updateModel(id, request);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "数据模型更新成功");
        return ResponseEntity.ok(result);
    }

    /**
     * 删除数据模型
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteModel(@PathVariable Long id) {
        modelService.deleteModel(id);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "数据模型删除成功");
        return ResponseEntity.ok(result);
    }

    /**
     * 发布模型
     */
    @PostMapping("/{id}/publish")
    public ResponseEntity<Map<String, Object>> publishModel(@PathVariable Long id) {
        modelService.publishModel(id);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "模型发布成功");
        return ResponseEntity.ok(result);
    }

    /**
     * 废弃模型
     */
    @PostMapping("/{id}/deprecate")
    public ResponseEntity<Map<String, Object>> deprecateModel(@PathVariable Long id) {
        modelService.deprecateModel(id);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "模型废弃成功");
        return ResponseEntity.ok(result);
    }

    /**
     * 获取模型详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getModelDetail(@PathVariable Long id) {
        ModelDetailVO detail = modelService.getModelDetail(id);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", detail);
        return ResponseEntity.ok(result);
    }

    /**
     * 分页查询模型
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> listModels(ModelQueryDTO query) {
        IPage<ModelVO> page = modelService.listModels(query);
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
     * 获取模型树
     */
    @GetMapping("/tree")
    public ResponseEntity<Map<String, Object>> getModelTree() {
        List<ModelVO> tree = modelService.getModelTree();
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", tree);
        return ResponseEntity.ok(result);
    }

    /**
     * 获取模型统计
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        ModelStatisticsVO statistics = modelService.getStatistics();
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", statistics);
        return ResponseEntity.ok(result);
    }

    /**
     * 检查编码唯一性
     */
    @GetMapping("/check-code/{code}")
    public ResponseEntity<Map<String, Object>> checkCodeUnique(@PathVariable String code) {
        boolean unique = modelService.isCodeUnique(code);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", Map.of("unique", unique));
        return ResponseEntity.ok(result);
    }
}
