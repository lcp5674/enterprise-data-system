package com.enterprise.edams.knowledge.dto;

import lombok.Data;

import java.util.List;

/**
 * 实体详情DTO (包含关系)
 */
@Data
public class EntityDetailDTO {
    private EntityDTO entity;
    private List<RelationDTO> outgoingRelations;
    private List<RelationDTO> incomingRelations;
    private List<EntityDTO> similarEntities;
}
