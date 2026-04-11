package com.enterprise.edams.analytics.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 分析报告DTO
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Data
@Schema(description = "分析报告数据传输对象")
public class AnalyticsReportDTO {

    @Schema(description = "报告ID")
    private Long id;

    @Schema(description = "报告名称")
    private String reportName;

    @Schema(description = "报告编码")
    private String reportCode;

    @Schema(description = "报告类型")
    private String reportType;

    @Schema(description = "数据源ID")
    private Long datasourceId;

    @Schema(description = "SQL查询语句")
    private String querySql;

    @Schema(description = "报告配置")
    private Map<String, Object> config;

    @Schema(description = "报告描述")
    private String description;

    @Schema(description = "图表类型")
    private String chartType;

    @Schema(description = "维度字段列表")
    private List<Map<String, Object>> dimensions;

    @Schema(description = "指标字段列表")
    private List<Map<String, Object>> metrics;

    @Schema(description = "筛选条件")
    private Map<String, Object> filters;

    @Schema(description = "排序规则")
    private List<Map<String, Object>> sortRules;

    @Schema(description = "数据刷新周期")
    private String refreshType;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "访问权限")
    private String accessLevel;

    @Schema(description = "创建人ID")
    private Long creatorId;

    @Schema(description = "创建人名称")
    private String creatorName;

    @Schema(description = "最后执行时间")
    private String lastExecuteTime;

    @Schema(description = "最后执行状态")
    private String lastExecuteStatus;

    @Schema(description = "执行次数")
    private Integer executeCount;

    @Schema(description = "浏览次数")
    private Integer viewCount;

    @Schema(description = "创建时间")
    private String createdTime;

    @Schema(description = "更新时间")
    private String updatedTime;
}
