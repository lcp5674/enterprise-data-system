package com.enterprise.edams.analytics.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enterprise.edams.analytics.entity.AnalysisTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 分析任务Mapper
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Mapper
public interface AnalysisTaskMapper extends BaseMapper<AnalysisTask> {

    /**
     * 根据任务名称查询
     */
    @Select("SELECT * FROM edams_analysis_task WHERE task_name = #{taskName} AND deleted = 0 LIMIT 1")
    AnalysisTask selectByTaskName(@Param("taskName") String taskName);

    /**
     * 根据任务类型查询
     */
    @Select("SELECT * FROM edams_analysis_task WHERE task_type = #{taskType} AND deleted = 0 ORDER BY created_time DESC")
    List<AnalysisTask> selectByTaskType(@Param("taskType") String taskType);

    /**
     * 根据状态查询
     */
    @Select("SELECT * FROM edams_analysis_task WHERE status = #{status} AND deleted = 0 ORDER BY created_time DESC")
    List<AnalysisTask> selectByStatus(@Param("status") String status);

    /**
     * 根据创建人查询
     */
    @Select("SELECT * FROM edams_analysis_task WHERE creator = #{creator} AND deleted = 0 ORDER BY created_time DESC")
    List<AnalysisTask> selectByCreator(@Param("creator") String creator);

    /**
     * 更新任务状态
     */
    @Update("UPDATE edams_analysis_task SET status = #{status}, updated_time = NOW() WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") String status);

    /**
     * 更新执行结果
     */
    @Update("UPDATE edams_analysis_task SET status = #{status}, execution_time = #{executionTime}, " +
            "result_rows = #{resultRows}, error_message = #{errorMessage}, updated_time = NOW() WHERE id = #{id}")
    int updateExecuteResult(@Param("id") Long id, @Param("status") String status,
                           @Param("executionTime") Long executionTime, @Param("resultRows") Long resultRows,
                           @Param("errorMessage") String errorMessage);

    /**
     * 统计各状态任务数量
     */
    @Select("SELECT COUNT(1) FROM edams_analysis_task WHERE status = #{status} AND deleted = 0")
    int countByStatus(@Param("status") String status);

    /**
     * 查询待执行的任务
     */
    @Select("SELECT * FROM edams_analysis_task WHERE status = 'PENDING' AND deleted = 0 ORDER BY created_time ASC LIMIT #{limit}")
    List<AnalysisTask> selectPendingTasks(@Param("limit") Integer limit);
}
