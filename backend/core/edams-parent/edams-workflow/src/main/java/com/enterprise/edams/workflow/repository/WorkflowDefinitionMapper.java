package com.enterprise.edams.workflow.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enterprise.edams.workflow.entity.WorkflowDefinition;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 工作流定义Mapper
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Mapper
public interface WorkflowDefinitionMapper extends BaseMapper<WorkflowDefinition> {

    @Select("SELECT * FROM wf_definition WHERE code = #{code} AND deleted = 0")
    WorkflowDefinition findByCode(@Param("code") String code);

    @Select("SELECT * FROM wf_definition WHERE process_def_key = #{key} AND deleted = 0 ORDER BY version DESC LIMIT 1")
    WorkflowDefinition findByProcessDefKey(@Param("key") String key);
}
