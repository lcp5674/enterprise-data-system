package com.enterprise.edams.report.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 报表模板DTO
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Data
@Schema(description = "报表模板数据传输对象")
public class ReportTemplateDTO {

    @Schema(description = "模板ID")
    private Long id;

    @Schema(description = "模板名称")
    private String templateName;

    @Schema(description = "模板编码")
    private String templateCode;

    @Schema(description = "模板类型")
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

    @Schema(description = "布局配置")
    private String layoutConfig;

    @Schema(description = "数据绑定配置")
    private String dataBinding;

    @Schema(description = "支持的格式")
    private String supportedFormats;

    @Schema(description = "状态: 0-禁用, 1-启用")
    private Integer status;

    @Schema(description = "创建人ID")
    private Long creatorId;

    @Schema(description = "创建人名称")
    private String creatorName;

    @Schema(description = "使用次数")
    private Integer usageCount;

    @Schema(description = "创建时间")
    private String createdTime;

    @Schema(description = "更新时间")
    private String updatedTime;
}
