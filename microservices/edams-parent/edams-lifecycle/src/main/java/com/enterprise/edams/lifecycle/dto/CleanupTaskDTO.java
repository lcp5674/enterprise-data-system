package com.enterprise.edams.lifecycle.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 清理任务DTO
 * 
 * @author EDAMS Team
 */
@Data
@Schema(description = "清理任务")
public class CleanupTaskDTO {

    @Schema(description = "任务ID")
    private String id;

    @Schema(description = "任务名称")
    private String name;

    @Schema(description = "任务描述")
    private String description;

    @Schema(description = "业务类型")
    private String businessType;

    @Schema(description = "数据表名")
    private String tableName;

    @Schema(description = "清理方式：1-物理删除，2-逻辑删除，3-归档后删除")
    private Integer cleanupType;

    @Schema(description = "执行周期（cron表达式）")
    private String cronExpression;

    @Schema(description = "是否启用")
    private Boolean enabled;

    @Schema(description = "执行超时时间（分钟）")
    private Integer timeoutMinutes;

    @Schema(description = "批量处理大小")
    private Integer batchSize;

    @Schema(description = "清理前是否备份")
    private Boolean backupBeforeDelete;

    @Schema(description = "最后执行时间")
    private LocalDateTime lastExecuteTime;

    @Schema(description = "下次执行时间")
    private LocalDateTime nextExecuteTime;

    @Schema(description = "执行次数")
    private Integer executeCount;

    @Schema(description = "成功次数")
    private Integer successCount;

    @Schema(description = "失败次数")
    private Integer failCount;

    @Schema(description = "清理记录总数")
    private Long totalCleanedCount;

    @Schema(description = "创建人")
    private String createdBy;

    @Schema(description = "创建时间")
    private LocalDateTime createdTime;

    @Schema(description = "更新时间")
    private LocalDateTime updatedTime;
}
