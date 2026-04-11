package com.enterprise.edams.llm.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enterprise.edams.llm.entity.LlmModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 大模型Mapper
 */
@Mapper
public interface LlmModelMapper extends BaseMapper<LlmModel> {

    /**
     * 查询启用的模型
     */
    @Select("SELECT * FROM llm_model WHERE enabled = true AND status = 'ACTIVE' ORDER BY priority DESC")
    List<LlmModel> selectEnabledModels();

    /**
     * 根据提供商查询
     */
    @Select("SELECT * FROM llm_model WHERE provider = #{provider} AND enabled = true ORDER BY priority DESC")
    List<LlmModel> selectByProvider(@Param("provider") String provider);

    /**
     * 根据模型类型查询
     */
    @Select("SELECT * FROM llm_model WHERE model_type = #{modelType} AND enabled = true ORDER BY priority DESC")
    List<LlmModel> selectByType(@Param("modelType") String modelType);

    /**
     * 根据模型代码查询
     */
    @Select("SELECT * FROM llm_model WHERE model_code = #{modelCode} AND enabled = true LIMIT 1")
    LlmModel selectByCode(@Param("modelCode") String modelCode);
}
