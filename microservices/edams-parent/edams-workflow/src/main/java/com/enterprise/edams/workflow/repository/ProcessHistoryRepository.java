package com.enterprise.edams.workflow.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enterprise.edams.workflow.entity.ProcessHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 流程历史记录数据访问层
 * 
 * @author EDAMS Team
 */
@Mapper
public interface ProcessHistoryRepository extends BaseMapper<ProcessHistory> {

    /**
     * 查询流程实例的历史记录
     */
    @Select("SELECT * FROM wf_process_history WHERE process_instance_id = #{processInstanceId} AND deleted = false ORDER BY operation_time ASC")
    List<ProcessHistory> findByProcessInstanceId(@Param("processInstanceId") String processInstanceId);

    /**
     * 查询节点的历史记录
     */
    @Select("SELECT * FROM wf_process_history WHERE process_instance_id = #{processInstanceId} AND node_id = #{nodeId} AND deleted = false ORDER BY operation_time ASC")
    List<ProcessHistory> findByProcessInstanceIdAndNodeId(@Param("processInstanceId") String processInstanceId, @Param("nodeId") String nodeId);
}
