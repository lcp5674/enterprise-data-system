package com.enterprise.edams.knowledge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enterprise.edams.knowledge.entity.EntityType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 实体类型Mapper接口
 *
 * @author Knowledge Team
 * @version 1.0.0
 */
@Mapper
public interface EntityTypeMapper extends BaseMapper<EntityType> {

    /**
     * 根据本体ID查询实体类型列表
     *
     * @param ontologyId 本体ID
     * @return 实体类型列表
     */
    List<EntityType> selectByOntologyId(@Param("ontologyId") String ontologyId);

    /**
     * 根据类型名称查询
     *
     * @param ontologyId 本体ID
     * @param typeName 类型名称
     * @return 实体类型
     */
    EntityType selectByTypeName(@Param("ontologyId") String ontologyId,
                                 @Param("typeName") String typeName);
}
