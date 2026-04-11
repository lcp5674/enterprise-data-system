package com.enterprise.edams.knowledge.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enterprise.edams.knowledge.entity.Relation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 关系Mapper
 */
@Mapper
public interface RelationMapper extends BaseMapper<Relation> {

    /**
     * 查询实体的所有关系
     */
    @Select("SELECT * FROM kb_relation WHERE (source_entity_id = #{entityId} OR target_entity_id = #{entityId}) " +
           "AND status = 'ACTIVE' ORDER BY confidence DESC")
    List<Relation> selectByEntityId(@Param("entityId") Long entityId);

    /**
     * 查询实体的出边
     */
    @Select("SELECT * FROM kb_relation WHERE source_entity_id = #{entityId} AND direction = 'DIRECT' " +
           "AND status = 'ACTIVE' ORDER BY confidence DESC")
    List<Relation> selectOutgoingRelations(@Param("entityId") Long entityId);

    /**
     * 查询实体的入边
     */
    @Select("SELECT * FROM kb_relation WHERE target_entity_id = #{entityId} AND direction = 'DIRECT' " +
           "AND status = 'ACTIVE' ORDER BY confidence DESC")
    List<Relation> selectIncomingRelations(@Param("entityId") Long entityId);

    /**
     * 查询指定类型的关系
     */
    @Select("SELECT * FROM kb_relation WHERE ontology_id = #{ontologyId} AND relation_type = #{relationType} " +
           "AND status = 'ACTIVE' ORDER BY confidence DESC")
    List<Relation> selectByType(@Param("ontologyId") Long ontologyId, @Param("relationType") String relationType);

    /**
     * 查询两个实体间的关系
     */
    @Select("SELECT * FROM kb_relation WHERE " +
           "(source_entity_id = #{sourceId} AND target_entity_id = #{targetId}) OR " +
           "(source_entity_id = #{targetId} AND target_entity_id = #{sourceId}) " +
           "AND status = 'ACTIVE'")
    List<Relation> selectBetweenEntities(@Param("sourceId") Long sourceId, @Param("targetId") Long targetId);

    /**
     * 获取关系统计
     */
    @Select("SELECT COUNT(*) FROM kb_relation WHERE ontology_id = #{ontologyId} AND status = 'ACTIVE'")
    Long countByOntologyId(@Param("ontologyId") Long ontologyId);
}
