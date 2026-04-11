package com.enterprise.edams.aiops.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enterprise.edams.aiops.entity.OpsTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 运维任务Mapper
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Mapper
public interface OpsTaskMapper extends BaseMapper<OpsTask> {

    /**
     * 根据状态查询任务
     */
    @Select("SELECT * FROM ops_task WHERE task_status = #{status} AND deleted = 0 ORDER BY priority DESC, planned_start_time ASC")
    List<OpsTask> findByStatus(@Param("status") String status);

    /**
     * 查询待执行任务
     */
    @Select("SELECT * FROM ops_task WHERE task_status = 'pending' AND planned_start_time <= #{now} AND deleted = 0 ORDER BY priority DESC, planned_start_time ASC")
    List<OpsTask> findPendingTasks(@Param("now") LocalDateTime now);

    /**
     * 查询执行中的任务
     */
    @Select("SELECT * FROM ops_task WHERE task_status = 'running' AND deleted = 0 ORDER BY actual_start_time ASC")
    List<OpsTask> findRunningTasks();

    /**
     * 查询目标关联的任务
     */
    @Select("SELECT * FROM ops_task WHERE target_id = #{targetId} AND deleted = 0 ORDER BY created_time DESC")
    List<OpsTask> findByTargetId(@Param("targetId") String targetId);

    /**
     * 查询超时任务
     */
    @Select("SELECT * FROM ops_task WHERE task_status IN ('pending', 'running') AND planned_end_time < #{now} AND deleted = 0")
    List<OpsTask> findOverdueTasks(@Param("now") LocalDateTime now);

    /**
     * 更新任务状态
     */
    @Update("UPDATE ops_task SET task_status = #{status}, progress_percent = #{progress}, updated_time = #{updatedTime} WHERE id = #{id}")
    int updateTaskStatus(@Param("id") Long id, @Param("status") String status, @Param("progress") Integer progress, @Param("updatedTime") LocalDateTime updatedTime);

    /**
     * 开始执行任务
     */
    @Update("UPDATE ops_task SET task_status = 'running', actual_start_time = #{startTime}, progress_percent = 0, updated_time = #{updatedTime} WHERE id = #{id}")
    int startTask(@Param("id") Long id, @Param("startTime") LocalDateTime startTime, @Param("updatedTime") LocalDateTime updatedTime);

    /**
     * 完成执行任务
     */
    @Update("UPDATE ops_task SET task_status = #{status}, actual_end_time = #{endTime}, result = #{result}, output_log = #{outputLog}, progress_percent = 100, updated_time = #{updatedTime} WHERE id = #{id}")
    int completeTask(@Param("id") Long id, @Param("status") String status, @Param("endTime") LocalDateTime endTime, @Param("result") String result, @Param("outputLog") String outputLog, @Param("updatedTime") LocalDateTime updatedTime);

    /**
     * 按类型统计任务
     */
    @Select("SELECT task_type, COUNT(*) as count FROM ops_task WHERE deleted = 0 GROUP BY task_type")
    List<java.util.Map<String, Object>> countByType();
}
