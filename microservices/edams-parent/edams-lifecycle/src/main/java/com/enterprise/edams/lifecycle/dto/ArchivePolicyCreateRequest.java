package com.enterprise.edams.lifecycle.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 归档策略创建请求
 * 
 * @author EDAMS Team
 */
@Data
@Schema(description = "归档策略创建请求")
public class ArchivePolicyCreateRequest {

    @NotBlank(message = "策略名称不能为空")
    @Schema(description = "策略名称", required = true)
    private String name;

    @NotBlank(message = "策略编码不能为空")
    @Schema(description = "策略编码", required = true)
    private String code;

    @Schema(description = "策略描述")
    private String description;

    @Schema(description = "业务类型")
    private String businessType;

    @Schema(description = "数据分类")
    private String dataCategory;

    @NotNull(message = "保留期限不能为空")
    @Schema(description = "保留期限（天）", required = true)
    private Integer retentionDays;

    @NotNull(message = "归档触发条件不能为空")
    @Schema(description = "归档触发条件：1-时间触发，2-容量触发，3-手动触发", required = true)
    private Integer triggerType;

    @Schema(description = "归档触发条件JSON")
    private String triggerCondition;

    @NotNull(message = "归档目标不能为空")
    @Schema(description = "归档目标：1-对象存储，2-文件系统，3-磁带库", required = true)
    private Integer archiveTarget;

    @Schema(description = "归档目标配置JSON")
    private String targetConfig;

    @Schema(description = "压缩方式：0-不压缩，1-GZIP，2-ZIP，3-7Z")
    private Integer compressionType;

    @Schema(description = "加密方式：0-不加密，1-AES256")
    private Integer encryptionType;

    @Schema(description = "是否删除源数据")
    private Boolean deleteSource;

    @Schema(description = "优先级")
    private Integer priority;
}
