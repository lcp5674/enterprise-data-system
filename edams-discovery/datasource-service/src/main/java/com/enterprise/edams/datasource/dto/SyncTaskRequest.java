package com.enterprise.edams.datasource.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.SuperBuilder;

/**
 * 数据源同步任务请求DTO
 */
@Data
@SuperBuilder
public class SyncTaskRequest {

    /**
     * 数据源ID
     */
    @NotNull(message = "数据源ID不能为空")
    private Long datasourceId;

    /**
     * 同步类型：FULL/INCREMENTAL
     */
    private String syncType = "FULL";

    /**
     * 是否强制同步（跳过锁定）
     */
    private Boolean forceSync = false;

    /**
     * 同步选项（JSON格式）
     */
    private String syncOptions;
}
