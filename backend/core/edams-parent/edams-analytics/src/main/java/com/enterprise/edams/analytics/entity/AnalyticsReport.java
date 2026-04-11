package com.enterprise.edams.analytics.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.enterprise.edams.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 分析报告实体
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("edams_analytics_report")
public class AnalyticsReport extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 报告名称 */
    @TableField("report_name")
    private String reportName;

    /** 报告编码 */
    @TableField("report_code")
    private String reportCode;

    /** 报告类型: DASHBOARD/TABLEAU/POWERBI/CUSTOM */
    @TableField("report_type")
    private String reportType;

    /** 数据源ID */
    @TableField("datasource_id")
    private Long datasourceId;

    /** SQL查询语句 */
    @TableField("query_sql")
    private String querySql;

    /** 报告配置(JSON) */
    @TableField("config_json")
    private String configJson;

    /** 报告描述 */
    @TableField("description")
    private String description;

    /** 图表类型: TABLE/CHART/PIVOT/MAP */
    @TableField("chart_type")
    private String chartType;

    /** 维度字段(JSON数组) */
    @TableField("dimensions")
    private String dimensions;

    /** 指标字段(JSON数组) */
    @TableField("metrics")
    private String metrics;

    /** 筛选条件(JSON) */
    @TableField("filters")
    private String filters;

    /** 排序规则(JSON) */
    @TableField("sort_rules")
    private String sortRules;

    /** 数据刷新周期: REALTIME/HOURLY/DAILY/WEEKLY/MANUAL */
    @TableField("refresh_type")
    private String refreshType;

    /** 状态: 0-草稿, 1-已发布, 2-已归档 */
    @TableField("status")
    private Integer status;

    /** 访问权限: PUBLIC/DEPT/PRIVATE */
    @TableField("access_level")
    private String accessLevel;

    /** 允许访问的部门ID列表 */
    @TableField("allowed_dept_ids")
    private String allowedDeptIds;

    /** 创建人ID */
    @TableField("creator_id")
    private Long creatorId;

    /** 创建人名称 */
    @TableField("creator_name")
    private String creatorName;

    /** 最后执行时间 */
    @TableField("last_execute_time")
    private String lastExecuteTime;

    /** 最后执行状态: SUCCESS/FAILED/RUNNING */
    @TableField("last_execute_status")
    private String lastExecuteStatus;

    /** 执行次数 */
    @TableField("execute_count")
    private Integer executeCount;

    /** 浏览次数 */
    @TableField("view_count")
    private Integer viewCount;
}
