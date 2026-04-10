package com.enterprise.edams.knowledge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enterprise.edams.knowledge.entity.RelationType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 关系类型Mapper接口
 *
 * @author Knowledge Team
 * @version 1.0.0
 */
@Mapper
public interface RelationTypeMapper extends BaseMapper<RelationType> {

    /**
     * 根据本体ID查询关系类型列表
     *
     * @param ontologyId 本体ID
     * @return 关系类型列表
     */
    List<RelationType> selectByOntologyId(@Param("ontologyId") String ontologyId);

    /**
     * 根据类型名称查询
     *
     * @param ontologyId 本体ID
     * @param typeName 类型名称
     * @return 关系类型
     */
    RelationType selectByTypeName(@Param("ontologyId") String ontologyName,
                                   @Param("typeName") String typeName);
}
