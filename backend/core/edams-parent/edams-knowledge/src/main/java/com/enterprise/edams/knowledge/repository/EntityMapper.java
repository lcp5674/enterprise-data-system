package com.enterprise.edams.knowledge.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.edams.knowledge.entity.Entity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 实体Mapper
 */
@Mapper
public interface EntityMapper extends BaseMapper<Entity> {

    /**
     * 分页查询实体
     */
    @Select("SELECT * FROM kb_entity WHERE ontology_id = #{ontologyId} AND status = 'ACTIVE' ORDER BY update_time DESC")
    IPage<Entity> selectPageByOntologyId(Page<Entity> page, @Param("ontologyId") Long ontologyId);

    /**
     * 根据类ID查询实体
     */
    @Select("SELECT * FROM kb_entity WHERE class_id = #{classId} AND status = 'ACTIVE' ORDER BY update_time DESC")
    List<Entity> selectByClassId(@Param("classId") Long classId);

    /**
     * 模糊搜索实体
     */
    @Select("SELECT * FROM kb_entity WHERE (name LIKE CONCAT('%', #{keyword}, '%') " +
           "OR alias LIKE CONCAT('%', #{keyword}, '%') OR description LIKE CONCAT('%', #{keyword}, '%')) " +
           "AND status = 'ACTIVE' ORDER BY confidence DESC, view_count DESC LIMIT #{limit}")
    List<Entity> searchEntities(@Param("keyword") String keyword, @Param("limit") Integer limit);

    /**
     * 根据唯一标识查询
     */
    @Select("SELECT * FROM kb_entity WHERE unique_id = #{uniqueId} AND status = 'ACTIVE'")
    Entity selectByUniqueId(@Param("uniqueId") String uniqueId);

    /**
     * 查询实体数量
     */
    @Select("SELECT COUNT(*) FROM kb_entity WHERE ontology_id = #{ontologyId} AND status = 'ACTIVE'")
    Long countByOntologyId(@Param("ontologyId") Long ontologyId);

    /**
     * 查询热门实体
     */
    @Select("SELECT * FROM kb_entity WHERE status = 'ACTIVE' ORDER BY view_count DESC, favorite_count DESC LIMIT #{limit}")
    List<Entity> selectHotEntities(@Param("limit") Integer limit);
}
