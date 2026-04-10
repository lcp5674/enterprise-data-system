package com.enterprise.edams.watermark.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.edams.watermark.dto.*;
import com.enterprise.edams.watermark.entity.*;
import com.enterprise.edams.watermark.service.WatermarkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/watermark")
@Tag(name = "水印管理", description = "水印添加与泄露溯源接口")
public class WatermarkController {
    
    private final WatermarkService watermarkService;
    
    @PostMapping("/add")
    @Operation(summary = "添加水印", description = "为文件添加水印")
    public WatermarkRecord addWatermark(@RequestBody AddWatermarkRequest request) {
        return watermarkService.addWatermark(request);
    }
    
    @PostMapping("/batch")
    @Operation(summary = "批量添加水印", description = "批量为文件添加水印")
    public List<WatermarkRecord> batchAddWatermark(@RequestBody BatchWatermarkRequest request) {
        return watermarkService.batchAddWatermark(request);
    }
    
    @GetMapping("/record/{id}")
    @Operation(summary = "获取水印记录", description = "获取水印记录详情")
    public WatermarkRecord getRecord(
            @Parameter(description = "记录ID") @PathVariable Long id) {
        return watermarkService.getRecord(id);
    }
    
    @GetMapping("/records")
    @Operation(summary = "查询水印记录", description = "分页查询水印记录")
    public Page<WatermarkRecord> searchRecords(RecordSearchRequest request) {
        return watermarkService.searchRecords(request);
    }
    
    @PostMapping("/template")
    @Operation(summary = "创建模板", description = "创建水印模板")
    public WatermarkTemplate createTemplate(
            @io.swagger.v3.oas.annotations.parameters.RequestBody @RequestBody 
            com.enterprise.edams.watermark.dto.TemplateCreateRequest request) {
        return watermarkService.createTemplate(request);
    }
    
    @PutMapping("/template/{id}")
    @Operation(summary = "更新模板", description = "更新水印模板")
    public WatermarkTemplate updateTemplate(
            @PathVariable Long id,
            @RequestBody com.enterprise.edams.watermark.dto.TemplateUpdateRequest request) {
        return watermarkService.updateTemplate(id, request);
    }
    
    @GetMapping("/templates")
    @Operation(summary = "获取模板列表", description = "获取水印模板列表")
    public List<WatermarkTemplate> getTemplates(
            @RequestParam(required = false) WatermarkType type) {
        return watermarkService.getTemplates(type);
    }
    
    @PostMapping("/trace")
    @Operation(summary = "泄露溯源", description = "对泄露文件进行溯源")
    public LeakTrace traceLeakage(
            @RequestParam String fileName,
            @RequestParam String watermarkContent) {
        return watermarkService.traceLeakage(fileName, watermarkContent);
    }
    
    @GetMapping("/traces")
    @Operation(summary = "查询溯源记录", description = "分页查询泄露溯源记录")
    public Page<LeakTrace> searchLeakTraces(LeakTraceSearchRequest request) {
        return watermarkService.searchLeakTraces(request);
    }
    
    @GetMapping("/statistics")
    @Operation(summary = "获取统计", description = "获取泄露溯源统计")
    public LeakStatistics getStatistics() {
        return watermarkService.getStatistics();
    }
}
