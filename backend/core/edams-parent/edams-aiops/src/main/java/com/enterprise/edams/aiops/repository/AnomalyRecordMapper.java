package com.enterprise.edams.aiops.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enterprise.edams.aiops.entity.AnomalyRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 异常记录Mapper
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Mapper
public interface AnomalyRecordMapper extends BaseMapper<AnomalyRecord> {

    /**
     * 根据严重程度查询
     */
    @Select("SELECT * FROM anomaly_record WHERE severity = #{severity} AND deleted = 0 ORDER BY detect_time DESC")
    List<AnomalyRecord> findBySeverity(@Param("severity") String severity);

    /**
     * 查询活跃异常
     */
    @Select("SELECT * FROM anomaly_record WHERE status IN ('detected', 'investigating') AND deleted = 0 ORDER BY detect_time DESC")
    List<AnomalyRecord> findActiveAnomalies();

    /**
     * 查询目标关联的异常
     */
    @Select("SELECT * FROM anomaly_record WHERE target_id = #{targetId} AND deleted = 0 ORDER BY detect_time DESC")
    List<AnomalyRecord> findByTargetId(@Param("targetId") String targetId);

    /**
     * 查询持续中的异常
     */
    @Select("SELECT * FROM anomaly_record WHERE end_time IS NULL AND deleted = 0 ORDER BY detect_time DESC")
    List<AnomalyRecord> findOngoingAnomalies();

    /**
     * 更新异常状态
     */
    @Update("UPDATE anomaly_record SET status = #{status}, end_time = #{endTime}, updated_time = #{updatedTime} WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") String status, @Param("endTime") LocalDateTime endTime, @Param("updatedTime") LocalDateTime updatedTime);

    /**
     * 按类型统计异常
     */
    @Select("SELECT anomaly_type, COUNT(*) as count FROM anomaly_record WHERE deleted = 0 GROUP BY anomaly_type")
    List<java.util.Map<String, Object>> countByType();

    /**
     * 查询高置信度异常
     */
    @Select("SELECT * FROM anomaly_record WHERE confidence >= #{minConfidence} AND deleted = 0 ORDER BY confidence DESC")
    List<AnomalyRecord> findHighConfidenceAnomalies(@Param("minConfidence") java.math.BigDecimal minConfidence);
}
