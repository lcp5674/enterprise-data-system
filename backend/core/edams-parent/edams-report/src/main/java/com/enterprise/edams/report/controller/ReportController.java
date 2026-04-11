package com.enterprise.edams.report.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.enterprise.edams.report.dto.ReportCreateRequest;
import com.enterprise.edams.report.dto.ReportDTO;
import com.enterprise.edams.report.service.ReportService;
import com.enterprise.edams.common.result.PageResult;
import com.enterprise.edams.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 报表控制器
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Tag(name = "报表管理", description = "报表的CRUD、生成、导出等接口")
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    @Operation(summary = "创建报表")
    public Result<ReportDTO> createReport(@Valid @RequestBody ReportCreateRequest request) {
        ReportDTO report = reportService.createReport(request, getCurrentUserId(), getCurrentUser());
        return Result.success(report);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新报表")
    public Result<ReportDTO> updateReport(
            @PathVariable Long id,
            @Valid @RequestBody ReportCreateRequest request) {
        ReportDTO report = reportService.updateReport(id, request);
        return Result.success(report);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除报表")
    public Result<Void> deleteReport(@PathVariable Long id) {
        reportService.deleteReport(id);
        return Result.success();
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取报表详情")
    public Result<ReportDTO> getReportById(@PathVariable Long id) {
        ReportDTO report = reportService.getReportById(id);
        return Result.success(report);
    }

    @GetMapping("/code/{reportCode}")
    @Operation(summary = "根据编码获取报表")
    public Result<ReportDTO> getReportByCode(@PathVariable String reportCode) {
        ReportDTO report = reportService.getReportByCode(reportCode);
        return Result.success(report);
    }

    @GetMapping("/list")
    @Operation(summary = "分页查询报表")
    public PageResult<ReportDTO> queryReports(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String reportType,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        IPage<ReportDTO> page = reportService.queryReports(keyword, reportType, status, pageNum, pageSize);
        return PageResult.success(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize());
    }

    @GetMapping("/type/{reportType}")
    @Operation(summary = "根据类型查询报表")
    public Result<List<ReportDTO>> getReportsByType(@PathVariable String reportType) {
        List<ReportDTO> reports = reportService.getReportsByType(reportType);
        return Result.success(reports);
    }

    @GetMapping("/creator/{creatorId}")
    @Operation(summary = "获取用户的报表")
    public Result<List<ReportDTO>> getReportsByCreator(@PathVariable Long creatorId) {
        List<ReportDTO> reports = reportService.getReportsByCreator(creatorId);
        return Result.success(reports);
    }

    @GetMapping("/template/{templateId}")
    @Operation(summary = "根据模板查询报表")
    public Result<List<ReportDTO>> getReportsByTemplate(@PathVariable Long templateId) {
        List<ReportDTO> reports = reportService.getReportsByTemplate(templateId);
        return Result.success(reports);
    }

    @PutMapping("/{id}/enable")
    @Operation(summary = "启用报表")
    public Result<Void> enableReport(@PathVariable Long id) {
        reportService.enableReport(id);
        return Result.success();
    }

    @PutMapping("/{id}/disable")
    @Operation(summary = "禁用报表")
    public Result<Void> disableReport(@PathVariable Long id) {
        reportService.disableReport(id);
        return Result.success();
    }

    @PostMapping("/{id}/execute")
    @Operation(summary = "执行报表")
    public Result<Map<String, Object>> executeReport(@PathVariable Long id) {
        Map<String, Object> result = reportService.executeReport(id);
        return Result.success(result);
    }

    @PostMapping("/{id}/generate")
    @Operation(summary = "生成报表")
    public Result<byte[]> generateReport(
            @PathVariable Long id,
            @RequestBody Map<String, Object> params) {
        byte[] data = reportService.generateReport(id, params);
        return Result.success(data);
    }

    @GetMapping("/{id}/export")
    @Operation(summary = "导出报表")
    public Result<byte[]> exportReport(
            @PathVariable Long id,
            @RequestParam String format,
            @RequestParam(required = false) Map<String, Object> params) {
        byte[] data = reportService.exportReport(id, format, params);
        return Result.success(data);
    }

    @GetMapping("/{id}/preview")
    @Operation(summary = "预览报表")
    public Result<String> previewReport(@PathVariable Long id) {
        String preview = reportService.previewReport(id);
        return Result.success(preview);
    }

    @PostMapping("/{id}/view")
    @Operation(summary = "增加浏览次数")
    public Result<Void> incrementViewCount(@PathVariable Long id) {
        reportService.incrementViewCount(id);
        return Result.success();
    }

    @PostMapping("/{id}/clone")
    @Operation(summary = "克隆报表")
    public Result<ReportDTO> cloneReport(
            @PathVariable Long id,
            @RequestParam String newReportName) {
        ReportDTO report = reportService.cloneReport(id, newReportName, getCurrentUserId(), getCurrentUser());
        return Result.success(report);
    }

    @GetMapping("/count")
    @Operation(summary = "统计报表总数")
    public Result<Long> countTotalReports() {
        long count = reportService.countTotalReports();
        return Result.success(count);
    }

    @GetMapping("/count/status/{status}")
    @Operation(summary = "统计各状态报表数量")
    public Result<Long> countByStatus(@PathVariable Integer status) {
        long count = reportService.countByStatus(status);
        return Result.success(count);
    }

    private Long getCurrentUserId() {
        return 1L;
    }

    private String getCurrentUser() {
        return "system";
    }
}
