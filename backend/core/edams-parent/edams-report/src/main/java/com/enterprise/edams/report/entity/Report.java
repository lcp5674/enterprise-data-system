package com.enterprise.edams.report.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.enterprise.edams.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 报表实体
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("edams_report")
public class Report extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableField("report_name")
    private String reportName;

    @TableField("report_code")
    private String reportCode;

    @TableField("report_type")
    private String reportType;

    @TableField("description")
    private String description;

    @TableField("datasource_id")
    private Long datasourceId;

    @TableField("template_id")
    private Long templateId;

    @TableField("query_sql")
    private String querySql;

    @TableField("parameters")
    private String parameters;

    @TableField("file_format")
    private String fileFormat;

    @TableField("status")
    private Integer status;

    @TableField("creator_id")
    private Long creatorId;

    @TableField("creator_name")
    private String creatorName;

    @TableField("execute_count")
    private Integer executeCount;

    @TableField("view_count")
    private Integer viewCount;

    @TableField("last_execute_time")
    private String lastExecuteTime;

    @TableField("last_execute_status")
    private String lastExecuteStatus;
}
