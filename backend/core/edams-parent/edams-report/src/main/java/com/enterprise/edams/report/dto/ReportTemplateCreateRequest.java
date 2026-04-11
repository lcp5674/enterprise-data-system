package com.enterprise.edams.report.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 报表模板创建请求
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Data
@Schema(description = "报表模板创建请求")
public class ReportTemplateCreateRequest {

    @NotBlank(message = "模板名称不能为空")
    @Schema(description = "模板名称", required = true)
    private String templateName;

    @Schema(description = "模板编码")
    private String templateCode;

    @NotBlank(message = "模板类型不能为空")
    @Schema(description = "模板类型: JASPER/EXCEL/WORD/CUSTOM", required = true)
    private String templateType;

    @Schema(description = "模板描述")
    private String description;

    @Schema(description = "文件路径")
    private String filePath;

    @Schema(description = "文件名称")
    private String fileName;

    @Schema(description = "文件大小")
    private Long fileSize;

    @Schema(description = "内容类型")
    private String contentType;

    @Schema(description = "布局配置(JSON)")
    private String layoutConfig;

    @Schema(description = "数据绑定配置(JSON)")
    private String dataBinding;

    @Schema(description = "支持的格式: PDF,EXCEL,WORD,HTML")
    private String supportedFormats;
}
