package com.enterprise.edams.report.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 报表DTO
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Data
@Schema(description = "报表数据传输对象")
public class ReportDTO {

    @Schema(description = "报表ID")
    private Long id;

    @Schema(description = "报表名称")
    private String reportName;

    @Schema(description = "报表编码")
    private String reportCode;

    @Schema(description = "报表类型")
    private String reportType;

    @Schema(description = "报表描述")
    private String description;

    @Schema(description = "数据源ID")
    private Long datasourceId;

    @Schema(description = "模板ID")
    private Long templateId;

    @Schema(description = "SQL查询语句")
    private String querySql;

    @Schema(description = "参数配置")
    private String parameters;

    @Schema(description = "文件格式")
    private String fileFormat;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "创建人ID")
    private Long creatorId;

    @Schema(description = "创建人名称")
    private String creatorName;

    @Schema(description = "执行次数")
    private Integer executeCount;

    @Schema(description = "浏览次数")
    private Integer viewCount;

    @Schema(description = "最后执行时间")
    private String lastExecuteTime;

    @Schema(description = "最后执行状态")
    private String lastExecuteStatus;

    @Schema(description = "创建时间")
    private String createdTime;

    @Schema(description = "更新时间")
    private String updatedTime;
}
