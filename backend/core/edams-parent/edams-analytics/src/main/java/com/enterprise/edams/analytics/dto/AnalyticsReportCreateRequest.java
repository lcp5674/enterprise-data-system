package com.enterprise.edams.analytics.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 分析报告创建请求
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Data
@Schema(description = "分析报告创建请求")
public class AnalyticsReportCreateRequest {

    @NotBlank(message = "报告名称不能为空")
    @Schema(description = "报告名称", required = true)
    private String reportName;

    @Schema(description = "报告编码")
    private String reportCode;

    @NotNull(message = "报告类型不能为空")
    @Schema(description = "报告类型: DASHBOARD/TABLEAU/POWERBI/CUSTOM", required = true)
    private String reportType;

    @Schema(description = "数据源ID")
    private Long datasourceId;

    @Schema(description = "SQL查询语句")
    private String querySql;

    @Schema(description = "报告配置")
    private Map<String, Object> config;

    @Schema(description = "报告描述")
    private String description;

    @Schema(description = "图表类型: TABLE/CHART/PIVOT/MAP")
    private String chartType;

    @Schema(description = "维度字段列表")
    private List<Map<String, Object>> dimensions;

    @Schema(description = "指标字段列表")
    private List<Map<String, Object>> metrics;

    @Schema(description = "筛选条件")
    private Map<String, Object> filters;

    @Schema(description = "排序规则")
    private List<Map<String, Object>> sortRules;

    @Schema(description = "数据刷新周期: REALTIME/HOURLY/DAILY/WEEKLY/MANUAL")
    private String refreshType;

    @Schema(description = "访问权限: PUBLIC/DEPT/PRIVATE")
    private String accessLevel;
}
