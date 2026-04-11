package com.enterprise.edams.report.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.enterprise.edams.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 报表模板实体
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("edams_report_template")
public class ReportTemplate extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableField("template_name")
    private String templateName;

    @TableField("template_code")
    private String templateCode;

    @TableField("template_type")
    private String templateType;

    @TableField("description")
    private String description;

    @TableField("file_path")
    private String filePath;

    @TableField("file_name")
    private String fileName;

    @TableField("file_size")
    private Long fileSize;

    @TableField("content_type")
    private String contentType;

    @TableField("layout_config")
    private String layoutConfig;

    @TableField("data_binding")
    private String dataBinding;

    @TableField("supported_formats")
    private String supportedFormats;

    @TableField("status")
    private Integer status;

    @TableField("creator_id")
    private Long creatorId;

    @TableField("creator_name")
    private String creatorName;

    @TableField("usage_count")
    private Integer usageCount;
}
