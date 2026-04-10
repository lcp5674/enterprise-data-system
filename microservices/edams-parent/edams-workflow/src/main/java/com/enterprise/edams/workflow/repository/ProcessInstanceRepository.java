package com.enterprise.edams.workflow.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enterprise.edams.workflow.entity.ProcessInstance;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 流程实例数据访问层
 * 
 * @author EDAMS Team
 */
@Mapper
public interface ProcessInstanceRepository extends BaseMapper<ProcessInstance> {

    /**
     * 根据Flowable实例ID查询
     */
    @Select("SELECT * FROM wf_process_instance WHERE flowable_instance_id = #{flowableInstanceId} AND deleted = false")
    ProcessInstance findByFlowableInstanceId(@Param("flowableInstanceId") String flowableInstanceId);

    /**
     * 根据业务ID查询
     */
    @Select("SELECT * FROM wf_process_instance WHERE business_id = #{businessId} AND deleted = false")
    ProcessInstance findByBusinessId(@Param("businessId") String businessId);

    /**
     * 查询用户发起的流程实例
     */
    @Select("SELECT * FROM wf_process_instance WHERE starter_id = #{starterId} AND deleted = false ORDER BY created_time DESC")
    List<ProcessInstance> findByStarterId(@Param("starterId") String starterId);

    /**
     * 查询待处理的流程实例
     */
    @Select("SELECT * FROM wf_process_instance WHERE status = 0 AND deleted = false ORDER BY created_time DESC")
    List<ProcessInstance> findRunningInstances();
}
