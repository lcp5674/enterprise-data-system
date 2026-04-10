package com.enterprise.edams.knowledge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enterprise.edams.knowledge.entity.Ontology;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 本体Mapper接口
 *
 * @author Knowledge Team
 * @version 1.0.0
 */
@Mapper
public interface OntologyMapper extends BaseMapper<Ontology> {

    /**
     * 根据图谱ID查询本体列表
     *
     * @param graphId 图谱ID
     * @return 本体列表
     */
    List<Ontology> selectByGraphId(@Param("graphId") String graphId);

    /**
     * 根据命名空间查询
     *
     * @param namespace 命名空间
     * @return 本体
     */
    Ontology selectByNamespace(@Param("namespace") String namespace);
}
