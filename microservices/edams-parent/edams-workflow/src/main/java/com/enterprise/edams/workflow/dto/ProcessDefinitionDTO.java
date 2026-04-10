package com.enterprise.edams.workflow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 流程定义DTO
 * 
 * @author EDAMS Team
 */
@Data
@Schema(description = "流程定义")
public class ProcessDefinitionDTO {

    @Schema(description = "流程定义ID")
    private String id;

    @Schema(description = "流程定义Key")
    private String processKey;

    @Schema(description = "流程定义名称")
    private String name;

    @Schema(description = "流程定义描述")
    private String description;

    @Schema(description = "流程分类")
    private String category;

    @Schema(description = "版本号")
    private Integer version;

    @Schema(description = "状态：0-草稿，1-已发布，2-已停用")
    private Integer status;

    @Schema(description = "是否最新版本")
    private Boolean isLatest;

    @Schema(description = "创建人")
    private String createdBy;

    @Schema(description = "创建时间")
    private LocalDateTime createdTime;

    @Schema(description = "更新时间")
    private LocalDateTime updatedTime;
}
