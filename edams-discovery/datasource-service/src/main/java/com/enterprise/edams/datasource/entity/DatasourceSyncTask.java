package com.enterprise.edams.datasource.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * 数据源同步任务实体
 */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@TableName("datasource_sync_task")
public class DatasourceSyncTask extends BaseEntity {

    /**
     * 数据源ID
     */
    private String datasourceId;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 同步类型：FULL/INCREMENTAL
     */
    private String syncType;

    /**
     * 同步范围配置(JSON)
     */
    private String syncScope;

    /**
     * 同步状态：PENDING/RUNNING/SUCCESS/FAILED
     */
    private String syncStatus;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 耗时(毫秒)
     */
    private Long durationMs;

    /**
     * 发现的资产数
     */
    private Integer assetsDiscovered;

    /**
     * 创建的资产数
     */
    private Integer assetsCreated;

    /**
     * 更新的资产数
     */
    private Integer assetsUpdated;

    /**
     * 失败的资产数
     */
    private Integer assetsFailed;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 触发类型：MANUAL/SCHEDULED/API
     */
    private String triggerType;

    /**
     * 触发人
     */
    private String triggerBy;
}
