package com.enterprise.edams.workflow.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enterprise.edams.workflow.entity.WorkflowInstance;
import org.apache.ibatis.annotations.Mapper;

/**
 * 工作流实例Mapper
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Mapper
public interface WorkflowInstanceMapper extends BaseMapper<WorkflowInstance> {
}
