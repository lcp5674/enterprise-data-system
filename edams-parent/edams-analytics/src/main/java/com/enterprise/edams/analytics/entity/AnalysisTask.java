package com.enterprise.edams.analytics.entity;

import com.enterprise.edams.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

/**
 * 分析任务实体
 *
 * @author Architecture Team
 * @version 1.0.0
 */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class AnalysisTask extends BaseEntity {

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 任务类型: ADHOC/REPORT/METRIC/VALUE_ASSESSMENT
     */
    private String taskType;

    /**
     * SQL查询语句
     */
    private String querySql;

    /**
     * 数据源ID
     */
    private Long datasourceId;

    /**
     * 任务状态: PENDING/RUNNING/SUCCESS/FAILED
     */
    private String status;

    /**
     * 执行耗时(毫秒)
     */
    private Long executionTime;

    /**
     * 结果数据量
     */
    private Long resultRows;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 任务参数(JSON)
     */
    private String parameters;

    /**
     * 创建人
     */
    private String creator;

    /**
     * 描述
     */
    private String description;
}
