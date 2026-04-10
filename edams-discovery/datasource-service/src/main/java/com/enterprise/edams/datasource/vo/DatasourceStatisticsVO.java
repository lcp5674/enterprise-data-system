package com.enterprise.edams.datasource.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 数据源统计VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DatasourceStatisticsVO {

    /**
     * 总数据源数
     */
    private Long totalCount;

    /**
     * 按类型统计
     */
    private Map<String, Long> byType;

    /**
     * 按状态统计
     */
    private Map<String, Long> byStatus;

    /**
     * 按健康状态统计
     */
    private Map<String, Long> byHealthStatus;

    /**
     * 本月新增
     */
    private Long monthlyNewCount;

    /**
     * 本周新增
     */
    private Long weeklyNewCount;

    /**
     * 今日新增
     */
    private Long dailyNewCount;

    /**
     * 已同步数据源数
     */
    private Long syncedCount;

    /**
     * 未同步数据源数
     */
    private Long unsyncedCount;
}
