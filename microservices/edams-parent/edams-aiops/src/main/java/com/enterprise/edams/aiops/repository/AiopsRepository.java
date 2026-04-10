package com.enterprise.edams.aiops.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enterprise.edams.aiops.model.AnomalyRecord;
import com.enterprise.edams.aiops.model.CapacityMetrics;
import com.enterprise.edams.aiops.model.HealthScore;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AIOps数据访问层
 *
 * @author AIOps Team
 */
@Mapper
public interface AiopsRepository {

    // ==================== AnomalyRecord 操作 ====================

    /**
     * 插入异常记录
     */
    int insertAnomalyRecord(AnomalyRecord record);

    /**
     * 查询异常记录列表
     */
    List<AnomalyRecord> findAnomalyRecords(
            @Param("serviceName") String serviceName,
            @Param("status") String status,
            @Param("severity") String severity,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * 根据ID查询异常记录
     */
    AnomalyRecord findAnomalyRecordById(@Param("id") String id);

    /**
     * 更新异常记录状态
     */
    int updateAnomalyRecordStatus(@Param("id") String id, @Param("status") String status);

    /**
     * 查询未解决的异常记录
     */
    List<AnomalyRecord> findUnresolvedAnomalies(@Param("since") LocalDateTime since);

    /**
     * 统计异常数量
     */
    int countAnomaliesBySeverity(@Param("severity") String severity, @Param("since") LocalDateTime since);

    // ==================== CapacityMetrics 操作 ====================

    /**
     * 插入容量指标
     */
    int insertCapacityMetrics(CapacityMetrics metrics);

    /**
     * 查询容量指标历史
     */
    List<CapacityMetrics> findCapacityMetricsHistory(
            @Param("resourceName") String resourceName,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * 查询最新容量指标
     */
    CapacityMetrics findLatestCapacityMetrics(@Param("resourceName") String resourceName);

    /**
     * 查询即将耗尽的资源
     */
    List<CapacityMetrics> findResourcesNearExhaustion(@Param("threshold") Double threshold);

    /**
     * 更新容量预测数据
     */
    int updateCapacityForecast(
            @Param("id") String id,
            @Param("forecastDataPoints") String forecastDataPoints,
            @Param("estimatedExhaustionDate") LocalDateTime estimatedExhaustionDate);

    // ==================== HealthScore 操作 ====================

    /**
     * 插入健康评分
     */
    int insertHealthScore(HealthScore healthScore);

    /**
     * 查询健康评分历史
     */
    List<HealthScore> findHealthScoreHistory(
            @Param("objectName") String objectName,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * 查询最新健康评分
     */
    HealthScore findLatestHealthScore(@Param("objectName") String objectName);

    /**
     * 查询健康评分下降趋势
     */
    List<HealthScore> findDegradingHealthScores(@Param("since") LocalDateTime since);

    /**
     * 查询风险服务
     */
    List<HealthScore> findAtRiskServices(@Param("threshold") Integer threshold);

    /**
     * 更新健康评分
     */
    int updateHealthScore(HealthScore healthScore);
}
