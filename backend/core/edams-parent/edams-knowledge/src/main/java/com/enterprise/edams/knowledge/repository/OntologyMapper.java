package com.enterprise.edams.knowledge.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enterprise.edams.knowledge.entity.Ontology;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 本体论Mapper
 */
@Mapper
public interface OntologyMapper extends BaseMapper<Ontology> {

    /**
     * 查询所有已发布的本体论
     */
    @Select("SELECT * FROM kb_ontology WHERE status = 'PUBLISHED' ORDER BY update_time DESC")
    List<Ontology> selectPublishedOntologies();

    /**
     * 查询用户的本体论
     */
    @Select("SELECT * FROM kb_ontology WHERE creator = #{creator} ORDER BY update_time DESC")
    List<Ontology> selectByCreator(@Param("creator") String creator);

    /**
     * 获取本体论统计信息
     */
    @Select("SELECT COUNT(*) FROM kb_ontology WHERE status = #{status}")
    Integer countByStatus(@Param("status") String status);
}
