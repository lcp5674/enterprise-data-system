package com.enterprise.edams.analytics.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.enterprise.edams.analytics.dto.DashboardConfigCreateRequest;
import com.enterprise.edams.analytics.dto.DashboardConfigDTO;
import com.enterprise.edams.analytics.service.DashboardConfigService;
import com.enterprise.edams.common.result.PageResult;
import com.enterprise.edams.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 仪表盘配置控制器
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/analytics/dashboards")
@RequiredArgsConstructor
@Tag(name = "仪表盘管理", description = "仪表盘的CRUD和配置接口")
public class DashboardController {

    private final DashboardConfigService dashboardService;

    /**
     * 创建仪表盘
     */
    @PostMapping
    @Operation(summary = "创建仪表盘")
    public Result<DashboardConfigDTO> createDashboard(@Valid @RequestBody DashboardConfigCreateRequest request) {
        DashboardConfigDTO dashboard = dashboardService.createDashboard(request, getCurrentUserId(), getCurrentUser());
        return Result.success(dashboard);
    }

    /**
     * 更新仪表盘
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新仪表盘")
    public Result<DashboardConfigDTO> updateDashboard(
            @PathVariable Long id,
            @Valid @RequestBody DashboardConfigCreateRequest request) {
        DashboardConfigDTO dashboard = dashboardService.updateDashboard(id, request);
        return Result.success(dashboard);
    }

    /**
     * 删除仪表盘
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除仪表盘")
    public Result<Void> deleteDashboard(@PathVariable Long id) {
        dashboardService.deleteDashboard(id);
        return Result.success();
    }

    /**
     * 获取仪表盘详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取仪表盘详情")
    public Result<DashboardConfigDTO> getDashboardById(@PathVariable Long id) {
        DashboardConfigDTO dashboard = dashboardService.getDashboardById(id);
        return Result.success(dashboard);
    }

    /**
     * 根据编码获取仪表盘
     */
    @GetMapping("/code/{dashboardCode}")
    @Operation(summary = "根据编码获取仪表盘")
    public Result<DashboardConfigDTO> getDashboardByCode(@PathVariable String dashboardCode) {
        DashboardConfigDTO dashboard = dashboardService.getDashboardByCode(dashboardCode);
        return Result.success(dashboard);
    }

    /**
     * 分页查询仪表盘
     */
    @GetMapping("/list")
    @Operation(summary = "分页查询仪表盘")
    public PageResult<DashboardConfigDTO> queryDashboards(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String dashboardType,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        IPage<DashboardConfigDTO> page = dashboardService.queryDashboards(keyword, dashboardType, status, pageNum, pageSize);
        return PageResult.success(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize());
    }

    /**
     * 根据类型查询仪表盘
     */
    @GetMapping("/type/{dashboardType}")
    @Operation(summary = "根据类型查询仪表盘")
    public Result<List<DashboardConfigDTO>> getDashboardsByType(@PathVariable String dashboardType) {
        List<DashboardConfigDTO> dashboards = dashboardService.getDashboardsByType(dashboardType);
        return Result.success(dashboards);
    }

    /**
     * 获取用户的仪表盘
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "获取用户的仪表盘")
    public Result<List<DashboardConfigDTO>> getDashboardsByUser(@PathVariable Long userId) {
        List<DashboardConfigDTO> dashboards = dashboardService.getDashboardsByUser(userId);
        return Result.success(dashboards);
    }

    /**
     * 获取系统默认仪表盘
     */
    @GetMapping("/default")
    @Operation(summary = "获取系统默认仪表盘")
    public Result<DashboardConfigDTO> getDefaultDashboard() {
        DashboardConfigDTO dashboard = dashboardService.getDefaultDashboard();
        return Result.success(dashboard);
    }

    /**
     * 获取用户默认仪表盘
     */
    @GetMapping("/user/{userId}/default")
    @Operation(summary = "获取用户默认仪表盘")
    public Result<DashboardConfigDTO> getUserDefaultDashboard(@PathVariable Long userId) {
        DashboardConfigDTO dashboard = dashboardService.getUserDefaultDashboard(userId);
        return Result.success(dashboard);
    }

    /**
     * 设置默认仪表盘
     */
    @PutMapping("/{id}/default")
    @Operation(summary = "设置默认仪表盘")
    public Result<Void> setAsDefault(@PathVariable Long id) {
        dashboardService.setAsDefault(id);
        return Result.success();
    }

    /**
     * 启用仪表盘
     */
    @PutMapping("/{id}/enable")
    @Operation(summary = "启用仪表盘")
    public Result<Void> enableDashboard(@PathVariable Long id) {
        dashboardService.enableDashboard(id);
        return Result.success();
    }

    /**
     * 禁用仪表盘
     */
    @PutMapping("/{id}/disable")
    @Operation(summary = "禁用仪表盘")
    public Result<Void> disableDashboard(@PathVariable Long id) {
        dashboardService.disableDashboard(id);
        return Result.success();
    }

    /**
     * 克隆仪表盘
     */
    @PostMapping("/{id}/clone")
    @Operation(summary = "克隆仪表盘")
    public Result<DashboardConfigDTO> cloneDashboard(
            @PathVariable Long id,
            @RequestParam String newDashboardName) {
        DashboardConfigDTO dashboard = dashboardService.cloneDashboard(id, newDashboardName, getCurrentUserId());
        return Result.success(dashboard);
    }

    /**
     * 记录访问
     */
    @PostMapping("/{id}/access")
    @Operation(summary = "记录仪表盘访问")
    public Result<Void> recordAccess(@PathVariable Long id) {
        dashboardService.recordAccess(id);
        return Result.success();
    }

    /**
     * 统计仪表盘总数
     */
    @GetMapping("/count")
    @Operation(summary = "统计仪表盘总数")
    public Result<Long> countTotalDashboards() {
        long count = dashboardService.countTotalDashboards();
        return Result.success(count);
    }

    private Long getCurrentUserId() {
        return 1L;
    }

    private String getCurrentUser() {
        return "system";
    }
}
