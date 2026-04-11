package com.enterprise.edams.knowledge.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enterprise.edams.knowledge.entity.OntologyClass;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 本体类Mapper
 */
@Mapper
public interface OntologyClassMapper extends BaseMapper<OntologyClass> {

    /**
     * 查询本体论下的所有类
     */
    @Select("SELECT * FROM kb_ontology_class WHERE ontology_id = #{ontologyId} ORDER BY sort_order ASC, id ASC")
    List<OntologyClass> selectByOntologyId(@Param("ontologyId") Long ontologyId);

    /**
     * 查询根类
     */
    @Select("SELECT * FROM kb_ontology_class WHERE ontology_id = #{ontologyId} AND parent_class_id IS NULL ORDER BY sort_order ASC")
    List<OntologyClass> selectRootClasses(@Param("ontologyId") Long ontologyId);

    /**
     * 查询子类的直接子类
     */
    @Select("SELECT * FROM kb_ontology_class WHERE parent_class_id = #{parentClassId} ORDER BY sort_order ASC")
    List<OntologyClass> selectSubclasses(@Param("parentClassId") Long parentClassId);

    /**
     * 获取类的树形结构
     */
    @Select("SELECT * FROM kb_ontology_class WHERE ontology_id = #{ontologyId} ORDER BY level ASC, sort_order ASC")
    List<OntologyClass> selectClassTree(@Param("ontologyId") Long ontologyId);
}
