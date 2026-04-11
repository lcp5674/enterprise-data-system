package com.enterprise.edams.llm.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.edams.llm.dto.LlmModelDTO;
import com.enterprise.edams.llm.entity.LlmModel;
import com.enterprise.edams.llm.service.LlmModelService;
import com.enterprise.edams.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 大模型控制器
 */
@Tag(name = "模型管理", description = "大模型的CRUD操作")
@RestController
@RequestMapping("/api/llm/model")
@RequiredArgsConstructor
public class LlmModelController {

    private final LlmModelService modelService;

    @GetMapping
    @Operation(summary = "分页查询模型")
    public Result<Page<LlmModel>> page(
            @Parameter(description = "模型代码") @RequestParam(required = false) String modelCode,
            @Parameter(description = "模型名称") @RequestParam(required = false) String modelName,
            @Parameter(description = "提供商") @RequestParam(required = false) String provider,
            @Parameter(description = "模型类型") @RequestParam(required = false) String modelType,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int pageSize) {
        LlmModelDTO dto = new LlmModelDTO();
        dto.setModelCode(modelCode);
        dto.setModelName(modelName);
        dto.setProvider(provider);
        dto.setModelType(modelType);
        Page<LlmModel> page = modelService.selectPage(dto, pageNum, pageSize);
        return Result.success(page);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询模型")
    public Result<LlmModelDTO> getById(@PathVariable Long id) {
        LlmModelDTO model = modelService.getById(id);
        return Result.success(model);
    }

    @GetMapping("/code/{modelCode}")
    @Operation(summary = "根据模型代码查询")
    public Result<LlmModelDTO> getByCode(@PathVariable String modelCode) {
        LlmModelDTO model = modelService.getByCode(modelCode);
        return Result.success(model);
    }

    @GetMapping("/enabled")
    @Operation(summary = "获取所有启用的模型")
    public Result<List<LlmModelDTO>> getEnabled() {
        List<LlmModelDTO> models = modelService.getEnabledModels();
        return Result.success(models);
    }

    @GetMapping("/provider/{provider}")
    @Operation(summary = "根据提供商获取模型")
    public Result<List<LlmModelDTO>> getByProvider(@PathVariable String provider) {
        List<LlmModelDTO> models = modelService.getByProvider(provider);
        return Result.success(models);
    }

    @GetMapping("/type/{modelType}")
    @Operation(summary = "根据类型获取模型")
    public Result<List<LlmModelDTO>> getByType(@PathVariable String modelType) {
        List<LlmModelDTO> models = modelService.getByType(modelType);
        return Result.success(models);
    }

    @PostMapping
    @Operation(summary = "创建模型")
    public Result<LlmModelDTO> create(@RequestBody LlmModelDTO dto) {
        LlmModelDTO created = modelService.create(dto);
        return Result.success(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新模型")
    public Result<LlmModelDTO> update(@PathVariable Long id, @RequestBody LlmModelDTO dto) {
        LlmModelDTO updated = modelService.update(id, dto);
        return Result.success(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除模型")
    public Result<Void> delete(@PathVariable Long id) {
        modelService.delete(id);
        return Result.success();
    }

    @PutMapping("/{id}/enabled")
    @Operation(summary = "启用/禁用模型")
    public Result<LlmModelDTO> setEnabled(@PathVariable Long id, @RequestParam boolean enabled) {
        LlmModelDTO model = modelService.setEnabled(id, enabled);
        return Result.success(model);
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "更新模型状态")
    public Result<LlmModelDTO> updateStatus(@PathVariable Long id, @RequestParam String status) {
        LlmModelDTO model = modelService.updateStatus(id, status);
        return Result.success(model);
    }

    @GetMapping("/{id}/test")
    @Operation(summary = "测试模型连接")
    public Result<Boolean> testConnection(@PathVariable Long id) {
        boolean success = modelService.testConnection(id);
        return Result.success(success);
    }
}
