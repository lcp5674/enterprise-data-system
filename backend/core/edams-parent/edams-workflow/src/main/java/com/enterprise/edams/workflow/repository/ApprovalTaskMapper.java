package com.enterprise.edams.workflow.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enterprise.edams.workflow.entity.ApprovalTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 审批任务Mapper
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Mapper
public interface ApprovalTaskMapper extends BaseMapper<ApprovalTask> {

    @Select("SELECT COUNT(*) FROM wf_task WHERE instance_id = #{instanceId} AND status = 0")
    int countPendingByInstance(@Param("instanceId") Long instanceId);

    @Select("SELECT COUNT(*) FROM wf_task WHERE assignee_id = #{assigneeId} AND status = 0")
    int countPendingByAssignee(@Param("assigneeId") Long assigneeId);
}
