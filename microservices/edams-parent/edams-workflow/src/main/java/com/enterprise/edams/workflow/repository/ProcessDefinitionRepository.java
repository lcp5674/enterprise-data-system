package com.enterprise.edams.workflow.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enterprise.edams.workflow.entity.ProcessDefinition;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 流程定义数据访问层
 * 
 * @author EDAMS Team
 */
@Mapper
public interface ProcessDefinitionRepository extends BaseMapper<ProcessDefinition> {

    /**
     * 根据流程Key查询最新版本
     */
    @Select("SELECT * FROM wf_process_definition WHERE process_key = #{processKey} AND is_latest = true AND deleted = false")
    ProcessDefinition findLatestByProcessKey(@Param("processKey") String processKey);

    /**
     * 根据流程Key查询所有版本
     */
    @Select("SELECT * FROM wf_process_definition WHERE process_key = #{processKey} AND deleted = false ORDER BY version DESC")
    List<ProcessDefinition> findByProcessKey(@Param("processKey") String processKey);

    /**
     * 查询最大版本号
     */
    @Select("SELECT MAX(version) FROM wf_process_definition WHERE process_key = #{processKey} AND deleted = false")
    Integer findMaxVersionByProcessKey(@Param("processKey") String processKey);
}
