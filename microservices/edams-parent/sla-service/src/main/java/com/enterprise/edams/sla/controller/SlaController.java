package com.enterprise.edams.sla.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.edams.sla.dto.*;
import com.enterprise.edams.sla.entity.SlaDefinition;
import com.enterprise.edams.sla.service.SlaDefinitionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/sla")
@Tag(name = "SLA管理", description = "SLA定义与监控接口")
public class SlaController {
    
    private final SlaDefinitionService slaDefinitionService;
    
    @PostMapping
    @Operation(summary = "创建SLA", description = "创建SLA定义")
    public SlaDefinition createSla(@RequestBody SlaCreateRequest request) {
        return slaDefinitionService.createSla(request);
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "更新SLA", description = "更新SLA定义")
    public SlaDefinition updateSla(
            @PathVariable Long id,
            @RequestBody SlaUpdateRequest request) {
        return slaDefinitionService.updateSla(id, request);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "删除SLA", description = "删除SLA定义")
    public void deleteSla(@PathVariable Long id) {
        slaDefinitionService.deleteSla(id);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "获取SLA详情", description = "获取SLA详细信息")
    public SlaDefinition getSla(@PathVariable Long id) {
        return slaDefinitionService.getSlaById(id);
    }
    
    @GetMapping
    @Operation(summary = "查询SLA列表", description = "分页查询SLA列表")
    public Page<SlaDefinition> searchSlas(SlaSearchRequest request) {
        return slaDefinitionService.searchSlas(request);
    }
    
    @PostMapping("/{id}/enable")
    @Operation(summary = "启用SLA", description = "启用指定SLA")
    public void enableSla(@PathVariable Long id) {
        slaDefinitionService.enableSla(id);
    }
    
    @PostMapping("/{id}/disable")
    @Operation(summary = "停用SLA", description = "停用指定SLA")
    public void disableSla(@PathVariable Long id) {
        slaDefinitionService.disableSla(id);
    }
    
    @GetMapping("/statistics")
    @Operation(summary = "获取SLA统计", description = "获取SLA统计信息")
    public SlaStatistics getStatistics() {
        return slaDefinitionService.getSlaStatistics();
    }
    
    @GetMapping("/{id}/compliance")
    @Operation(summary = "获取SLA达标率", description = "获取指定SLA的达标率")
    public SlaCompliance getCompliance(@PathVariable Long id) {
        return slaDefinitionService.getSlaCompliance(id);
    }
}
