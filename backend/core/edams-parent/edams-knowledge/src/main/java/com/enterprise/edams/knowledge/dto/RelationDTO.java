package com.enterprise.edams.knowledge.dto;

import lombok.Data;

/**
 * 关系DTO
 */
@Data
public class RelationDTO {
    private Long id;
    private Long ontologyId;
    private Long sourceEntityId;
    private String sourceEntityName;
    private Long targetEntityId;
    private String targetEntityName;
    private String relationType;
    private String relationName;
    private String description;
    private String direction;
    private String properties;
    private Double weight;
    private Integer confidence;
    private String evidence;
    private Boolean isInferred;
    private String status;
    private String creator;
}
