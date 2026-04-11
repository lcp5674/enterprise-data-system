package com.enterprise.edams.knowledge.dto;

import lombok.Data;

import java.util.List;

/**
 * 知识图谱搜索结果DTO
 */
@Data
public class KnowledgeSearchDTO {
    private Long ontologyId;
    private String keyword;
    private List<EntityDTO> entities;
    private List<RelationDTO> relations;
    private List<OntologyClassDTO> classes;
    private Long totalEntities;
    private Long totalRelations;
    private Long totalClasses;
}
