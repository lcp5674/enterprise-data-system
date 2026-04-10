package com.enterprise.edams.workflow.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enterprise.edams.workflow.entity.ProcessTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 流程任务数据访问层
 * 
 * @author EDAMS Team
 */
@Mapper
public interface ProcessTaskRepository extends BaseMapper<ProcessTask> {

    /**
     * 根据Flowable任务ID查询
     */
    @Select("SELECT * FROM wf_process_task WHERE flowable_task_id = #{flowableTaskId} AND deleted = false")
    ProcessTask findByFlowableTaskId(@Param("flowableTaskId") String flowableTaskId);

    /**
     * 查询流程实例的任务列表
     */
    @Select("SELECT * FROM wf_process_task WHERE process_instance_id = #{processInstanceId} AND deleted = false ORDER BY created_time DESC")
    List<ProcessTask> findByProcessInstanceId(@Param("processInstanceId") String processInstanceId);

    /**
     * 查询用户的待办任务
     */
    @Select("SELECT * FROM wf_process_task WHERE assignee_id = #{assigneeId} AND status = 0 AND deleted = false ORDER BY created_time DESC")
    List<ProcessTask> findTodoByAssigneeId(@Param("assigneeId") String assigneeId);

    /**
     * 查询用户的已办任务
     */
    @Select("SELECT * FROM wf_process_task WHERE assignee_id = #{assigneeId} AND status = 1 AND deleted = false ORDER BY handle_time DESC")
    List<ProcessTask> findDoneByAssigneeId(@Param("assigneeId") String assigneeId);

    /**
     * 查询用户的抄送任务
     */
    @Select("SELECT * FROM wf_process_task WHERE task_type = 2 AND (candidate_ids LIKE CONCAT('%', #{userId}, '%')) AND deleted = false ORDER BY created_time DESC")
    List<ProcessTask> findCcByUserId(@Param("userId") String userId);
}
