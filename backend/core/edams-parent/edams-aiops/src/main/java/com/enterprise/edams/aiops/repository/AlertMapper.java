package com.enterprise.edams.aiops.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enterprise.edams.aiops.entity.Alert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 告警Mapper
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Mapper
public interface AlertMapper extends BaseMapper<Alert> {

    /**
     * 根据告警级别查询
     */
    @Select("SELECT * FROM aiop_alert WHERE alert_level = #{level} AND deleted = 0 ORDER BY alert_time DESC")
    List<Alert> findByLevel(@Param("level") String level);

    /**
     * 根据状态查询未处理告警
     */
    @Select("SELECT * FROM aiop_alert WHERE alert_status IN ('pending', 'acknowledged') AND deleted = 0 ORDER BY alert_time DESC")
    List<Alert> findPendingAlerts();

    /**
     * 查询目标关联的活跃告警
     */
    @Select("SELECT * FROM aiop_alert WHERE target_id = #{targetId} AND alert_status NOT IN ('resolved', 'closed') AND deleted = 0")
    List<Alert> findActiveByTargetId(@Param("targetId") String targetId);

    /**
     * 确认告警
     */
    @Update("UPDATE aiop_alert SET alert_status = 'acknowledged', ack_time = #{ackTime}, ack_by = #{ackBy}, updated_time = #{updatedTime} WHERE id = #{id}")
    int acknowledgeAlert(@Param("id") Long id, @Param("ackTime") LocalDateTime ackTime, @Param("ackBy") String ackBy, @Param("updatedTime") LocalDateTime updatedTime);

    /**
     * 解决告警
     */
    @Update("UPDATE aiop_alert SET alert_status = 'resolved', resolve_time = #{resolveTime}, resolve_by = #{resolveBy}, solution = #{solution}, updated_time = #{updatedTime} WHERE id = #{id}")
    int resolveAlert(@Param("id") Long id, @Param("resolveTime") LocalDateTime resolveTime, @Param("resolveBy") String resolveBy, @Param("solution") String solution, @Param("updatedTime") LocalDateTime updatedTime);

    /**
     * 按级别统计告警数量
     */
    @Select("SELECT alert_level, COUNT(*) as count FROM aiop_alert WHERE deleted = 0 GROUP BY alert_level")
    List<java.util.Map<String, Object>> countByLevel();

    /**
     * 查询时间范围内的告警
     */
    @Select("SELECT * FROM aiop_alert WHERE alert_time BETWEEN #{startTime} AND #{endTime} AND deleted = 0 ORDER BY alert_time DESC")
    List<Alert> findByTimeRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
}
