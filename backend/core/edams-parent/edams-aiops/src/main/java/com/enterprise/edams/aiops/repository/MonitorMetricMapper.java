package com.enterprise.edams.aiops.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enterprise.edams.aiops.entity.MonitorMetric;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 监控指标Mapper
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Mapper
public interface MonitorMetricMapper extends BaseMapper<MonitorMetric> {

    /**
     * 根据目标ID查询最新指标
     */
    @Select("SELECT * FROM monitor_metric WHERE target_id = #{targetId} ORDER BY collect_time DESC LIMIT 1")
    MonitorMetric findLatestByTargetId(@Param("targetId") String targetId);

    /**
     * 查询时间范围内的指标
     */
    @Select("SELECT * FROM monitor_metric WHERE target_id = #{targetId} AND metric_name = #{metricName} " +
            "AND collect_time BETWEEN #{startTime} AND #{endTime} ORDER BY collect_time ASC")
    List<MonitorMetric> findByTimeRange(@Param("targetId") String targetId,
                                         @Param("metricName") String metricName,
                                         @Param("startTime") LocalDateTime startTime,
                                         @Param("endTime") LocalDateTime endTime);

    /**
     * 查询超过阈值的指标
     */
    @Select("SELECT * FROM monitor_metric WHERE metric_name = #{metricName} AND metric_value > #{threshold} " +
            "AND collect_time > #{since} ORDER BY collect_time DESC")
    List<MonitorMetric> findAboveThreshold(@Param("metricName") String metricName,
                                           @Param("threshold") java.math.BigDecimal threshold,
                                           @Param("since") LocalDateTime since);

    /**
     * 按指标类型统计
     */
    @Select("SELECT metric_type, COUNT(*) as count FROM monitor_metric WHERE deleted = 0 GROUP BY metric_type")
    List<java.util.Map<String, Object>> countByMetricType();

    /**
     * 查询未处理的告警关联指标
     */
    @Select("SELECT m.* FROM monitor_metric m INNER JOIN alert_rule r ON m.metric_name = r.metric_name " +
            "WHERE r.enabled = 1 AND m.deleted = 0 ORDER BY m.collect_time DESC LIMIT #{limit}")
    List<MonitorMetric> findForAlertEvaluation(@Param("limit") int limit);
}
