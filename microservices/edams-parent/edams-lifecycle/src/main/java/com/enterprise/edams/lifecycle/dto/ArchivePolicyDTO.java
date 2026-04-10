package com.enterprise.edams.lifecycle.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 归档策略DTO
 * 
 * @author EDAMS Team
 */
@Data
@Schema(description = "归档策略")
public class ArchivePolicyDTO {

    @Schema(description = "策略ID")
    private String id;

    @Schema(description = "策略名称")
    private String name;

    @Schema(description = "策略编码")
    private String code;

    @Schema(description = "策略描述")
    private String description;

    @Schema(description = "业务类型")
    private String businessType;

    @Schema(description = "数据分类")
    private String dataCategory;

    @Schema(description = "保留期限（天）")
    private Integer retentionDays;

    @Schema(description = "归档触发条件：1-时间触发，2-容量触发，3-手动触发")
    private Integer triggerType;

    @Schema(description = "归档目标：1-对象存储，2-文件系统，3-磁带库")
    private Integer archiveTarget;

    @Schema(description = "压缩方式：0-不压缩，1-GZIP，2-ZIP，3-7Z")
    private Integer compressionType;

    @Schema(description = "加密方式：0-不加密，1-AES256")
    private Integer encryptionType;

    @Schema(description = "是否删除源数据")
    private Boolean deleteSource;

    @Schema(description = "是否启用")
    private Boolean enabled;

    @Schema(description = "优先级")
    private Integer priority;

    @Schema(description = "最后执行时间")
    private LocalDateTime lastExecuteTime;

    @Schema(description = "执行次数")
    private Integer executeCount;

    @Schema(description = "成功次数")
    private Integer successCount;

    @Schema(description = "失败次数")
    private Integer failCount;

    @Schema(description = "创建人")
    private String createdBy;

    @Schema(description = "创建时间")
    private LocalDateTime createdTime;

    @Schema(description = "更新时间")
    private LocalDateTime updatedTime;
}
