package com.enterprise.edams.knowledge.controller;

import com.enterprise.edams.common.result.Result;
import com.enterprise.edams.knowledge.dto.DataImportDTO;
import com.enterprise.edams.knowledge.dto.DataImportResultDTO;
import com.enterprise.edams.knowledge.service.DataImportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 数据导入接口
 *
 * @author Knowledge Team
 * @version 1.0.0
 */
@Slf4j
@Tag(name = "数据导入", description = "从各种数据源导入节点和边数据")
@RestController
@RequestMapping("/api/v1/knowledge/import")
@RequiredArgsConstructor
public class DataImportController {

    private final DataImportService dataImportService;

    @Operation(summary = "导入数据", description = "从文件或数据源导入节点和边数据到图谱")
    @PostMapping
    public Result<DataImportResultDTO> importData(@RequestBody DataImportDTO dto) {
        log.info("开始导入数据: graphId={}, type={}", dto.getGraphId(), dto.getImportType());
        DataImportResultDTO result = dataImportService.importData(dto);
        return Result.success(result);
    }

    @Operation(summary = "上传文件并导入", description = "上传文件并导入到指定图谱")
    @PostMapping("/upload")
    public Result<DataImportResultDTO> uploadAndImport(
            @Parameter(description = "图谱ID") @RequestParam String graphId,
            @Parameter(description = "节点类型") @RequestParam String nodeType,
            @Parameter(description = "文件") @RequestParam("file") MultipartFile file) {
        log.info("上传文件并导入: graphId={}, fileName={}", graphId, file.getOriginalFilename());
        DataImportResultDTO result = dataImportService.importFromFile(graphId, nodeType, file);
        return Result.success(result);
    }

    @Operation(summary = "获取导入状态", description = "获取数据导入任务的状态")
    @GetMapping("/status/{taskId}")
    public Result<DataImportResultDTO> getImportStatus(
            @Parameter(description = "任务ID") @PathVariable String taskId) {
        DataImportResultDTO result = dataImportService.getImportStatus(taskId);
        return Result.success(result);
    }

    @Operation(summary = "取消导入任务", description = "取消正在执行的导入任务")
    @DeleteMapping("/cancel/{taskId}")
    public Result<Void> cancelImport(
            @Parameter(description = "任务ID") @PathVariable String taskId) {
        dataImportService.cancelImport(taskId);
        return Result.success("导入任务已取消", null);
    }

    @Operation(summary = "从数据库导入", description = "从现有数据库表导入数据到图谱")
    @PostMapping("/database")
    public Result<DataImportResultDTO> importFromDatabase(
            @Parameter(description = "图谱ID") @RequestParam String graphId,
            @Parameter(description = "数据源ID") @RequestParam String datasourceId,
            @Parameter(description = "SQL查询") @RequestParam String sql,
            @Parameter(description = "节点类型") @RequestParam String nodeType,
            @Parameter(description = "名称字段") @RequestParam String nameField) {
        log.info("从数据库导入: graphId={}, datasourceId={}", graphId, datasourceId);
        DataImportResultDTO result = dataImportService.importFromDatabase(graphId, datasourceId, sql, nodeType, nameField);
        return Result.success(result);
    }
}
