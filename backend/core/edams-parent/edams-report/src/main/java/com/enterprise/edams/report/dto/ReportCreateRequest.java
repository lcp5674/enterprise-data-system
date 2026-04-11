package com.enterprise.edams.report.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 报表创建请求
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Data
@Schema(description = "报表创建请求")
public class ReportCreateRequest {

    @NotBlank(message = "报表名称不能为空")
    @Schema(description = "报表名称", required = true)
    private String reportName;

    @Schema(description = "报表编码")
    private String reportCode;

    @NotBlank(message = "报表类型不能为空")
    @Schema(description = "报表类型", required = true)
    private String reportType;

    @Schema(description = "报表描述")
    private String description;

    @Schema(description = "数据源ID")
    private Long datasourceId;

    @Schema(description = "模板ID")
    private Long templateId;

    @Schema(description = "SQL查询语句")
    private String querySql;

    @Schema(description = "参数配置(JSON)")
    private String parameters;

    @Schema(description = "文件格式: PDF/EXCEL/WORD/HTML")
    private String fileFormat;
}
